# 1. VPC 모듈 호출
module "vpc" {
  source = "../../modules/vpc"

  project_name = var.project_name
  environment  = var.environment
  vpc_cidr     = var.vpc_cidr
  azs          = var.azs
  
  private_subnets  = var.private_subnets
  public_subnets   = var.public_subnets
  database_subnets = var.database_subnets
  
  single_nat_gateway = true
}

# 2. EKS 모듈 호출
module "eks" {
  source = "../../modules/eks"

  project_name = var.project_name
  environment  = var.environment
  cluster_name = "${var.project_name}-${var.environment}-cluster"
  
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnets
  
  instance_types = ["m5.large"]
  min_size       = 2
  max_size       = 15  
  
  depends_on = [module.vpc] 
}

# 3. 데이터 계층 모듈 호출 (Redis)
module "data" {
  source = "../../modules/data"

  project_name     = var.project_name
  environment      = var.environment
  vpc_id           = module.vpc.vpc_id
  database_subnets = module.vpc.database_subnets
  
  redis_node_type  = "cache.m5.large" 
  
  automatic_failover_enabled = true
  num_cache_clusters         = 2

  # EKS 모듈에서 생성된 노드 보안 그룹 ID를 Redis 모듈로 전달
  eks_node_security_group_id = module.eks.node_security_group_id
}

# 4. SQS 대기열 모듈 호출 
module "queue" {
  source = "../../modules/queue"

  project_name = var.project_name
  environment  = var.environment
  
  queue_name = "${var.project_name}-waiting-queue.fifo"
  fifo_queue = true
  
  visibility_timeout_seconds = 60
}

# 5. Loki(로키) 엔진 인프라 호출
module "loki" {
  source            = "../../modules/loki"
  project_name      = var.project_name
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

# 6. EBS CSI 드라이버 및 스토리지 권한 설정 (IaC 통합)
resource "aws_eks_addon" "ebs_csi" {
  # 하드코딩 대신 EKS 모듈의 출력값을 참조합니다.
  cluster_name = module.eks.cluster_name
  addon_name   = "aws-ebs-csi-driver"
  
  # 수동 설치된 리소스와 충돌 시 테라폼 설정을 우선합니다.
  resolve_conflicts_on_update = "OVERWRITE"

  depends_on = [module.eks]
}

resource "aws_iam_role_policy_attachment" "node_ebs_policy" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
  role       = module.eks.node_iam_role_name # EKS 모듈 출력값으로 대체
  depends_on = [module.eks]
}

# 7. Helm 프로바이더 설정 (EKS 클러스터 인증 연동)
/*data "aws_eks_cluster" "cluster" {
  name = module.eks.cluster_name
}*/

# Kubernetes 프로바이더: StorageClass 등 K8s 리소스 제어용
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

# 8. Loki(로키) Helm 배포 (S3/IRSA 연동)
resource "helm_release" "loki" {
  name             = "loki"
  
  # [수정] 인터넷 저장소가 아닌 로컬 폴더 경로를 직접 바라봅니다.
  # 현재 main.tf가 있는 prod 폴더 기준의 상대 경로입니다.
  chart            = "./helm-charts/loki-stack"

  # [삭제/주석] 로컬 차트를 쓸 때는 repository와 version을 명시하지 않습니다.
  # (로컬 폴더 안의 Chart.yaml에 정의된 정보를 따르기 때문입니다.)
  # repository       = "https://grafana.github.io/helm-charts"
  # version          = "6.6.2"

  namespace        = "loki"
  create_namespace = true

  # 앞서 작성한 values.yaml 경로 참조
  values = [
    file("${path.module}/helm-values/loki-values.yaml")
  ]

  # EKS 노드가 준비되고, Loki용 인프라(S3/IAM 등)가 준비된 후 배포되도록 보장
  depends_on = [module.eks, module.loki] 
}

