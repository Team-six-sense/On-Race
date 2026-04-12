# =====================================================================
# [서비스 배포] Gateway 전용 리소스 (Deployment, Service, PDB)
# =====================================================================
# 1. Gateway Deployment
resource "kubernetes_deployment_v1" "on_race_gateway" {
  metadata {
    name      = "on-race-scg"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  wait_for_rollout = false

  spec {
    replicas = 2
    selector {
      match_labels = { app = "t6-on-race-scg" }
    }

    template {
      metadata {
        labels = { app = "t6-on-race-scg" }
      }

      spec {
        service_account_name = kubernetes_service_account_v1.api_sa.metadata[0].name

        container {
          name              = "gateway"
          image             = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:gateway-${var.image_tag}"
          image_pull_policy = "Always"
          
          port {
            container_port = 8080
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-Dspring.profiles.active=prod -XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }
          env {
            name  = "JWT_SECRET"
            value = "onrace-jwt-secret-key-must-be-at-least-32-bytes-long-for-hmac-sha-256-standard-2026"
          }
          env {
            name  = "GATEWAY_INTERNAL_SECRET"
            value = "on-race-internal-gateway-secret-key-2026"
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
            value = local.db_creds.password
          }
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
            value = local.db_creds.password
          }
          env {
            name  = "AUTH_SERVICE_URI"
            value = "http://t6-on-race-auth.t6-on-race-prod.svc.cluster.local:80"
          }
          env {
            name  = "MAIN_SERVICE_URI"
            value = "http://t6-on-race-api.t6-on-race-prod.svc.cluster.local:80"
          }

          resources {
            requests = { cpu = "250m", memory = "512Mi" }
            limits   = { cpu = "1000m", memory = "1Gi" }
          }

          startup_probe {
            tcp_socket {
              port = 8080
            }
            initial_delay_seconds = 10
            period_seconds        = 5
            failure_threshold     = 30
            timeout_seconds       = 5
          }
          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8080
            }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 5
          }
          liveness_probe {
            http_get {
              path = "/actuator/health/liveness"
              port = 8080
            }
            initial_delay_seconds = 60
            period_seconds        = 15
            timeout_seconds       = 5
          }
        }

        # 5. Redis TLS 터널링용 stunnel 사이드카 (Alpine 이미지 기반 런타임 설치)
        container {
          name  = "stunnel"
          image = "alpine:latest"

          # [핵심 수정] Alpine 부팅 시 stunnel 설치 후 ConfigMap 설정을 기반으로 실행
          command = ["/bin/sh", "-c", "apk add --no-cache stunnel && /usr/bin/stunnel /etc/stunnel/stunnel.conf"]
          
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

        topology_spread_constraint {
          max_skew           = 1
          topology_key       = "topology.kubernetes.io/zone"
          when_unsatisfiable = "DoNotSchedule"
          label_selector {
            match_labels = { app = "t6-on-race-scg" }
          }
        }

        volume {
          name = "stunnel-conf"
          config_map { name = kubernetes_config_map_v1.redis_stunnel_conf.metadata[0].name }
        }
      }
    }
  }
}

# 2. 내부 통신용 서비스
resource "kubernetes_service_v1" "on_race_gateway" {
  metadata {
    name      = "t6-on-race-scg"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    selector = { app = "t6-on-race-scg" }
    port {
      port        = 80
      target_port = 8080
      protocol    = "TCP"
    }
    type = "ClusterIP"
  }
}

# 3. 가용성 보장 정책
resource "kubernetes_pod_disruption_budget_v1" "gateway_pdb" {
  metadata {
    name      = "on-race-scg-pdb"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    min_available = 1
    selector { match_labels = { app = "t6-on-race-scg" } }
  }
}