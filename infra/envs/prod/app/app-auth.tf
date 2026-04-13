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
        service_account_name = kubernetes_service_account_v1.api_sa.metadata[0].name

        # -----------------------------------------------------------
        # 메인 컨테이너: Auth API
        # -----------------------------------------------------------
        container {
          name              = "auth"
          image             = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:auth-latest"
          image_pull_policy = "Always"
          
          port {
            container_port = 8081
          }

          # [AUTH 서비스] 운영 환경(prod) 전용 통합 환경 변수
          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-Dspring.datasource.url=jdbc:mysql://${data.terraform_remote_state.base.outputs.rds_proxy_endpoint}:3306/onrace?sslMode=REQUIRED&useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true -Dspring.profiles.active=prod -Dgateway.internal.secret=on-race-internal-gateway-secret-key-2026 -XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }

          # 공통 DB 연결 정보
          env {
            name  = "DB_ENDPOINT"
            value = data.terraform_remote_state.base.outputs.rds_proxy_endpoint
          }
          env {
            name  = "DB_USERNAME"
            value = local.db_creds.username
          }
          env {
            name  = "DB_PASSWORD"
            value = local.db_creds.password
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

          # AUTH 전용 Redis 연결 정보
          env {
            name  = "AUTH_REDIS_HOST"
            value = "127.0.0.1"
          }
          env {
            name  = "AUTH_REDIS_PORT"
            value = "6379"
          }
          env {
            name  = "AUTH_REDIS_PASSWORD"
            value = local.db_creds.password
          }

          # 보안 비밀키 (32바이트 이상 규격 준수)
          env {
            name  = "JWT_SECRET"
            value = "onrace-jwt-secret-key-must-be-at-least-32-bytes-long-for-hmac-sha-256-standard-2026"
          }
          env {
            name  = "GATEWAY_INTERNAL_SECRET"
            value = "on-race-internal-gateway-secret-key-2026"
          }

          # 내부 서비스 통신 URI (API 서비스 호출용)
          env {
            name  = "MAIN_SERVICE_URI"
            value = "http://t6-on-race-api.t6-on-race-prod.svc.cluster.local:80"
          }

          resources {
            requests = { cpu = "250m", memory = "800Mi" }
            limits   = { cpu = "1200m", memory = "2Gi" }
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