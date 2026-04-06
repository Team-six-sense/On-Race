resource "helm_release" "argocd" {
  name       = "argocd"
  repository = "https://argoproj.github.io/argo-helm"
  chart      = "argo-cd"
  version    = "7.8.0" # 보안 패치가 포함된 차트 버전

  namespace        = "argocd"
  create_namespace = true

  # 보안팀 권고 이미지 태그 강제 지정
  set {
    name  = "server.image.tag"
    value = "v2.14.20"
  }
  set {
    name  = "controller.image.tag"
    value = "v2.14.20"
  }
  set {
    name  = "repoServer.image.tag"
    value = "v2.14.20"
  }

  set {
    name  = "server.service.type"
    value = "ClusterIP" 
  }
}