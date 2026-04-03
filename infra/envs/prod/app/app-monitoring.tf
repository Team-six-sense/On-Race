# 1. Loki 전용 IAM 역할(IRSA) 및 S3 리소스 모듈 호출
module "loki" {
  source            = "../../../modules/loki"
  project_name      = var.project_name
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
  
  timeout          = 1500

  # [핵심] templatefile을 통해 변수들을 실제 값으로 치환하여 주입
  values = [
    templatefile("${path.module}/helm-values/loki-values.yaml", {
      loki_role_arn   = module.loki.loki_role_arn
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

  timeout = 600

  # EBS CSI 드라이버와 gp3 스토리지 클래스가 먼저 준비되도록 강제
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
  
  set {
    name  = "service.type"
    value = "LoadBalancer"
  }

  # Loki와 Prometheus가 준비된 후 설치
  depends_on = [aws_eks_addon.ebs_csi, helm_release.loki, helm_release.prometheus]
}