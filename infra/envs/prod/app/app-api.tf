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
    selector {
      match_labels = {
        app = "on-race-api"
      }
    }

    template {
      metadata {
        labels = {
          app = "on-race-api"
        }
      }

      spec {
        # [최적화 1] Stunnel이 준비될 때까지 API 컨테이너 실행 대기
        init_container {
          name    = "wait-for-stunnel"
          image   = "busybox:1.28"
          command = ["sh", "-c", "until nc -z localhost 6379; do echo waiting for stunnel; sleep 2; done;"]
        }

        # [최적화 2] API 서버의 안정성을 위해 On-demand 노드에 우선 배치
        affinity {
          node_affinity {
            required_during_scheduling_ignored_during_execution {
              node_selector_term {
                match_expressions {
                  key      = "karpenter.sh/capacity-type"
                  operator = "In"
                  values   = ["on-demand"]
                }
              }
            }
          }
        }

        # 메인 API 컨테이너 (Spring Boot)
        container {
          name  = "api"
          image = "nginx:alpine"
          
          # Spring Boot 3 표준 포트 (필요 시 80으로 수정)
          port {
            container_port = 8080 
          }

          # [최적화 3] 사이드카를 통한 Redis 접속 설정 주입
          env {
            name  = "SPRING_REDIS_HOST"
            value = "127.0.0.1"
          }
          env {
            name  = "SPRING_REDIS_PORT"
            value = "6379"
          }

          resources {
            requests = {
              cpu    = "200m"
              memory = "512Mi" # Spring Boot 구동을 고려해 메모리 증설
            }
            limits = {
              cpu    = "500m"
              memory = "1Gi"
            }
          }

          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 30
            period_seconds        = 10
          }
        }

        # [최적화 4] Redis TLS 암호화를 위한 Stunnel 사이드카
        container {
          name  = "stunnel"
          image = "dweomer/stunnel:latest"
          
          port {
            container_port = 6379
          }

          volume_mount {
            name       = "stunnel-conf"
            mount_path = "/etc/stunnel/stunnel.conf"
            sub_path   = "stunnel.conf"
            read_only  = true
          }

          resources {
            requests = {
              cpu    = "50m"
              memory = "64Mi"
            }
          }
        }

        volume {
          name = "stunnel-conf"
          config_map {
            name = kubernetes_config_map_v1.redis_stunnel_conf.metadata[0].name
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
      target_port = 8080 # 컨테이너 포트(8080)와 일치
      protocol    = "TCP"
    }

    # 외부 접속이 가능하도록 타입을 변경합니다.
    type = "LoadBalancer"
  }

  # 테라폼이 로드밸런서 주소 할당을 기다리지 않고 즉시 배포 성공 처리
  wait_for_load_balancer = false 
  
  # 아래 2번 항목에서 생성할 대기 리소스에 의존성 부여
  depends_on = [time_sleep.wait_for_lb_controller]
}