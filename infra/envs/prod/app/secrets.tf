resource "kubernetes_secret_v1" "vqa_signing_key" {
  metadata {
    name      = "vqa-signing-key"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  data = {
    "vqa_private_key.pem" = file("${path.module}/certs/vqa_private_key.pem")
  }
}