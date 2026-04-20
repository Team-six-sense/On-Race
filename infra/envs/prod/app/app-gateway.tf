# =====================================================================
# [서비스 배포] Gateway 전용 리소스 (Deployment, Service, PDB)
# =====================================================================

# 1. Gateway Deployment
resource "kubernetes_deployment_v1" "on_race_gateway" {
  metadata {
    name      = "on-race-gateway"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  wait_for_rollout = true

  spec {
    replicas = 1 # 최소 사양을 위해 1로 조정
    selector {
      match_labels = {
        app = "on-race-gateway"
      }
    }

    template {
      metadata {
        labels = {
          app = "on-race-gateway"
        }
      }

      spec {
        service_account_name = kubernetes_service_account_v1.gateway_sa.metadata[0].name

        # -----------------------------------------------------------
        # 메인 컨테이너: Gateway (Spring Cloud Gateway)
        # -----------------------------------------------------------
        container {
          name              = "gateway"
          image             = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:gateway-${var.image_tag}"
          image_pull_policy = "Always"

          port {
            container_port = 8080
          }

          # [GATEWAY 서비스] 서버/프로필 설정
          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }
          env {
            name  = "GATEWAY_SERVER_PORT"
            value = "8080"
          }
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }

          # [수정] 보안 비밀키 멀티라인 변경
          env {
            name = "JWT_SECRET"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "JWT_SECRET"
              }
            }
          }
          env {
            name = "JWT_ACCESS_TOKEN_EXPIRATION"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "JWT_ACCESS_TOKEN_EXPIRATION"
              }
            }
          }
          env {
            name = "JWT_REFRESH_TOKEN_EXPIRATION"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "JWT_REFRESH_TOKEN_EXPIRATION"
              }
            }
          }
          env {
            name = "GATEWAY_INTERNAL_SECRET"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "GATEWAY_INTERNAL_SECRET"
              }
            }
          }
          env {
            name = "QUEUE_TOKEN_SECRET"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "QUEUE_TOKEN_SECRET"
              }
            }
          }

          # 공통 Redis 연결 정보 (stunnel 127.0.0.1 경유)
          env {
            name  = "SPRING_REDIS_HOST"
            value = "127.0.0.1"
          }
          env {
            name  = "SPRING_REDIS_PORT"
            value = "6379"
          }
          env {
            name  = "SPRING_REDIS_PASSWORD"
            value = local.redis_password
          }

          # GATEWAY 전용 Redis 연결 정보 (Rate Limit용)
          env {
            name  = "GATEWAY_REDIS_HOST"
            value = "127.0.0.1"
          }
          env {
            name  = "GATEWAY_REDIS_PORT"
            value = "6379"
          }
          env {
            name  = "GATEWAY_REDIS_PASSWORD"
            value = local.redis_password
          }

          # 라우팅용 내부 서비스 URI
          env {
            name  = "AUTH_SERVICE_URI"
            value = "http://on-race-auth.t6-on-race-prod.svc.cluster.local:80"
          }
          env {
            name  = "MAIN_SERVICE_URI"
            value = "http://on-race-api.t6-on-race-prod.svc.cluster.local:80"
          }
          env {
            name  = "QUEUE_SERVICE_URI"
            value = "http://on-race-queue.t6-on-race-prod.svc.cluster.local:80"
          }
          env {
            name  = "CORS_ALLOWED_ORIGIN_PATTERNS"
            value = "https://on-race.com"
          }

          # [수정] UI 및 기타 설정 멀티라인 변경
          env {
            name  = "CHALLENGE_URI"
            value = "https://on-race.com/challenge"
          }
          env {
            name  = "WAITING_ROOM_URI"
            value = "https://on-race.com/queue"
          }
          env {
            name  = "QUEUE_CACHE_POLL_INTERVAL_MS"
            value = "5000"
          }
          env {
            name  = "GATEWAY_SHUTDOWN_TIMEOUT"
            value = "30s"
          }

          resources {
            requests = {
              cpu    = "100m"    # 최소 사양
              memory = "384Mi" # 최소 사양
            }
            limits = {
              cpu    = "500m"
              memory = "768Mi"
            }
          }

          startup_probe {
            tcp_socket {
              port = 8080
            }
            initial_delay_seconds = 10
            period_seconds        = 5
            failure_threshold     = 60
            timeout_seconds       = 15
          }
          readiness_probe {
            tcp_socket {
              port = 8080
            }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 15
          }
          liveness_probe {
            tcp_socket {
              port = 8080
            }
            initial_delay_seconds = 60
            period_seconds        = 15
            timeout_seconds       = 15
          }
        }

        # -----------------------------------------------------------
        # 사이드카 컨테이너: Stunnel (Redis TLS 터널링)
        # -----------------------------------------------------------
        container {
          name  = "stunnel"
          image = "916228846377.dkr.ecr.ap-northeast-2.amazonaws.com/t6-on-race-repo:stunnel-latest"

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
            requests = { cpu = "10m", memory = "20Mi" }
            limits   = { cpu = "50m", memory = "50Mi" }
          }
        }

        # -----------------------------------------------------------
        # Pod 레벨 설정 (가용성 및 볼륨)
        # -----------------------------------------------------------
        topology_spread_constraint {
          max_skew           = 1
          topology_key       = "topology.kubernetes.io/zone"
          when_unsatisfiable = "ScheduleAnyway"
          label_selector {
            match_labels = {
              app = "on-race-gateway"
            }
          }
        }

        volume {
          name = "stunnel-conf"
          config_map {
            name = kubernetes_config_map_v1.redis_stunnel_conf.metadata[0].name
          }
        }
        volume {
          name = "on-race-common-secrets"
          secret {
            secret_name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
          }
        }
      }
    }
  }
}

# 2. 내부 통신용 서비스
resource "kubernetes_service_v1" "on_race_gateway" {
  metadata {
    name      = "on-race-gateway"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    selector = {
      app = "on-race-gateway"
    }
    port {
      port        = 80
      target_port = 8080
      protocol    = "TCP"
    }
    type = "ClusterIP"
  }
}