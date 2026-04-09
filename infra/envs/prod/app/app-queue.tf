# =====================================================================
# [서비스 배포] Queue 전용 리소스 (Deployment, Service, PDB)
# =====================================================================
# 1. Queue Worker Deployment
resource "kubernetes_deployment_v1" "on_race_queue" {
  metadata {
    name      = "on-race-queue"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  wait_for_rollout = false

  spec {
    replicas = 2
    selector {
      match_labels = { app = "on-race-queue" }
    }

    template {
      metadata {
        labels = { app = "on-race-queue" }
      }

      spec {
        service_account_name = kubernetes_service_account_v1.api_sa.metadata[0].name

        container {
          name              = "queue"
          image             = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:queue-${var.image_tag}"
          image_pull_policy = "Always"
          
          port {
            container_port = 8082
          }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }
          
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-Dspring.datasource.url=jdbc:mysql://${data.terraform_remote_state.base.outputs.rds_proxy_endpoint}:3306/onrace?sslMode=REQUIRED&useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true -Dspring.profiles.active=prod -XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }

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
          env {
            name  = "SQS_QUEUE_URL"
            value = data.terraform_remote_state.base.outputs.queue_url
          }

          env {
            name  = "SPRING_REDIS_HOST"
            value = "127.0.0.1"
          }
          env {
            name  = "SPRING_REDIS_PORT"
            value = "6379"
          }

          resources {
            requests = { cpu = "250m", memory = "800Mi" }
            limits   = { cpu = "1200m", memory = "2Gi" }
          }

          startup_probe {
            tcp_socket {
              port = 8082
            }
            initial_delay_seconds = 10
            period_seconds        = 5
            failure_threshold     = 30
          }
          readiness_probe {
            http_get {
              path = "/actuator/health"
              port = 8082
            }
            initial_delay_seconds = 30
            period_seconds        = 10
          }
          liveness_probe {
            http_get {
              path = "/actuator/health/liveness"
              port = 8082
            }
            initial_delay_seconds = 60
            period_seconds        = 15
          }
        }

        container {
          name  = "stunnel"
          image = "dweomer/stunnel:latest"
          
          port {
            container_port = 6379
          }
          
          env {
            name  = "STUNNEL_SERVICE"
            value = "redis-stunnel"
          }
          env {
            name  = "STUNNEL_ACCEPT"
            value = "127.0.0.1:6379"
          }
          env {
            name  = "STUNNEL_CONNECT"
            value = "${data.terraform_remote_state.base.outputs.redis_endpoint}:6379"
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
            match_labels = { app = "on-race-queue" }
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
resource "kubernetes_service_v1" "on_race_queue" {
  metadata {
    name      = "t6-on-race-queue"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    selector = { app = "on-race-queue" }
    port {
      port        = 80
      target_port = 8082
      protocol    = "TCP"
    }
    type = "ClusterIP"
  }
}

# 3. 가용성 보장 정책
resource "kubernetes_pod_disruption_budget_v1" "queue_pdb" {
  metadata {
    name      = "on-race-queue-pdb"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    min_available = 1
    selector { match_labels = { app = "on-race-queue" } }
  }
}