resource "kubernetes_config_map_v1" "redis_stunnel_conf" {
  metadata {
    name      = "redis-stunnel-conf"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  data = {
    "stunnel.conf" = <<EOF
foreground = yes
[redis-tls]
client = yes
accept = 127.0.0.1:6379
connect = ${data.terraform_remote_state.base.outputs.redis_endpoint}:6379
EOF
  }
}

resource "kubernetes_deployment_v1" "on_race_api" {
  metadata {
    name      = "on-race-api"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    replicas = 2
    selector { match_labels = { app = "on-race-api" } }
    template {
      metadata { labels = { app = "on-race-api" } }
      spec {
        container {
          name  = "api"
          image = "nginx"
          port { container_port = 80 }
          resources {
            requests = {
              cpu    = "100m"
              memory = "128Mi"
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_service_v1" "on_race_api" {
  metadata {
    name      = "t6-on-race-api"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    
    annotations = {
      # 1. AWS 로드밸런서 자체의 이름을 지정 (콘솔에서 t6-로 검색 가능)
      "service.beta.kubernetes.io/aws-load-balancer-name" = "t6-on-race-api-lb"
      
      # 2. 로드밸런서 타입 및 통신 방식 설정
      "service.beta.kubernetes.io/aws-load-balancer-type"            = "external"
      "service.beta.kubernetes.io/aws-load-balancer-nlb-target-type" = "ip"
      "service.beta.kubernetes.io/aws-load-balancer-scheme"         = "internet-facing"
      
      # 3. 타겟 그룹에 추가 태그 부여 (Project 태그 등)
      "service.beta.kubernetes.io/aws-load-balancer-additional-resource-tags" = "Project=${var.project_name},Environment=${var.environment}"
    }
  }

  spec {
    # deployment의 labels와 일치해야 합니다.
    selector = { 
      app = "on-race-api" 
    }

    port {
      port        = 80
      target_port = 80
      protocol    = "TCP"
    }

    # 외부 접속이 가능하도록 타입을 변경합니다.
    type = "LoadBalancer"
  }
}