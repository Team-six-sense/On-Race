# 1. Loki 전용 IAM 역할(IRSA) 및 S3 리소스 모듈 호출
module "loki" {
  source            = "../../../modules/loki"
  project_name      = var.project_name
  environment  = var.environment
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

# 2. Loki 배포 (템플릿 파일을 사용해 ARN 및 버킷명 동적 주입)
resource "helm_release" "loki" {
  name             = "loki"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "loki"
  namespace        = "loki"
  create_namespace = true
  
  # [유지] 무한 대기 방지 옵션 (잘 설정됨)
  wait            = false
  cleanup_on_fail = true
  timeout         = 300

  values = [
    templatefile("${path.module}/helm-values/loki-values.yaml", {
      loki_role_arn    = module.loki.loki_role_arn
      loki_bucket_name = module.loki.loki_bucket_name
    })
  ]

  depends_on = [module.eks, module.loki]
}

# 3. Prometheus 배포 (TPS 메트릭 수집 및 KEDA 스케일링 소스)
resource "helm_release" "prometheus" {
  name             = "t6-on-race-prometheus"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "prometheus"
  version          = "26.0.0"
  namespace        = "monitoring"
  create_namespace = true

  # [추가] Prometheus 무한 대기 방지
  wait            = false
  cleanup_on_fail = true
  timeout         = 600

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

  depends_on = [
    module.eks,
    aws_eks_addon.ebs_csi,
    kubernetes_storage_class_v1.gp3_default
  ]
}

# 4. Grafana 배포 (로그 및 메트릭 시각화)
resource "helm_release" "grafana" {
  name             = "grafana"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "grafana"
  version          = "8.8.0"
  namespace        = "monitoring"
  create_namespace = true
  values           = [file("${path.module}/helm-values/grafana-values.yaml")]
  
  # [추가] Grafana 무한 대기 방지
  wait            = false
  cleanup_on_fail = true
  timeout         = 300

  set {
    name  = "service.type"
    value = "ClusterIP" # [수정] LoadBalancer -> ClusterIP (삭제 지연 방지 및 보안)
  }

  depends_on = [aws_eks_addon.ebs_csi, helm_release.loki, helm_release.prometheus]
}

# 5. Promtail 배포 (Loki로 로그를 전송하는 에이전트)
resource "helm_release" "promtail" {
  name             = "promtail"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "promtail"
  namespace        = "monitoring"
  create_namespace = true

  values = [
    file("${path.module}/helm-values/promtail-config-snippet.yaml")
  ]
  
  # DaemonSet으로 동작하며 모든 노드의 로그를 수집하여 Loki Gateway로 쏩니다.
  set {
    name  = "config.clients[0].url"
    value = "http://loki-gateway.loki.svc.cluster.local/loki/api/v1/push"
  }

  depends_on = [helm_release.loki]
}