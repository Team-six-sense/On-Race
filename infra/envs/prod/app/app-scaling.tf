resource "kubernetes_storage_class_v1" "gp3_default" {
  metadata {
    name = "gp3"
    annotations = {
      "storageclass.kubernetes.io/is-default-class" = "true"
    }
  }
  storage_provisioner    = "ebs.csi.aws.com"
  reclaim_policy         = "Delete"
  allow_volume_expansion = true
  volume_binding_mode    = "WaitForFirstConsumer"
  parameters = { type = "gp3" }
  depends_on = [aws_eks_addon.ebs_csi]
}

# 2. KEDA의 SQS 조회 권한 정책 (FIFO 큐 접미사 확인)
resource "aws_iam_policy" "keda_sqs_policy" {
  name = "${var.project_name}-${var.environment}-keda-sqs-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action   = ["sqs:GetQueueAttributes", "sqs:GetQueueUrl", "sqs:ReceiveMessage"] # ReceiveMessage 권한 권장
      Effect   = "Allow"
      Resource = "arn:aws:sqs:${var.aws_region}:${data.aws_caller_identity.current.account_id}:${var.project_name}-waiting-queue.fifo"
    }]
  })
}

# 3. KEDA Operator용 IRSA 생성
module "keda_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"
  
  role_name = "${var.project_name}-${var.environment}-keda-operator-role"
  
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["keda:keda-operator"] 
    }
  }
  
  role_policy_arns = { 
    sqs = aws_iam_policy.keda_sqs_policy.arn 
  }
}

# 4. KEDA Helm Release
resource "helm_release" "keda" {
  name             = "keda"
  repository       = "https://kedacore.github.io/charts"
  chart            = "keda"
  namespace        = "keda"
  version          = "2.17.0"
  create_namespace = true

  timeout          = 300

  set {
    name  = "operator.serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = module.keda_irsa.iam_role_arn
  }

  depends_on = [module.eks, module.keda_irsa]
}

resource "time_sleep" "wait_30_seconds_for_keda" {
  depends_on      = [helm_release.keda]
  create_duration = "30s"
}
/*
# 5. KEDA ScaledObject (HPA 로직)
resource "kubernetes_manifest" "on_race_tps_scaler" {
  manifest = {
    apiVersion = "keda.sh/v1alpha1"
    kind       = "ScaledObject"
    metadata = {
      name      = "on-race-api-scaler"
      namespace = kubernetes_namespace_v1.app.metadata[0].name
    }
    spec = {
      scaleTargetRef  = { name = "on-race-api" }
      minReplicaCount = 2
      maxReplicaCount = 300 # [수정] 최대 확장 한도 상향

      advanced = {
        restoreToOriginalReplicaCount = true
        horizontalPodAutoscalerConfig = {
          behavior = {
            # [최적화] 티켓팅 종료 후 트래픽 급감 시 점진적 제거 (Redis 커넥션 충격 완화)
            scaleDown = {
              stabilizationWindowSeconds = 300
              policies = [
                {
                  type          = "Percent"
                  value         = 10
                  periodSeconds = 60
                }
              ]
            }
            # [추가] 티켓팅 시작 시 폭발적 유입에 대응하기 위해 즉시 확장
            scaleUp = {
              stabilizationWindowSeconds = 0
              policies = [
                {
                  type          = "Percent"
                  value         = 100
                  periodSeconds = 15
                }
              ]
            }
          }
        }
      }

      triggers = [
        {
          type = "cron"
          metadata = {
            timezone        = "Asia/Seoul"
            # [수정] 티켓팅 1시간 전(20:10)부터 100대 확보 유지
            start           = "10 20 * * *" 
            end             = "10 21 * * *" 
            desiredReplicas = "100"
          }
        },
        {
          type = "prometheus"
          metadata = {
            serverAddress = "http://t6-on-race-prometheus-server.monitoring.svc.cluster.local:80"
            metricName    = "onrace_tps_requests_total"
            threshold     = "50" # 파드당 50 TPS 기준 (300대 시 총 35,000 TPS 수용)
            query         = "sum(rate(onrace_tps_requests_total{result=\"allowed\"}[1m]))"
          }
        },
        {
          type = "aws-sqs-queue"
          metadata = {
            queueURL      = data.terraform_remote_state.base.outputs.queue_url
            awsRegion     = var.aws_region
            queueLength   = "10" # 대기열 10개당 파드 1대 추가
            identityOwner = "operator"
          }
        }
      ]
    }
  }
  depends_on = [time_sleep.wait_30_seconds_for_keda, kubernetes_deployment_v1.on_race_api]
}
*/

