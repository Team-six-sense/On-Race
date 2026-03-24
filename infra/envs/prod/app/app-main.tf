# 1. Base 계층의 상태(State) 불러오기
data "terraform_remote_state" "base" {
  backend = "s3" 

  config = {
    # base-providers.tf에 적힌 내용과 '똑같이' 맞춰줍니다.
    bucket = "t6-on-race-terraform-state-prod"
    key    = "prod/base/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

# 2. EKS 모듈 호출 (Base의 VPC, 서브넷 정보 활용)
module "eks" {
  source = "../../../modules/eks"

  project_name = var.project_name
  environment  = var.environment
  cluster_name = "${var.project_name}-${var.environment}-cluster"
  
  vpc_id     = data.terraform_remote_state.base.outputs.vpc_id
  subnet_ids = data.terraform_remote_state.base.outputs.private_subnets
  
  instance_types = ["m5.large"]
  min_size       = 2
  max_size       = 15  
}

# 3. Loki(로키) 엔진 인프라 호출
module "loki" {
  source            = "../../../modules/loki"
  project_name      = var.project_name
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

# 4. EBS CSI 드라이버 및 스토리지 권한 설정
resource "aws_eks_addon" "ebs_csi" {
  cluster_name                = module.eks.cluster_name
  addon_name                  = "aws-ebs-csi-driver"
  resolve_conflicts_on_update = "OVERWRITE"

  depends_on = [module.eks]
}

# KEDA를 위한 휴식 시간 (30초)
resource "time_sleep" "wait_30_seconds_for_keda" {
  depends_on = [helm_release.keda]
  create_duration = "30s"
}

# Karpenter를 위한 휴식 시간 (30초)
resource "time_sleep" "wait_30_seconds_for_karpenter" {
  depends_on = [helm_release.karpenter]
  create_duration = "60s"
}

resource "aws_iam_role_policy_attachment" "node_ebs_policy" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
  role       = module.eks.node_iam_role_name 
  depends_on = [module.eks]
}

# 5. 프로바이더 설정 (EKS 인증 연동)
provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name]
    command     = "aws"
  }
}

provider "helm" {
  kubernetes {
    host                   = module.eks.cluster_endpoint
    cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
    
    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name]
      command     = "aws"
    }
  }
}

# 6. Loki Helm 배포
resource "helm_release" "loki" {
  name             = "loki"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "loki" 
  namespace        = "loki"
  create_namespace = true

  values = [
    file("${path.module}/helm-values/loki-values.yaml")
  ]

  depends_on = [module.eks, module.loki] 
}

# 7. Grafana Helm 배포
resource "helm_release" "grafana" {
  name             = "grafana"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "grafana"
  version          = "8.5.1"
  namespace        = "monitoring"
  create_namespace = true

  values = [
    file("${path.module}/helm-values/grafana-values.yaml")
  ]

  set {
    name  = "service.type"
    value = "LoadBalancer"
  }

  depends_on = [aws_eks_addon.ebs_csi, helm_release.loki]
}

# 8. Karpenter IAM 및 기초 인프라 모듈
module "karpenter" {
  source = "../../../modules/karpenter"

  project_name      = var.project_name
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

# 9. Karpenter Helm 배포
resource "helm_release" "karpenter" {
  namespace        = "karpenter"
  create_namespace = true
  name             = "karpenter"
  repository       = "oci://public.ecr.aws/karpenter"
  chart            = "karpenter"
  version          = "1.0.1" 

  set {
    name  = "settings.clusterName"
    value = module.eks.cluster_name
  }

  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = module.karpenter.controller_role_arn
  }

  depends_on = [module.eks, module.karpenter]
}

# 10. gp3 StorageClass 설정
resource "kubernetes_storage_class_v1" "gp3_default" {
  metadata {
    name = "gp3"
    annotations = {
      "storageclass.kubernetes.io/is-default-class" = "true"
    }
  }

  storage_provisioner    = "ebs.csi.aws.com" 
  reclaim_policy         = "Delete"
  volume_binding_mode    = "WaitForFirstConsumer"
  allow_volume_expansion = true

  parameters = {
    type = "gp3"
  }

  depends_on = [module.eks, aws_eks_addon.ebs_csi]
}