# 9. Grafana(그라파나) Helm 배포 (EBS PVC 연동)
resource "helm_release" "grafana" {
  name             = "grafana"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "grafana"
  version = "8.5.1"
  namespace        = "monitoring"
  create_namespace = true

  values = [
    file("${path.module}/helm-values/grafana-values.yaml")
  ]

  # AWS LoadBalancer를 생성합니다.
  set {
    name  = "service.type"
    value = "LoadBalancer"
  }

  # EBS CSI 드라이버와 Loki가 준비된 후 배포
  depends_on = [aws_eks_addon.ebs_csi, helm_release.loki]
}

# 10. Karpenter IAM 및 기초 인프라 모듈 호출
module "karpenter" {
  source = "../../modules/karpenter"

  project_name      = var.project_name
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

# 11. Karpenter Helm 배포
resource "helm_release" "karpenter" {
  namespace        = "karpenter"
  create_namespace = true
  name             = "karpenter"
  repository       = "oci://public.ecr.aws/karpenter"
  chart            = "karpenter"
  version          = "1.0.1" # EKS 1.30 환경에 맞춘 최신 안정화 버전

  set {
    name  = "settings.clusterName"
    value = module.eks.cluster_name
  }

  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = module.karpenter.controller_role_arn
  }

  # IAM 권한(module.karpenter)이 먼저 생성된 후 Helm이 배포되도록 순서 보장
  depends_on = [module.eks, module.karpenter]
}

# 12. gp3 StorageClass를 기본값으로 생성
resource "kubernetes_storage_class_v1" "gp3_default" {
  metadata {
    name = "gp3"
    annotations = {
      # 이 옵션이 핵심입니다. 모든 PVC가 기본적으로 이 규격을 쓰게 합니다.
      "storageclass.kubernetes.io/is-default-class" = "true"
    }
  }

  storage_provisioner    = "ebs.csi.aws.com" # 최신 EBS CSI 드라이버 사용
  reclaim_policy         = "Delete"
  volume_binding_mode    = "WaitForFirstConsumer"
  allow_volume_expansion = true

  parameters = {
    type = "gp3"
  }

  # EKS와 EBS CSI 드라이버가 먼저 준비되어야 합니다.
  depends_on = [module.eks, aws_eks_addon.ebs_csi]
}

# 13. Promtail(로그 수집기) Helm 배포
resource "helm_release" "promtail" {
  name       = "promtail"
  repository = "https://grafana.github.io/helm-charts"
  chart      = "promtail"
  version = "6.16.6"
  namespace  = "loki" # Loki와 같은 네임스페이스에 두는 것이 관리하기 편합니다.

  set {
    name  = "config.clients[0].url"
    # Loki 서비스의 DNS 주소를 정확히 지정합니다.
    value = "http://loki.loki.svc.cluster.local:3100/loki/api/v1/push"
  }

  # Loki가 먼저 떠 있어야 로그를 받을 수 있습니다.
  depends_on = [helm_release.loki]
}

# 13.5 애플리케이션 전용 네임스페이스 사전 생성
resource "kubernetes_namespace_v1" "app" {
  metadata {
    # 예: on-race-prod 형태의 네임스페이스 생성
    name = "${var.project_name}-${var.environment}"
  }
  
  # EKS 클러스터가 뜬 이후에 생성되어야 함
  depends_on = [module.eks]
}

# 14. 애플리케이션 파드용 Stunnel(Redis 프록시) ConfigMap
resource "kubernetes_config_map_v1" "redis_stunnel_conf" {
  metadata {
    name      = "redis-stunnel-conf"
    namespace = kubernetes_namespace_v1.app.metadata[0].name # 하드코딩된 "default"를 지우고, 위에서 만든 네임스페이스를 동적 참조
  }

  data = {
    "stunnel.conf" = replace(<<-EOF
      foreground = yes
      pid = /tmp/stunnel.pid
      delay_dns = yes

      [redis-tls]
      client = yes
      accept = 127.0.0.1:6379
      connect = ${module.data.redis_endpoint}:6379
    EOF
    , "\r", "")
  }

  depends_on = [module.eks, module.data]
}


# 15. KEDA(Kubernetes Event-driven Autoscaling) Helm 배포
# 지능형 스케일링을 위한 컨트롤러와 CRD를 설치합니다.
resource "helm_release" "keda" {
  name             = "keda"
  repository       = "https://kedacore.github.io/charts"
  chart            = "keda"
  namespace        = "keda"
  version = "2.14.0"
  create_namespace = true

  # EKS 클러스터가 준비된 후 설치 진행
  depends_on = [module.eks]
}


