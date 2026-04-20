# =====================================================================
# [서비스 배포] Queue 전용 리소스 (Deployment, Service, PDB)
# =====================================================================

# 1. Queue Worker Deployment
resource "kubernetes_deployment_v1" "on_race_queue" {
  metadata {
    name      = "on-race-queue"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  wait_for_rollout = true # 안정적인 배포를 위해 true로 변경 권장

  spec {
    replicas = 1 # 최소 사양을 위해 1로 조정
    selector {
      match_labels = {
        app = "on-race-queue"
      }
    }

    template {
      metadata {
        labels = {
          app = "on-race-queue"
        }
      }

      spec {
        service_account_name = kubernetes_service_account_v1.queue_sa.metadata[0].name

        # -----------------------------------------------------------
        # 메인 컨테이너: Queue Service
        # -----------------------------------------------------------
        container {
          name              = "queue"
          image             = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:queue-${var.image_tag}"
          image_pull_policy = "Always"

          port {
            container_port = 8083
          }

          # [QUEUE 서비스] 서버/프로필 설정
          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }
          env {
            name  = "QUEUE_SERVER_PORT"
            value = "8083"
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
            name = "QUEUE_TOKEN_SECRET"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "QUEUE_TOKEN_SECRET"
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

          # Redis 연결 정보 (stunnel 경유)
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
            value = local.redis_password # Redis 전용 비밀번호 주입
          }
          env {
            name  = "QUEUE_REDIS_HOST"
            value = "127.0.0.1"
          }
          env {
            name  = "QUEUE_REDIS_PORT"
            value = "6379"
          }

          # [수정] Redis 비밀번호 및 SSL 설정 멀티라인 변경
          env {
            name  = "QUEUE_REDIS_PASSWORD"
            value = local.redis_password # Redis 전용 비밀번호 주입
          }
          env {
            name  = "QUEUE_REDIS_SSL_ENABLED"
            value = "true" # Queue Redis SSL 사용 여부 / 운영: true (ElastiCache transit_encryption_enabled=true 이므로 true로 유지)
          }

          env {
            name  = "REDISSON_CONNECTION_POOL_SIZE"
            value = "64" # Redisson 커넥션 풀 크기 / 운영: 64
          }
          env {
            name  = "REDISSON_MIN_IDLE"
            value = "32" # Redisson 최소 유휴 커넥션 수 (CONNECTION_POOL_SIZE의 절반)
          }

          # [수정] 성능 튜닝 멀티라인 변경
          env {
            name  = "QUEUE_TOMCAT_MAX_THREADS"
            value = "300"
          }
          env {
            name  = "QUEUE_TOMCAT_MIN_SPARE_THREADS"
            value = "60"
          }
          env {
            name  = "QUEUE_TOMCAT_ACCEPT_COUNT"
            value = "500"
          }
          env {
            name  = "QUEUE_TOMCAT_MAX_CONNECTIONS"
            value = "5000"
          }

          # [수정] 대기열 배치 처리 설정 멀티라인 변경
          env {
            name  = "QUEUE_BATCH_SIZE"
            value = "500"
          }
          env {
            name  = "QUEUE_INTERVAL_MS"
            value = "2000"
          }
          env {
            name  = "QUEUE_PASS_TTL_SECONDS"
            value = "900"
          }
          env {
            name  = "QUEUE_POLL_BASE_MS"
            value = "3000"
          }
          env {
            name  = "QUEUE_POLL_JITTER_MS"
            value = "2000"
          }
          env {
            name  = "QUEUE_SHUTDOWN_TIMEOUT"
            value = "30s"
          }

          resources {
            requests = {
              cpu    = "150m"    # 최소 사양
              memory = "512Mi" # 최소 사양
            }
            limits = {
              cpu    = "500m"
              memory = "1Gi"
            }
          }

          startup_probe {
            tcp_socket {
              port = 8083
            }
            initial_delay_seconds = 10
            period_seconds        = 5
            failure_threshold     = 60
            timeout_seconds       = 15
          }
          readiness_probe {
            tcp_socket {
              port = 8083
            }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 15
          }
          liveness_probe {
            tcp_socket {
              port = 8083
            }
            initial_delay_seconds = 60
            period_seconds        = 15
            timeout_seconds       = 15
          }
        }

        # -----------------------------------------------------------
        # 사이드카 컨테이너: Stunnel
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
              app = "on-race-queue"
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

# 2. 내부 통신용 서비스
resource "kubernetes_service_v1" "on_race_queue" {
  metadata {
    name      = "on-race-queue"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    selector = {
      app = "on-race-queue"
    }
    port {
      port        = 80
      target_port = 8083
      protocol    = "TCP"
    }
    type = "ClusterIP"
  }
}