# 11. 애플리케이션 네임스페이스
resource "kubernetes_namespace_v1" "app" {
  metadata {
    name = "${var.project_name}-${var.environment}"
  }
  depends_on = [module.eks]
}

# 12. 애플리케이션 파드용 Stunnel(Redis 프록시) ConfigMap
resource "kubernetes_config_map_v1" "redis_stunnel_conf" {
  metadata {
    name      = "redis-stunnel-conf"
    namespace = kubernetes_namespace_v1.app.metadata[0].name 
  }

  data = {
    "stunnel.conf" = replace(<<-EOF
      foreground = yes
      pid = /tmp/stunnel.pid
      delay_dns = yes

      [redis-tls]
      client = yes
      accept = 127.0.0.1:6379
      # Base의 Redis 엔드포인트 참조
      connect = ${data.terraform_remote_state.base.outputs.redis_endpoint}:6379
    EOF
    , "\r", "")
  }

  depends_on = [module.eks]
}

# 13. KEDA Helm 배포
resource "helm_release" "keda" {
  name             = "keda"
  repository       = "https://kedacore.github.io/charts"
  chart            = "keda"
  namespace        = "keda"
  version          = "2.14.0"
  create_namespace = true

  depends_on = [module.eks]
}

# 14. KEDA ScaledObject: 하이브리드 지능형 스케일링
resource "kubernetes_manifest" "on_race_tps_scaler" {
  manifest = {
    apiVersion = "keda.sh/v1alpha1"
    kind       = "ScaledObject"
    metadata = {
      name      = "on-race-api-scaler"
      namespace = kubernetes_namespace_v1.app.metadata[0].name
    }
    spec = {
      scaleTargetRef = {
        name = "on-race-api"
      }
      minReplicaCount = 2
      maxReplicaCount = 300
      
      advanced = {
        restoreToOriginalReplicaCount = true
        horizontalPodAutoscalerConfig = {
          behavior = {
            scaleDown = {
              stabilizationWindowSeconds = 300
              policies = [{ type = "Percent", value = 10, periodSeconds = 60 }]
            }
          }
        }
      }

      triggers = [
        {
          type = "cron"
          metadata = {
            timezone        = "Asia/Seoul"
            start           = "05 21 * * *" 
            end             = "10 21 * * *"
            desiredReplicas = "100"
          }
        },
        {
          type = "prometheus"
          metadata = {
            serverAddress = "http://t6-on-race-prometheus-server.monitoring.svc.cluster.local:80"
            metricName    = "onrace_tps_requests_total"
            threshold     = "50"
            query         = replace(<<-EOT
              sum(rate(onrace_tps_requests_total{result="allowed"}[1m]))
            EOT
            , "\r", "")
          }
        },
        {
          type = "aws-sqs-queue"
          metadata = {
            # Base의 SQS URL 참조
            queueURL      = data.terraform_remote_state.base.outputs.queue_url 
            awsRegion     = "ap-northeast-2"
            queueLength   = "5" 
            identityOwner = "operator" 
          }
        }
      ]
    }
  }

  depends_on = [
    time_sleep.wait_30_seconds_for_keda,
    kubernetes_namespace_v1.app,
    kubernetes_deployment_v1.on_race_api
  ]
}

# 15. Prometheus Helm 배포
resource "helm_release" "prometheus" {
  name             = "t6-on-race-prometheus"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "prometheus"
  version          = "25.21.0" 
  namespace        = "monitoring"
  create_namespace = true

  set {
    name  = "server.persistentVolume.enabled"
    value = "true" 
  }

  set {
    name  = "alertmanager.enabled"
    value = "false" 
  }

  set {
    name  = "server.persistentVolume.storageClass"
    value = "gp3"
  }

  depends_on = [module.eks]
}

