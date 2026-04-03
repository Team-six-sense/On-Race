# 2. API 파드 전용 IAM 역할 (IRSA) 생성
module "api_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "${var.project_name}-${var.environment}-api-role"
  
  role_policy_arns = {
    secrets = "arn:aws:iam::aws:policy/SecretsManagerReadWrite"
    sqs     = aws_iam_policy.keda_sqs_policy.arn 
  }

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${kubernetes_namespace_v1.app.metadata[0].name}:on-race-api-sa"]
    }
  }
}

# 3. API 전용 Service Account 생성
resource "kubernetes_service_account_v1" "api_sa" {
  metadata {
    name      = "on-race-api-sa"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    annotations = {
      "eks.amazonaws.com/role-arn" = module.api_irsa.iam_role_arn
    }
  }
}

# 4. Secrets Manager에서 DB 암호 가져오기
data "aws_secretsmanager_secret" "db_secret" {
  name = "${var.project_name}-${var.environment}-db-password-v4" 
}

data "aws_secretsmanager_secret_version" "db_secret_val" {
  secret_id = data.aws_secretsmanager_secret.db_secret.id
}

locals {
  db_creds = jsondecode(data.aws_secretsmanager_secret_version.db_secret_val.secret_string)
}

# 5. 보안 그룹 규칙: EKS 노드 -> RDS Proxy / Redis 접속 허용
resource "aws_security_group_rule" "eks_to_rds_proxy" {
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.rds_proxy_sg_id
  source_security_group_id = module.eks.node_security_group_id
}

resource "aws_security_group_rule" "eks_to_redis" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.redis_sg_id
  source_security_group_id = module.eks.node_security_group_id
}

# 7. 메인 API Deployment (Java 21 최적화 및 AI 연동)
resource "kubernetes_deployment_v1" "on_race_api" {
  metadata {
    name      = "on-race-api"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }

  wait_for_rollout = false

  spec {
    replicas = 2
    selector {
      match_labels = { app = "on-race-api" }
    }

    template {
      metadata {
        labels = { app = "on-race-api" }
      }

      spec {
        service_account_name = kubernetes_service_account_v1.api_sa.metadata[0].name

        init_container {
          name  = "wait-for-ai-macro"
          image = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:busybox"
          command = [
            "sh",
            "-c",
            # [수정] 동일 계층(app-ai-rules.tf)에 선언된 리소스를 직접 참조
            "until nc -z ${aws_instance.ai_macro_detector[0].private_ip} 8000; do echo 'Waiting for AI Macro EC2...'; sleep 3; done;"
          ]
        }

        container {
          name  = "api"
          image = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:latest"
          
          image_pull_policy = "Always"
          port { container_port = 8080 }

          # [Java 21 최적화]
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
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
          env {
            name  = "AI_MODEL_URL"
            value = "http://${aws_instance.ai_macro_detector[0].private_ip}:8000"
          }

          # [Startup Probe] AI EC2 연결 및 SG 규칙 전파 대기
          startup_probe {
            tcp_socket {
              port = 8000
            }
            initial_delay_seconds = 5
            period_seconds        = 5
            failure_threshold     = 60 
          }

          resources {
            requests = { cpu = "500m", memory = "1.5Gi" }
            limits   = { cpu = "1200m", memory = "2Gi" }
          }

          readiness_probe {
            http_get { 
              path = "/actuator/health"
              port = 8080 
            }
            initial_delay_seconds = 30
            period_seconds        = 10
          }

          liveness_probe {
            http_get { 
              path = "/actuator/health/liveness"
              port = 8080 
            }
            initial_delay_seconds = 60
            period_seconds        = 15
          }

          lifecycle {
            pre_stop {
              exec { command = ["sh", "-c", "sleep 10"] }
            }
          }
        }

        # Stunnel 사이드카 (Redis TLS)
        container {
          name  = "stunnel"
          image = "dweomer/stunnel:latest"
          port  { container_port = 6379 }

          env {
            name  = "STUNNEL_SERVICE"
            value = "redis-tls"
          }
          env {
            name  = "STUNNEL_ACCEPT"
            value = "127.0.0.1:6379"
          }
          env {
            name  = "STUNNEL_CONNECT"
            value = "${data.terraform_remote_state.base.outputs.redis_endpoint}:6379"
          }

          resources {
            requests = { cpu = "50m", memory = "64Mi" }
            limits   = { cpu = "100m", memory = "128Mi" }
          }
          volume_mount {
            name       = "stunnel-conf"
            mount_path = "/etc/stunnel/stunnel.conf"
            sub_path   = "stunnel.conf"
            read_only  = true
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

# 8. 로드밸런서 서비스
resource "kubernetes_service_v1" "on_race_api" {
  metadata {
    name      = "t6-on-race-api"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    
    annotations = {
      "service.beta.kubernetes.io/aws-load-balancer-name"            = "t6-on-race-api-lb"
      "service.beta.kubernetes.io/aws-load-balancer-type"            = "external"
      "service.beta.kubernetes.io/aws-load-balancer-nlb-target-type" = "ip"
      "service.beta.kubernetes.io/aws-load-balancer-scheme"          = "internet-facing"
      "service.beta.kubernetes.io/aws-load-balancer-additional-resource-tags" = "Project=${var.project_name},Environment=${var.environment}"
      "prometheus.io/scrape" = "true"
      "prometheus.io/path"   = "/actuator/prometheus"
      "prometheus.io/port"   = "8080"
    }
  }

  spec {
    selector = { app = "on-race-api" }
    port {
      port        = 80
      target_port = 8080
      protocol    = "TCP"
    }
    type = "LoadBalancer"
  }

  wait_for_load_balancer = false 
  depends_on              = [time_sleep.wait_for_lb_controller]
}

# 9. Stunnel ConfigMap
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