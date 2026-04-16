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
  parameters = {
    type = "gp3"
  }
  depends_on = [aws_eks_addon.ebs_csi]
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

# 5. KEDA ScaledObject (Prometheus 기반 API 스케일러)
# [해결] kubernetes_manifest 대신 kubectl_manifest를 사용합니다.
resource "kubectl_manifest" "on_race_tps_scaler" {
  yaml_body = <<-YAML
apiVersion: keda.sh/v1alpha1
kind: ScaledObject
metadata:
  name: on-race-api-scaler
  # [수정] 하드코딩된 네임스페이스 대신 동적 참조를 사용합니다.
  namespace: ${kubernetes_namespace_v1.app.metadata[0].name}
spec:
  scaleTargetRef:
    name: on-race-api
  minReplicaCount: 2
  maxReplicaCount: 300
  triggers:
    - type: prometheus
      metadata:
        serverAddress: http://t6-on-race-prometheus-server.monitoring.svc.cluster.local:80
        metricName: onrace_tps_requests_total
        threshold: "50"
        query: 'sum(rate(onrace_tps_requests_total{result="allowed"}[1m]))'
  YAML

  # module.eks 대신, 클러스터 생성 후 가장 먼저 생성되는 k8s 리소스인 네임스페이스에 의존합니다.
  depends_on = [kubernetes_namespace_v1.app, time_sleep.wait_30_seconds_for_keda, kubernetes_deployment_v1.on_race_api]
}

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