# =====================================================================
# [서비스 배포] Auth 전용 리소스 (Deployment, Service, PDB)
# =====================================================================
# 1. Auth Deployment
resource "kubernetes_deployment_v1" "on_race_auth" {
  metadata {
    name      = "on-race-auth"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  wait_for_rollout = false

  spec {
    replicas = 2
    selector {
      match_labels = { app = "on-race-auth" }
    }

    template {
      metadata {
        labels = { app = "on-race-auth" }
      }

      spec {
        service_account_name = kubernetes_service_account_v1.auth_sa.metadata[0].name # app-iam.tf에서 생성

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
            name  = "SPRING_JPA_HIBERNATE_DDL_AUTO" # AUTH_JPA_DDL_AUTO
            value = "validate" # [수정] 최초 배포 시 테이블 자동 생성을 위해 임시로 update 로 변경 
          }
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-Dspring.datasource.url=jdbc:mysql://${data.terraform_remote_state.base.outputs.rds_proxy_endpoint}:3306/onrace?sslMode=REQUIRED&useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true -XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }

          # 보안 비밀키 (32바이트 이상 규격 준수)
          env {
            name  = "JWT_SECRET"
            value = "onrace-jwt-secret-key-must-be-at-least-32-bytes-long-for-hmac-sha-256-standard-2026"
          }
          env {
            name  = "JWT_ACCESS_TOKEN_EXPIRATION"
            value = "1800000"
          }
          env {
            name  = "JWT_REFRESH_TOKEN_EXPIRATION"
            value = "604800000"
          }
          env {
            name  = "GATEWAY_INTERNAL_SECRET"
            value = "on-race-internal-gateway-secret-key-2026"
          }

          # 공통 DB 연결 정보
          # AUTH 전용 DB 연결 정보 (기존 로직 유지)
          env {
            name  = "AUTH_DB_NAME"
            value = "onrace"
          }
          env {
            name  = "AUTH_DB_PORT"
            value = "3306"
          }
          # AUTH 전용 DB 연결 정보 (기존 로직 유지)
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
            value = local.db_creds.password
          }

          # 공통 Redis 연결 정보
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
            value = local.db_creds.password
          }
          env {
            name  = "AUTH_REDIS_PASSWORD"
            value = local.db_creds.password
          }

          # 외부 서비스 연동 (Mails, SMS, OAuth)
          env {
            name  = "SOLAPI_API_KEY"
            value = "NCSR9BHTS0IOCWVI"
          }
          env {
            name  = "SOLAPI_API_SECRET"
            value = "DA0GOHEI2OC0RIJ3TG9WIZLGJCTLTNGF"
          }
          env {
            name  = "SOLAPI_SENDER"
            value = "01097568664"
          }
          env {
            name  = "MAIL_USERNAME"
            value = "onrace.dev@gmail.com"
          }
          env {
            name  = "MAIL_PASSWORD"
            value = "kxxaiqanmhnelzqz"
          }
          env {
            name  = "KAKAO_CLIENT_ID"
            value = "dummy-kakao-client-id" # 필요시 Secrets Manager로 교체
          }
          env {
            name  = "NAVER_CLIENT_ID"
            value = "dummy-naver-client-id" # 필요시 Secrets Manager로 교체
          }

          # 내부 서비스 통신 URI (API 서비스 호출용)
          env {
            name  = "MAIN_SERVICE_URI"
            value = "http://t6-on-race-api.t6-on-race-prod.svc.cluster.local:80"
          }

          # [추가] 프로메테우스 메트릭에 애플리케이션 식별 태그 추가
          env {
            name  = "MANAGEMENT_METRICS_TAGS_APPLICATION"
            value = "on-race-auth"
          }

          resources {
            requests = { cpu = "250m", memory = "800Mi" }
            limits   = { cpu = "1200m", memory = "2Gi" }
          }

          # 성능 튜닝
          env {
            name  = "AUTH_HIKARI_MAX_POOL_SIZE"
            value = "30"
          }
          env {
            name  = "AUTH_TOMCAT_MAX_THREADS"
            value = "250"
          }

          startup_probe {
            tcp_socket { port = 8081 }
            initial_delay_seconds = 10
            period_seconds        = 5
            failure_threshold     = 60
            timeout_seconds       = 15
          }
          readiness_probe {
            tcp_socket { port = 8081 }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 15
          }
          liveness_probe {
            tcp_socket { port = 8081 }
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
          when_unsatisfiable = "ScheduleAnyway" # 분석 결과에 따라 완화
          label_selector {
            match_labels = { app = "on-race-auth" }
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
    name      = "t6-on-race-auth"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    # Prometheus가 메트릭을 수집할 수 있도록 어노테이션을 추가합니다.
    annotations = {
      "prometheus.io/scrape" = "true"
      "prometheus.io/path"   = "/actuator/prometheus"
      "prometheus.io/port"   = "80"
    }
  }
  spec {
    selector = { app = "on-race-auth" }
    port {
      port        = 80
      target_port = 8081
      protocol    = "TCP"
    }
    type = "ClusterIP"
  }
}

# 3. 가용성 보장 정책
resource "kubernetes_pod_disruption_budget_v1" "auth_pdb" {
  metadata {
    name      = "on-race-auth-pdb"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    min_available = 1
    selector { match_labels = { app = "on-race-auth" } }
  }
}