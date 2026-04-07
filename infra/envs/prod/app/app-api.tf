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

        # AI 서버 기동 전까지 API 기동을 대기시키는 초기화 컨테이너
        init_container {
          name  = "wait-for-ai-macro"
          image = "public.ecr.aws/docker/library/busybox:latest"
          command = ["sh", "-c", "until nc -z ${aws_instance.ai_macro_detector[0].private_ip} 8000; do echo 'Waiting for AI Macro EC2...'; sleep 3; done"]
        }

        container {
          name  = "api"
          image = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:${var.image_tag}"
          
          image_pull_policy = "Always"
          port { container_port = 8080 }

          # [운영 설정] Spring Boot 프로파일 지정
          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }

          # [Java 21 최적화] JVM 옵션에 운영 프로파일 명시적 추가
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-Dspring.profiles.active=prod -XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }

          # [VQA] CloudFront Signed URL 관련 환경 변수
          env {
            name  = "CLOUDFRONT_KEY_ID"
            value = aws_cloudfront_public_key.vqa_key_v2.id
          }
          env {
            name  = "CLOUDFRONT_DOMAIN"
            value = "https://cdn.on-race.com"
          }
          env {
            name  = "PRIVATE_KEY_PATH"
            value = "/app/certs/vqa_private_key.pem"
          }

          # 인프라 연결 설정 (DB, SQS, Redis)
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
            value = "127.0.0.1" # Stunnel 사이드카를 통한 루프백 통신
          }
          env {
            name  = "SPRING_REDIS_PORT"
            value = "6379"
          }
          env {
            name  = "AI_MODEL_URL"
            value = "http://${aws_instance.ai_macro_detector[0].private_ip}:8000"
          }

          volume_mount {
            name       = "vqa-key-volume"
            mount_path = "/app/certs"
            read_only  = true
          }

          resources {
            requests = { cpu = "500m", memory = "1.5Gi" }
            limits   = { cpu = "1200m", memory = "2Gi" }
          }

          # [수정] Startup Probe: 포트를 8000에서 실제 앱 포트인 8080으로 교정
          startup_probe {
            tcp_socket { port = 8080 } 
            initial_delay_seconds = 10
            period_seconds        = 5
            failure_threshold     = 30 
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

        # Redis TLS 보안 통신을 위한 Stunnel 사이드카
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
          name = "vqa-key-volume"
          secret {
            secret_name = kubernetes_secret_v1.vqa_signing_key.metadata[0].name
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
      # [수정] AWS 로드밸런서 관련 모든 설정 삭제 (배포/삭제 시 지연 원인 제거)
      
      # [유지] 프로메테우스 모니터링 수집 설정
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
    # [수정] 내부 통신 전용으로 확정
    type = "ClusterIP"
  }

  # [유지] 삭제 시 'Finalizer' 고착 방지를 위한 안전장치
  provisioner "local-exec" {
    when    = destroy
    command = "kubectl patch svc ${self.metadata[0].name} -n ${self.metadata[0].namespace} -p '{\"metadata\":{\"finalizers\":null}}' --type merge || true"
  }
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