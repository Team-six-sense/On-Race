resource "helm_release" "keda" {
  name             = "keda"
  repository       = "https://kedacore.github.io/charts"
  chart            = "keda"
  namespace        = "keda"
  version          = "2.14.0"
  create_namespace = true
  depends_on       = [module.eks]
}

resource "time_sleep" "wait_30_seconds_for_keda" {
  depends_on      = [helm_release.keda]
  create_duration = "30s"
}

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
      maxReplicaCount = 300
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
            query         = "sum(rate(onrace_tps_requests_total{result=\"allowed\"}[1m]))"
          }
        },
        {
          type = "aws-sqs-queue"
          metadata = {
            queueURL      = data.terraform_remote_state.base.outputs.queue_url
            awsRegion     = "ap-northeast-2"
            queueLength   = "5"
            identityOwner = "operator"
          }
        }
      ]
    }
  }
  depends_on = [time_sleep.wait_30_seconds_for_keda, kubernetes_deployment_v1.on_race_api]
}

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
  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = module.karpenter.controller_role_arn
  }
  depends_on = [module.eks, module.karpenter]
}

resource "time_sleep" "wait_30_seconds_for_karpenter" {
  depends_on      = [helm_release.karpenter]
  create_duration = "60s"
}

resource "kubernetes_manifest" "karpenter_node_class" {
  manifest = {
    apiVersion = "karpenter.k8s.aws/v1"
    kind       = "EC2NodeClass"
    metadata = {
      name = "default"
    }
    spec = {
      # AL2023 기반의 최신 EKS 최적화 AMI 자동 선택
      amiFamily = "AL2023"
      role      = module.karpenter.node_iam_role_name

      # [에러 해결 핵심] amiSelectorTerms를 명시적으로 정의
      amiSelectorTerms = [
        {
          alias = "al2023@latest"
        }
      ]

      subnetSelectorTerms = [
        {
          tags = {
            "karpenter.sh/discovery" = module.eks.cluster_name
          }
        }
      ]

      securityGroupSelectorTerms = [
        {
          tags = {
            "karpenter.sh/discovery" = module.eks.cluster_name
          }
        }
      ]
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
        # 노드가 비었거나 자원이 낭비될 때 자동으로 정리하는 정책
        consolidationPolicy = "WhenEmptyOrUnderutilized"
        consolidateAfter    = "1m"
      }
    }
  }
  depends_on = [kubernetes_manifest.karpenter_node_class]
}