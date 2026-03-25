# 1. Loki 전용 IAM 역할(IRSA) 모듈
module "loki" {
  source            = "../../../modules/loki"
  project_name      = var.project_name
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

# 2. Loki 배포 (템플릿 파일을 사용해 IAM Role ARN을 동적 주입)
resource "helm_release" "loki" {
  name             = "loki"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "loki"
  namespace        = "loki"
  create_namespace = true

  # [핵심] templatefile을 통해 loki-values.yaml 내의 ${loki_role_arn}에 실제 값을 주입
  values = [
    templatefile("${path.module}/helm-values/loki-values.yaml", {
      loki_role_arn = module.loki.loki_role_arn
    })
  ]

  depends_on = [module.eks, module.loki]
}

# 3. Prometheus 배포 (TPS 메트릭 수집 및 KEDA 스케일링 소스)
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

# 4. Grafana 배포 (로그 및 메트릭 시각화)
resource "helm_release" "grafana" {
  name             = "grafana"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "grafana"
  version          = "8.5.1"
  namespace        = "monitoring"
  create_namespace = true
  values           = [file("${path.module}/helm-values/grafana-values.yaml")]
  
  set {
    name  = "service.type"
    value = "LoadBalancer"
  }

  # [주의] Loki가 먼저 설치되어야 Grafana 데이터 소스 연결이 실패하지 않음
  depends_on = [aws_eks_addon.ebs_csi, helm_release.loki, helm_release.prometheus]
}