# 16. KEDA ScaledObject: Prometheus 메트릭 기반 지능형 스케일링
# 16. KEDA ScaledObject: 하이브리드 지능형 스케일링 (Cron + Prometheus + SQS)
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
        # [a] 예약형 스케일링: 티켓팅 오픈 전 100대 예열
        {
          type = "cron"
          metadata = {
            timezone        = "Asia/Seoul"
            start           = "05 21 * * *" 
            end             = "10 21 * * *"
            desiredReplicas = "100"
          }
        },

        # [b] 실시간 유입 기반: Prometheus TPS
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
        }, # <--- 콤마 추가됨 (해결 완료!)

        # [c] 대기열 적체 기반: AWS SQS
        {
          type = "aws-sqs-queue"
          metadata = {
            # [수정] var.sqs_url 대신 모듈의 출력값을 직접 참조하여 안정성 확보
            queueURL      = module.queue.queue_url 
            awsRegion     = "ap-northeast-2"
            queueLength   = "5" 
            identityOwner = "operator" 
          }
        }
      ]
    }
  }

  # 의존성 관리: KEDA, 네임스페이스, 디플로이먼트가 모두 준비된 후 실행
  depends_on = [
    helm_release.keda, 
    kubernetes_namespace_v1.app,
    kubernetes_deployment_v1.on_race_api
  ]
}

# 17. Prometheus Helm 배포 (KEDA 메트릭 공급용)
resource "helm_release" "prometheus" {
  name             = "t6-on-race-prometheus"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "prometheus"
  version          = "25.21.0" # EKS 1.30 호환 안정 버전
  namespace        = "monitoring"
  create_namespace = true

  # KEDA가 찾고 있는 서비스 이름(prometheus-server)을 유지하기 위한 기본 설정

  set {
    name  = "server.persistentVolume.enabled"
    value = "true" # 테스트 환경이므로 우선 저장소 비활성화 (필요시 true로 변경)
  }

  set {
    name  = "alertmanager.enabled"
    value = "false" # 불필요한 리소스 방지
  }

  # gp3 스토리지를 쓰도록 추가 설정 (12번 항목의 gp3_default 참조)
  set {
    name  = "server.persistentVolume.storageClass"
    value = "gp3"
  }

  # EKS 클러스터가 준비된 후 설치
  depends_on = [module.eks]
}

# 18. KEDA 테스트용 샘플 Deployment
resource "kubernetes_deployment_v1" "on_race_api" {
  metadata {
    name      = "on-race-api" # ScaledObject의 scaleTargetRef와 일치 필수
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }

  spec {
    replicas = 2 # 초기 파드 개수
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

          # CPU 스케일링 계산을 위한 자원 할당량 정의
          resources {
            requests = {
              cpu    = "100m"   # 0.1 코어 (스케일링 계산의 100% 기준점)
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

# 19. on-race-api 내부 통신용 서비스 (ClusterIP)
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

# 20. Karpenter NodePool 및 EC2NodeClass 설정
resource "kubernetes_manifest" "karpenter_node_class" {
  manifest = {
    apiVersion = "karpenter.k8s.aws/v1"
    kind       = "EC2NodeClass"
    metadata = {
      name = "default"
    }
    spec = {
      amiFamily = "AL2023" # Amazon Linux 2023 사용
      
      # 어떤 AMI를 쓸지 정의합니다. 
      # alias를 사용하면 최신 EKS 최적화 이미지를 자동으로 찾아줍니다.
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
  depends_on = [helm_release.karpenter]
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
        # [수정] v1 표준 명칭으로 변경
        consolidationPolicy = "WhenEmptyOrUnderutilized" 
        # [추가] 필수 항목: 노드 정리를 위해 대기할 시간 (보통 1분을 권장합니다)
        consolidateAfter    = "1m"
      }
    }
  }
  depends_on = [kubernetes_manifest.karpenter_node_class]
}