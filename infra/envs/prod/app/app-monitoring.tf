module "loki" {
  source            = "../../../modules/loki"
  project_name      = var.project_name
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

resource "helm_release" "loki" {
  name             = "loki"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "loki"
  namespace        = "loki"
  create_namespace = true
  values           = [file("${path.module}/helm-values/loki-values.yaml")]
  depends_on       = [module.eks, module.loki]
}

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
  depends_on = [aws_eks_addon.ebs_csi, helm_release.loki]
}