# 6. Karpenter 설정 (v1.0.1 최신 사양 반영)
module "karpenter" {
  source            = "../../../modules/karpenter"
  project_name      = var.project_name
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

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
  
  # [수정] Karpenter v1.x에서는 serviceAccount 주석 경로가 중요합니다.
  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = module.karpenter.irsa_arn
  }
  
  # [추가] 컨트롤러가 사용할 인터럽트 큐 설정 (스팟 인스턴스 사용 시 필수)
  set {
    name  = "settings.interruptionQueue"
    value = module.karpenter.queue_name
  }

  depends_on = [module.eks, module.karpenter]
}

resource "time_sleep" "wait_60_seconds_for_karpenter" {
  depends_on      = [helm_release.karpenter]
  create_duration = "60s"
}

# [추가] Karpenter Helm 설치 후 CRD 및 웹훅이 준비될 때까지 대기
resource "time_sleep" "wait_60_seconds_for_karpenter" {
  depends_on = [helm_release.karpenter]
  create_duration = "60s"
}

# 7. EC2NodeClass (v1 API 반영)
resource "kubernetes_manifest" "karpenter_node_class" {
  manifest = {
    apiVersion = "karpenter.k8s.aws/v1"
    kind       = "EC2NodeClass"
    metadata = {
      name = "default"
    }
    spec = {
      amiFamily = "AL2023"
      role      = module.karpenter.node_iam_role_name

      metadataOptions = {
        httpEndpoint               = "enabled"
        httpProtocolIPv6           = "disabled"
        httpPutResponseHopLimit    = 2
        httpTokens                 = "required"
      }

      amiSelectorTerms = [
        { alias = "al2023@latest" }
      ]

      subnetSelectorTerms = [
        { tags = { "karpenter.sh/discovery" = module.eks.cluster_name } }
      ]

      securityGroupSelectorTerms = [
        { tags = { "karpenter.sh/discovery" = module.eks.cluster_name } }
      ]
      
      tags = {
        "Name"        = "${var.project_name}-karpenter-node"
        "Environment" = var.environment
      }
    }
  }
  # Karpenter 설치 후 60초 대기 후 실행
  depends_on = [time_sleep.wait_60_seconds_for_karpenter]
}

# 8. NodePool (기본형)
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
            { key = "karpenter.sh/capacity-type", operator = "In", values = ["spot"] },
            { key = "k8s.amazonaws.com/instance-category", operator = "In", values = ["c", "m", "r"] },
            { key = "k8s.amazonaws.com/instance-cpu", operator = "In", values = ["2", "4", "8"] },
            { key = "kubernetes.io/arch", operator = "In", values = ["amd64"] },
            { key = "topology.kubernetes.io/zone", operator = "In", values = ["ap-northeast-2a", "ap-northeast-2c"] }
          ]
        }
      }
      disruption = {
        consolidationPolicy = "WhenEmptyOrUnderutilized"
        consolidateAfter    = "1m"
      }
      limits = {
        cpu = "1000"
      }
    }
  }
  # NodeClass가 먼저 생성되어야 함
  depends_on = [kubernetes_manifest.karpenter_node_class]
}

# 9. VQA 전용 고성능 NodePool
resource "kubernetes_manifest" "karpenter_vqa_node_pool" {
  manifest = {
    apiVersion = "karpenter.sh/v1"
    kind       = "NodePool"
    metadata = {
      name = "vqa-compute"
    }
    spec = {
      template = {
        metadata = {
          labels = {
            "workload" = "vqa"
          }
        }
        spec = {
          nodeClassRef = {
            group = "karpenter.k8s.aws"
            kind  = "EC2NodeClass"
            name  = "default"
          }
          taints = [
            {
              key    = "workload"
              value  = "vqa"
              effect = "NoSchedule"
            }
          ]
          requirements = [
            { key = "karpenter.sh/capacity-type", operator = "In", values = ["spot"] },
            { key = "k8s.amazonaws.com/instance-family", operator = "In", values = ["c6a", "c7a"] },
            { key = "kubernetes.io/arch", operator = "In", values = ["amd64"] },
            { key = "topology.kubernetes.io/zone", operator = "In", values = ["ap-northeast-2a", "ap-northeast-2c"] }
          ]
        }
      }
      disruption = {
        consolidationPolicy = "WhenEmptyOrUnderutilized"
        consolidateAfter    = "1m"
      }
      limits = {
        cpu = "200"
      }
    }
  }
  # NodeClass가 먼저 생성되어야 함
  depends_on = [kubernetes_manifest.karpenter_node_class]
}