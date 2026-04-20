# =====================================================================
# [공통 인프라] 모든 마이크로서비스(API, Auth, Gateway, Queue)가 공유
# =====================================================================

# [보안 개선] API 서비스 전용 IAM 정책 (최소 권한 원칙)

# 2. 보안 그룹 규칙: EKS 노드 -> RDS Proxy / Redis 접속 허용
resource "aws_security_group_rule" "eks_to_rds_proxy" {
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.rds_proxy_sg_id
  source_security_group_id = module.eks.nodes_security_group_id
}

resource "aws_security_group_rule" "eks_to_redis" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.redis_sg_id
  source_security_group_id = module.eks.nodes_security_group_id
}

# 3. Stunnel ConfigMap (Redis TLS 통신을 위해 모든 서비스가 사이드카로 공유)
resource "kubernetes_config_map_v1" "redis_stunnel_conf" {
  metadata {
    name      = "redis-stunnel-conf"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  data = {
    "stunnel.conf" = <<-EOF
foreground = yes
debug = info
# 컨테이너 내 권한 문제를 방지하기 위해 반드시 추가해야 합니다.
pid = /tmp/stunnel.pid

[redis-tls]
client = yes
accept = 127.0.0.1:6379
connect = ${data.terraform_remote_state.base.outputs.redis_endpoint}:6379
EOF
  }
}

# 3-1. [Security Improvement] Manage sensitive values using Kubernetes Secrets
resource "kubernetes_secret_v1" "on_race_common_secrets" {
  metadata {
    name      = "on-race-common-secrets"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  data = {
    "JWT_SECRET"                   = local.common_secrets.JWT_SECRET
    "JWT_ACCESS_TOKEN_EXPIRATION"  = local.common_secrets.JWT_ACCESS_TOKEN_EXPIRATION
    "JWT_REFRESH_TOKEN_EXPIRATION" = local.common_secrets.JWT_REFRESH_TOKEN_EXPIRATION
    "GATEWAY_INTERNAL_SECRET"      = local.common_secrets.GATEWAY_INTERNAL_SECRET
    "QUEUE_TOKEN_SECRET"           = local.common_secrets.QUEUE_TOKEN_SECRET
    "SOLAPI_API_KEY"               = local.common_secrets.SOLAPI_API_KEY
    "SOLAPI_API_SECRET"            = local.common_secrets.SOLAPI_API_SECRET
    "SOLAPI_SENDER"                = local.common_secrets.SOLAPI_SENDER
    "MAIL_USERNAME"                = local.common_secrets.MAIL_USERNAME
    "MAIL_PASSWORD"                = local.common_secrets.MAIL_PASSWORD
    "KAKAO_CLIENT_ID"              = local.common_secrets.KAKAO_CLIENT_ID
    "KAKAO_CLIENT_SECRET"          = local.common_secrets.KAKAO_CLIENT_SECRET
    "NAVER_CLIENT_ID"              = local.common_secrets.NAVER_CLIENT_ID
    "NAVER_CLIENT_SECRET"          = local.common_secrets.NAVER_CLIENT_SECRET
    "TOSS_SECRET_KEY"              = local.common_secrets.TOSS_SECRET_KEY
  }
  type = "Opaque"
}

# # =====================================================================
# # [보안] CloudFront 서명키 생성 및 관리
# # =====================================================================

# # 1. 서명에 사용할 RSA-2048 비공개 키 생성
# resource "tls_private_key" "vqa_key" {
#   algorithm = "RSA"
#   rsa_bits  = 2048
# }

# # 2. 생성된 비공개 키를 쿠버네티스 시크릿으로 저장
# # 이 시크릿은 API 파드에 마운트되어 서명에 사용됩니다.
# resource "kubernetes_secret_v1" "vqa_signing_key" {
#   metadata {
#     name      = "vqa-signing-key"
#     namespace = kubernetes_namespace_v1.app.metadata[0].name
#   }
#   data = {
#     # 애플리케이션이 /app/certs/private_key.pem 경로에서 읽도록 설정되어 있어야 합니다.
#     "private_key.pem" = tls_private_key.vqa_key.private_key_pem
#   }
# }

# # 3. 생성된 키의 공개 키 부분을 CloudFront에 업로드
# # CloudFront는 이 공개 키를 사용해 API가 생성한 서명을 검증합니다.
# resource "aws_cloudfront_public_key" "vqa_key_v2" {
#   name        = "vqa-signing-key-v2"
#   encoded_key = tls_private_key.vqa_key.public_key_pem
#   comment     = "Public key for On-Race VQA content signing"
# }

# =====================================================================
# [서비스 배포] Main API 전용 리소스 (Deployment, Service, PDB)
# =====================================================================
resource "kubernetes_deployment_v1" "on_race_api" {
  metadata {
    name      = "on-race-api"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }

  # [개선] 배포가 완전히 성공할 때까지 기다리도록 설정하여 안정성을 높입니다.
  wait_for_rollout = true
  timeouts {
    update = "10m" # 배포 업데이트 시 최대 10분까지 대기
  }

  spec {
    replicas = 1 # 최소 사양을 위해 1로 조정
    selector {
      match_labels = { app = "on-race-api" }
    }

    template {
      metadata {
        labels = { app = "on-race-api" }
      }

      spec {
        service_account_name = kubernetes_service_account_v1.api_sa.metadata[0].name

        container {
          name              = "api"
          image             = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:main-${var.image_tag}"
          image_pull_policy = "Always"

          port {
            container_port = 8080
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }
          env {
            name  = "MAIN_SERVER_PORT"
            value = "8080"
          }
          env {
            name  = "SPRING_JPA_HIBERNATE_DDL_AUTO"
            value = "validate"
          }
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }
          # [수정] SSL 연결을 위한 환경 변수 추가
          env {
            name  = "MAIN_DB_SSL_MODE"
            value = "REQUIRED"
          }

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
            name  = "MAIN_DB_NAME"
            value = "onrace"
          }
          env {
            name  = "MAIN_DB_HOST"
            value = data.terraform_remote_state.base.outputs.rds_proxy_endpoint
          }
          env {
            name  = "MAIN_DB_PORT"
            value = "3306"
          }
          env {
            name  = "MAIN_DB_USERNAME"
            value = local.db_creds.username
          }
          env {
            name  = "MAIN_DB_PASSWORD"
            value = local.db_creds.password
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

          # [수정] HikariCP 멀티라인 블록
          env {
            name  = "MAIN_HIKARI_MAX_POOL_SIZE"
            value = "50" # 풀 최대 커넥션 수 / 운영: 50
          }
          env {
            name  = "MAIN_HIKARI_MIN_IDLE"
            value = "20" # 풀 최소 유휴 커넥션 수 / 운영: 20
          }
          env {
            name  = "MAIN_HIKARI_CONNECTION_TIMEOUT"
            value = "3000" # 커넥션 획득 대기 (ms) / 운영: 3000
          }
          env {
            name  = "MAIN_HIKARI_MAX_LIFETIME"
            value = "1800000"
          }
          env {
            name  = "MAIN_HIKARI_IDLE_TIMEOUT"
            value = "600000"
          }
          env {
            name  = "MAIN_HIKARI_LEAK_DETECTION"
            value = "5000" # 커넥션 누수 감지 (ms) / 운영: 5000
          }

          # [수정] JPA 멀티라인 블록
          env {
            name  = "MAIN_HIBERNATE_FORMAT_SQL"
            value = "false" # SQL 포맷팅 출력 / 운영: false
          }
          env {
            name  = "MAIN_HIBERNATE_USE_SQL_COMMENTS"
            value = "false" # SQL 주석 출력 / 운영: false
          }

          # [수정] Tomcat 멀티라인 블록
          env {
            name  = "MAIN_TOMCAT_MAX_THREADS"
            value = "400" # 최대 요청 스레드 수 / 운영: 400
          }
          env {
            name  = "MAIN_TOMCAT_MIN_SPARE_THREADS"
            value = "80" # 최소 유휴 스레드 수 / 운영: 80
          }
          env {
            name  = "MAIN_TOMCAT_ACCEPT_COUNT"
            value = "300" # TCP 대기 큐 크기 (스레드 풀 포화 시) / 운영: 300
          }
          env {
            name  = "MAIN_TOMCAT_MAX_CONNECTIONS"
            value = "8192"
          }

          env {
            name  = "REDISSON_CONNECTION_POOL_SIZE" # Redisson 커넥션 풀 크기
            value = "64"
          }
          env {
            name  = "REDISSON_MIN_IDLE" # Redisson 최소 유휴 커넥션 수
            value = "32"
          }
          
          env {
            name = "TOSS_SECRET_KEY"
            value_from {
              secret_key_ref {
                name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
                key  = "TOSS_SECRET_KEY"
              }
            }
          }

          env {
            name  = "AWS_S3_BUCKET"
            value = data.terraform_remote_state.base.outputs.ai_vqa_bucket_name
          }
          env {
            name  = "AWS_REGION"
            value = "ap-northeast-2"
          }
          env {
            name  = "AWS_S3_PRESIGN_EXPIRE_SECONDS"
            value = "300" # S3 Presigned URL 만료 (초) / 운영: 300
          }

          env {
            name  = "AUTH_SERVICE_URI"
            value = "http://on-race-auth.t6-on-race-prod.svc.cluster.local:80"
          }
          # env {
          #   name  = "VQA_SERVICE_URL"
          #   value = "http://on-race-vqa.t6-on-race-prod.svc.cluster.local:8000"
          # }
          env {
            name  = "AI_MODEL_URL"
            value = "http://ai-model.on-race.local:8000"
          }

          # env {
          #   name  = "CLOUDFRONT_KEY_ID"
          #   value = aws_cloudfront_public_key.vqa_key_v2.id
          # }
          env {
            name  = "CLOUDFRONT_DOMAIN"
            value = "https://cdn.on-race.com"
          }

          env {
            name  = "MANAGEMENT_METRICS_TAGS_APPLICATION"
            value = "on-race-api"
          }

          # [수정] 기타 설정 멀티라인 블록
          env {
            name  = "MAIN_REDIS_SSL_ENABLED"
            value = "true" # Main Redis SSL 사용 여부 / 운영: true (ElastiCache transit_encryption_enabled=true 이므로 true로 유지)
          }
          env {
            name  = "MAIN_TRACING_ENABLED"
            value = "true"
          }
          env {
            name  = "MAIN_ENTRY_RESERVATION_TTL_SECONDS"
            value = "600" # 선점 예약 TTL (초, 운영: 600 (10분))
          }
          env {
            name  = "MAIN_EVENT_QUEUE_START_MINUTES"
            value = "60"
          }
          env {
            name  = "MAIN_EVENT_QUEUE_END_MINUTES"
            value = "60"
          }
          env {
            name  = "MAIN_SHUTDOWN_TIMEOUT"
            value = "30s"
          }

          # volume_mount {
          #   name       = "vqa-key-volume"
          #   mount_path = "/app/certs"
          #   read_only  = true
          # }

          resources {
            requests = { cpu = "150m", memory = "512Mi" } # 최소 사양 (k6 README -Xmx512m 기반)
            limits   = { cpu = "500m", memory = "1Gi" }
          }

          startup_probe {
            tcp_socket { port = 8080 }
            initial_delay_seconds = 10
            period_seconds        = 5
            failure_threshold     = 60 # 운영: 60
            timeout_seconds       = 15
          }

          readiness_probe {
            tcp_socket { port = 8080 }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 15 # 운영: 15
          }

          liveness_probe {
            tcp_socket { port = 8080 }
            initial_delay_seconds = 60
            period_seconds        = 15
            timeout_seconds       = 15 # 운영: 15
          }
        }

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

        topology_spread_constraint {
          max_skew           = 1
          topology_key       = "topology.kubernetes.io/zone"
          when_unsatisfiable = "ScheduleAnyway"
          label_selector {
            match_labels = { app = "on-race-api" }
          }
        }

        # volume {
        #   name = "vqa-key-volume"
        #   secret { secret_name = kubernetes_secret_v1.vqa_signing_key.metadata[0].name }
        # }
        volume {
          name = "stunnel-conf"
          config_map { name = kubernetes_config_map_v1.redis_stunnel_conf.metadata[0].name }
        }
        volume {
          name = "on-race-common-secrets"
          secret { secret_name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name }
        }
      }
    }
  }
}

resource "kubernetes_service_v1" "on_race_api" {
  metadata {
    name      = "on-race-api"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    annotations = {
      "prometheus.io/scrape" = "true"
      "prometheus.io/path"   = "/actuator/prometheus"
      "prometheus.io/port"   = "80"
    }
  }
  spec {
    selector = { app = "on-race-api" }
    port {
      port        = 80
      target_port = 8080
      protocol    = "TCP"
    }
    type = "ClusterIP"
  }
}