# 16. KEDA 테스트용 샘플 Deployment
resource "kubernetes_deployment_v1" "on_race_api" {
  metadata {
    name      = "on-race-api" 
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }

  spec {
    replicas = 2 
    selector {
      match_labels = {
        app = "on-race-api"
      }
    }

    template {
      metadata {
        labels = {
          app = "on-race-api"
        }
      }

      spec {
        container {
          name  = "api"
          image = "nginx" 
          
          port {
            container_port = 80
          }

          resources {
            requests = {
              cpu    = "100m"   
              memory = "128Mi"
            }
            limits = {
              cpu    = "500m"
              memory = "256Mi"
            }
          }
        }
      }
    }
  }

  depends_on = [kubernetes_namespace_v1.app]
}

# 17. on-race-api 내부 통신용 서비스 (ClusterIP)
resource "kubernetes_service_v1" "on_race_api" {
  metadata {
    name      = "on-race-api"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    selector = {
      app = "on-race-api"
    }
    port {
      port        = 80
      target_port = 80
    }
    type = "ClusterIP"
  }
}

# 18. Karpenter NodePool 및 EC2NodeClass 설정
resource "kubernetes_manifest" "karpenter_node_class" {
  manifest = {
    apiVersion = "karpenter.k8s.aws/v1"
    kind       = "EC2NodeClass"
    metadata = {
      name = "default"
    }
    spec = {
      amiFamily = "AL2023" 
      
      amiSelectorTerms = [
        {
          alias = "al2023@latest" 
        }
      ]

      role = module.karpenter.node_iam_role_name 
      
      subnetSelectorTerms = [{
        tags = { "karpenter.sh/discovery" = module.eks.cluster_name }
      }]
      
      securityGroupSelectorTerms = [{
        tags = { "karpenter.sh/discovery" = module.eks.cluster_name }
      }]
    }
  }
  depends_on = [time_sleep.wait_30_seconds_for_karpenter]
}

resource "kubernetes_manifest" "karpenter_node_pool" {
  manifest = {
    apiVersion = "karpenter.sh/v1"
    kind       = "NodePool"
    metadata = {
      name = "default"
    }
    spec = {
      template = {
        spec = {
          nodeClassRef = {
            group = "karpenter.k8s.aws"
            kind  = "EC2NodeClass"
            name  = "default"
          }
          requirements = [
            { key = "karpenter.sh/capacity-type", operator = "In", values = ["spot", "on-demand"] },
            { key = "k8s.amazonaws.com/instance-category", operator = "In", values = ["c", "m", "r"] },
            { key = "kubernetes.io/arch", operator = "In", values = ["amd64"] }
          ]
        }
      }
      disruption = {
        consolidationPolicy = "WhenEmptyOrUnderutilized" 
        consolidateAfter    = "1m"
      }
    }
  }
  depends_on = [kubernetes_manifest.karpenter_node_class]
}

# 19. EKS 노드가 Redis(Base 계층)에 접근할 수 있도록 허용하는 SG 규칙
resource "aws_security_group_rule" "eks_to_redis" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  
  # 도착지: Base에서 만든 Redis 보안 그룹
  security_group_id        = data.terraform_remote_state.base.outputs.redis_security_group_id
  
  # 출발지: 방금 App에서 만든 EKS 노드 보안 그룹
  source_security_group_id = module.eks.node_security_group_id
}

# 1. AI팀 전용 S3 접근 정책 (Read/Write)
resource "aws_iam_policy" "ai_s3_access" {
  name        = "${var.project_name}-ai-s3-access"
  description = "Allow AI team to access VQA data bucket"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
        Effect   = "Allow"
        Resource = [
          "arn:aws:s3:::t6-on-race-ai-vqa-data-prod",
          "arn:aws:s3:::t6-on-race-ai-vqa-data-prod/*"
        ]
      }
    ]
  })
}

# 2. AI팀 전용 IRSA 역할 생성 (OIDC 연동)
module "ai_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "${var.project_name}-ai-irsa"
  
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${kubernetes_namespace_v1.app.metadata[0].name}:ai-service-account"]
    }
  }

  role_policy_arns = {
    s3 = aws_iam_policy.ai_s3_access.arn
  }
}

# 3. 쿠버네티스 서비스 어카운트 생성 및 역할 연결
resource "kubernetes_service_account_v1" "ai_sa" {
  metadata {
    name      = "ai-service-account"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    annotations = {
      "eks.amazonaws.com/role-arn" = module.ai_irsa.iam_role_arn
    }
  }
}