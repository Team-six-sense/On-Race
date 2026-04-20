# =====================================================================
# [서비스 배포] Auth 전용 리소스 (Deployment, Service, PDB)
# =====================================================================

# 1. Auth Deployment
resource "kubernetes_deployment_v1" "on_race_auth" {
  metadata {
    name      = "on-race-auth"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  wait_for_rollout = true # 안정적인 배포를 위해 true로 변경 권장

  spec {
    replicas = 1 # 최소 사양을 위해 1로 조정
    selector {
      match_labels = {
        app = "on-race-auth"
      }
    }

    template {
      metadata {
        labels = {
          app = "on-race-auth"
        }
      }

      spec {
        service_account_name = kubernetes_service_account_v1.auth_sa.metadata[0].name

        # -----------------------------------------------------------
        # 메인 컨테이너: Auth API
        # -----------------------------------------------------------
        container {
          name              = "auth"
          image             = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:auth-${var.image_tag}"
          image_pull_policy = "Always"

          port {
            container_port = 8081
          }

          # [AUTH 서비스] 서버/프로필/JPA 설정
          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }
          env {
            name  = "AUTH_SERVER_PORT"
            value = "8081"
          }
          env {
            name  = "SPRING_JPA_HIBERNATE_DDL_AUTO"
            value = "validate"
          }
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }
          # [버그 수정] SSL 연결을 위한 환경 변수 추가
          env {
            name  = "AUTH_DB_SSL_MODE"
            value = "REQUIRED"
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

          # AUTH 전용 DB 연결 정보
          env {
            name  = "AUTH_DB_NAME"
            value = "onrace"
          }
          env {
            name  = "AUTH_DB_PORT"
            value = "3306"
          }
          env {
            name  = "AUTH_DB_HOST"
            value = data.terraform_remote_state.base.outputs.rds_proxy_endpoint
          }
          env {
            name  = "AUTH_DB_USERNAME"
            value = local.db_creds.username
          }
          env {
            name  = "AUTH_DB_PASSWORD"
            value = local.db_creds.password # DB 비밀번호 주입
          }

          # [수정] HikariCP 설정 멀티라인 변경
          env {
            name  = "AUTH_HIKARI_MAX_POOL_SIZE"
            value = "30" # 풀 최대 커넥션 수 / 운영: 30
          }
          env {
            name  = "AUTH_HIKARI_MIN_IDLE"
            value = "10" # 풀 최소 유휴 커넥션 수 / 운영: 10
          }
          env {
            name  = "AUTH_HIKARI_CONNECTION_TIMEOUT"
            value = "3000" # 커넥션 획득 대기 (ms) / 운영: 3000
          }
          env {
            name  = "AUTH_HIKARI_MAX_LIFETIME"
            value = "1800000"
          }
          env {
            name  = "AUTH_HIKARI_IDLE_TIMEOUT"
            value = "600000"
          }

          # [수정] JPA / Hibernate 설정 멀티라인 변경
          env {
            name  = "AUTH_JPA_DDL_AUTO"
            value = "validate" # JPA DDL 전략 / 운영: validate
          }
          env {
            name  = "AUTH_HIBERNATE_FORMAT_SQL"
            value = "false" # SQL 포맷팅 출력 / 운영: false
          }
          env {
            name  = "AUTH_HIBERNATE_USE_SQL_COMMENTS"
            value = "false" # SQL 주석 출력 / 운영: false
          }

          # [수정] Tomcat 설정 멀티라인 변경
          env {
            name  = "AUTH_TOMCAT_MAX_THREADS"
            value = "250" # 최대 요청 스레드 수 / 운영: 250
          }
          env {
            name  = "AUTH_TOMCAT_MIN_SPARE_THREADS"
            value = "50" # 최소 유휴 스레드 수 / 운영: 50
          }
          env {
            name  = "AUTH_TOMCAT_ACCEPT_COUNT"
            value = "150" # TCP 대기 큐 크기 (스레드 풀 포화 시) / 운영: 150
          }

          env {
            name  = "REDISSON_CONNECTION_POOL_SIZE" # Redisson 커넥션 풀 크기
            value = "64"
          }
          env {
            name  = "REDISSON_MIN_IDLE" # Redisson 최소 유휴 커넥션 수
            value = "32"
          }
          
          # Redis 설정 멀티라인 변경
          env {
            name  = "AUTH_REDIS_SSL_ENABLED"
            value = "true" # Auth Redis SSL 사용 여부 / 운영: true (ElastiCache transit_encryption_enabled=true 이므로 true로 유지)
          }
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

          # [수정] 외부 서비스 연동 (Mails, SMS, OAuth) 멀티라인 변경
          env {
            name = "SOLAPI_API_KEY"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "SOLAPI_API_KEY"
              }
            }
          }
          env {
            name = "SOLAPI_API_SECRET"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "SOLAPI_API_SECRET"
              }
            }
          }
          env {
            name = "SOLAPI_SENDER"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "SOLAPI_SENDER"
              }
            }
          }
          env {
            name = "MAIL_USERNAME"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "MAIL_USERNAME"
              }
            }
          }
          env {
            name = "MAIL_PASSWORD"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "MAIL_PASSWORD"
              }
            }
          }
          env {
            name = "KAKAO_CLIENT_ID"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "KAKAO_CLIENT_ID"
              }
            }
          }
          env {
            name = "KAKAO_CLIENT_SECRET"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "KAKAO_CLIENT_SECRET"
              }
            }
          }
          env {
            name = "NAVER_CLIENT_ID"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "NAVER_CLIENT_ID"
              }
            }
          }
          env {
            name = "NAVER_CLIENT_SECRET"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "NAVER_CLIENT_SECRET"
              }
            }
          }

          env {
            name  = "MAIL_HOST"
            value = "smtp.gmail.com"
          }
          env {
            name  = "MAIL_PORT"
            value = "587"
          }

          env {
            name  = "MAIN_SERVICE_URI"
            value = "http://on-race-api.t6-on-race-prod.svc.cluster.local:80"
          }

          env {
            name  = "MANAGEMENT_METRICS_TAGS_APPLICATION"
            value = "on-race-auth"
          }

          env {
            name  = "PASSWORD_RESET_BASE_URL"
            value = "https://on-race.com/password/reset"
          }
          env {
            name  = "AUTH_SHUTDOWN_TIMEOUT"
            value = "30s"
          }

          resources {
            requests = {
              cpu    = "150m"    # 최소 사양
              memory = "384Mi" # 최소 사양 (k6 README -Xmx256m 기반)
            }
            limits = {
              cpu    = "500m"
              memory = "768Mi"
            }
          }

          startup_probe {
            tcp_socket {
              port = 8081
            }
            initial_delay_seconds = 10
            period_seconds        = 5
            failure_threshold     = 60
            timeout_seconds       = 15
          }
          readiness_probe {
            tcp_socket {
              port = 8081
            }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 15
          }
          liveness_probe {
            tcp_socket {
              port = 8081
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
              app = "on-race-auth"
            }
          }
        }

        volume {
          name = "on-race-common-secrets"
          secret {
            secret_name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
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
resource "kubernetes_service_v1" "on_race_auth" {
  metadata {
    name      = "on-race-auth"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    annotations = {
      "prometheus.io/scrape" = "true"
      "prometheus.io/path"   = "/actuator/prometheus"
      "prometheus.io/port"   = "80"
    }
  }
  spec {
    selector = {
      app = "on-race-auth"
    }
    port {
      port        = 80
      target_port = 8081
      protocol    = "TCP"
    }
    type = "ClusterIP"
  }
}