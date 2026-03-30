# 2. API 파드 전용 IAM 역할 (IRSA) 생성
module "api_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "${var.project_name}-${var.environment}-api-role"
  
  role_policy_arns = {
    # Secrets Manager 및 SQS(app-scaling.tf 정의) 접근 권한
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

# 4. Secrets Manager에서 DB 암호 가져오기 (v4 참조)
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


# 7. 메인 API Deployment
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

        container {
          name  = "api"
          image = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${data.aws_region.current.name}.amazonaws.com/t6-on-race-api:latest"
          
          # [수정] Nginx 기본 포트 80으로 변경
          port { container_port = 80 }

          # [수정] Nginx는 Java가 아니므로 JVM 옵션은 불필요 (주석 처리 또는 삭제)
          # env {
          #   name  = "JAVA_TOOL_OPTIONS"
          #   value = "-XX:InitialRAMPercentage=75.0 ..."
          # }

          # DB/SQS/Redis 환경 변수는 유지해도 Nginx가 무시하므로 무방합니다.
          env { 
            name = "DB_ENDPOINT" 
            value = data.terraform_remote_state.base.outputs.rds_proxy_endpoint 
          }
          env { 
            name = "DB_USERNAME" 
            value = local.db_creds.username 
          }
          env { 
            name = "DB_PASSWORD" 
            value = local.db_creds.password 
          }
          env { 
            name = "SQS_QUEUE_URL" 
            value = data.terraform_remote_state.base.outputs.queue_url 
          }
          env { 
            name = "SPRING_REDIS_HOST" 
            value = "127.0.0.1" 
          }
          env { 
            name = "SPRING_REDIS_PORT" 
            value = "6379" 
          }

          resources {
            requests = { cpu = "200m", memory = "512Mi" } # Nginx는 1Gi까지 필요 없으므로 하향 가능
            limits   = { cpu = "500m", memory = "1Gi" }
          }

          # [수정] 헬스 체크 경로 / 및 포트 80으로 변경
          readiness_probe {
            http_get {
              path = "/"
              port = 80
            }
            initial_delay_seconds = 5 # Nginx는 실행이 빠르므로 대기 시간 단축
            period_seconds        = 10
          }

          liveness_probe {
            http_get {
              path = "/"
              port = 80
            }
            initial_delay_seconds = 10
            period_seconds        = 15
          }

          lifecycle {
            pre_stop {
              exec { command = ["sh", "-c", "sleep 10"] }
            }
          }
        }

        # Stunnel 사이드카는 구조 유지를 위해 남겨둡니다.
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

/*
# 7. 메인 API Deployment
resource "kubernetes_deployment_v1" "on_race_api" {
  metadata {
    name      = "on-race-api"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }

  # 테라폼이 모든 파드가 Running이 될 때까지 기다리지 않고 인프라 생성을 완료합니다.
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

        container {
          name  = "api"
          image = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${data.aws_region.current.name}.amazonaws.com/t6-on-race-api:latest"
          
          port { container_port = 8080 }

          # [수정] JVM 메모리 최적화: Limit(2Gi)의 75%를 힙으로 할당
          # 나머지 25%(약 512Mi)는 Native Memory를 위해 비워두어 OOMKilled 방지
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

          # [수정] 자원 할당량 상향 조정
          resources {
            requests = { 
              cpu    = "200m"
              memory = "1Gi" 
            }
            limits   = { 
              cpu    = "1000m" 
              memory = "2Gi" 
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

        container {
          name  = "stunnel"
          image = "dweomer/stunnel:latest"
          port  { container_port = 6379 }

          # [최적화] 사이드카 리소스도 명시하여 안정성 확보
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
}*/

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
      
      # [참고] Nginx는 기본적으로 프로메테우스 메트릭을 제공하지 않으므로 
      # 테스트 중에는 아래 설정을 무시하거나 주석 처리해도 무방합니다.
      "prometheus.io/scrape" = "false" 
      "prometheus.io/path"   = "/"
      "prometheus.io/port"   = "80"
    }
  }

  spec {
    selector = { app = "on-race-api" }
    port {
      port        = 80
      # [수정] Nginx 컨테이너 포트인 80으로 변경
      target_port = 80 
      protocol    = "TCP"
    }
    type = "LoadBalancer"
  }

  wait_for_load_balancer = false 
  depends_on             = [time_sleep.wait_for_lb_controller]
}

/*
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
      
      # 프로메테우스 메트릭 수집 활성화
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
  depends_on             = [time_sleep.wait_for_lb_controller]
}*/

# 9. Redis TLS 통신을 위한 Stunnel 설정 (삭제되었다면 다시 추가)
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