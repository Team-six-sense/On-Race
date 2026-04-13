# =====================================================================
# [공통 인프라] 모든 마이크로서비스(API, Auth, Gateway, Queue)가 공유
# =====================================================================

# 1. API 파드 전용 IAM 역할 (IRSA) 생성
module "api_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "${var.project_name}-${var.environment}-api-role"
  
  role_policy_arns = {
    secrets = "arn:aws:iam::aws:policy/SecretsManagerReadWrite"
    sqs     = aws_iam_policy.keda_sqs_policy.arn # SQS 권한 (KEDA 스케일링 및 메시지 처리용)
    s3      = "arn:aws:iam::aws:policy/AmazonS3FullAccess" # S3 접근 권한 부여
  }

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${kubernetes_namespace_v1.app.metadata[0].name}:on-race-api-sa"]
    }
  }
}

# 2. API 전용 Service Account 생성 (모든 서비스 파드가 이 SA를 공유하여 IAM 권한 획득)
resource "kubernetes_service_account_v1" "api_sa" {
  metadata {
    name      = "on-race-api-sa"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    annotations = {
      "eks.amazonaws.com/role-arn" = module.api_irsa.iam_role_arn
    }
  }
}

# 3. Secrets Manager에서 DB 암호 가져오기
data "aws_secretsmanager_secret" "db_secret" {
  name = "${var.project_name}-${var.environment}-db-password-v4" 
}

data "aws_secretsmanager_secret_version" "db_secret_val" {
  secret_id = data.aws_secretsmanager_secret.db_secret.id
}

locals {
  db_creds = jsondecode(data.aws_secretsmanager_secret_version.db_secret_val.secret_string)
}

# 4. 보안 그룹 규칙: EKS 노드 -> RDS Proxy / Redis 접속 허용
resource "aws_security_group_rule" "eks_to_rds_proxy" {
  type              = "ingress"
  from_port         = 3306
  to_port           = 3306
  protocol          = "tcp"
  security_group_id = data.terraform_remote_state.base.outputs.rds_proxy_sg_id
  cidr_blocks       = [data.terraform_remote_state.base.outputs.vpc_cidr]
}

resource "aws_security_group_rule" "eks_to_redis" {
  type              = "ingress"
  from_port         = 6379
  to_port           = 6379
  protocol          = "tcp"
  security_group_id = data.terraform_remote_state.base.outputs.redis_sg_id
  cidr_blocks       = [data.terraform_remote_state.base.outputs.vpc_cidr]
}

# 5. Stunnel ConfigMap (Redis TLS 통신을 위해 모든 서비스가 사이드카로 공유)
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

# =====================================================================
# [서비스 배포] Main API 전용 리소스 (Deployment, Service, PDB)
# =====================================================================
# 6. 메인 API Deployment
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

        container {
          name              = "api"
          image             = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:main-latest"
          image_pull_policy = "Always"
          
          port {
            container_port = 8080
          }

          # [API 서비스] 운영 환경(prod) 전용 통합 환경 변수
          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-Dspring.datasource.url=jdbc:mysql://${data.terraform_remote_state.base.outputs.rds_proxy_endpoint}:3306/onrace?sslMode=REQUIRED&useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true -Dspring.profiles.active=prod -XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }

          # 데이터베이스 연결 정보 (기존 로직 유지)
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

          # Redis 연결 정보 (stunnel 127.0.0.1 경유)
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
            name  = "MAIN_REDIS_HOST"
            value = "127.0.0.1"
          }
          env {
            name  = "MAIN_REDIS_PORT"
            value = "6379"
          }
          env {
            name  = "MAIN_REDIS_PASSWORD"
            value = local.db_creds.password
          }

          # AWS 및 인프라 설정 (SQS, S3)
          env {
            name  = "SQS_QUEUE_URL"
            value = data.terraform_remote_state.base.outputs.queue_url
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
            value = "900"
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

          # 외부/내부 서비스 통신 URI
          env {
            name  = "VQA_SERVICE_URL"
            value = "http://on-race-vqa-service.t6-on-race-prod.svc.cluster.local:8000"
          }
          env {
            name  = "AI_MODEL_URL"
            value = "http://ai-model.on-race.local:8000"
          }

          # CDN 및 CloudFront 설정
          env {
            name  = "CLOUDFRONT_KEY_ID"
            value = aws_cloudfront_public_key.vqa_key_v2.id
          }
          env {
            name  = "CLOUDFRONT_DOMAIN"
            value = "https://cdn.on-race.com"
          }

          volume_mount {
            name       = "vqa-key-volume"
            mount_path = "/app/certs"
            read_only  = true
          }

          resources {
            requests = { cpu = "250m", memory = "800Mi" }
            limits   = { cpu = "1200m", memory = "2Gi" }
          }

          startup_probe {
            tcp_socket { port = 8080 }
            initial_delay_seconds = 10
            period_seconds        = 5
            failure_threshold     = 60
            timeout_seconds       = 15
          }

          readiness_probe {
            tcp_socket { port = 8080 }
            initial_delay_seconds = 30
            period_seconds        = 10
            timeout_seconds       = 15
          }

          liveness_probe {
            tcp_socket { port = 8080 }
            initial_delay_seconds = 60
            period_seconds        = 15
            timeout_seconds       = 15
          }
        }

        # 5. Redis TLS 터널링용 stunnel 사이드카 (Alpine 이미지 기반 런타임 설치)
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

        # 가용성 정책 완화
        topology_spread_constraint {
          max_skew           = 1
          topology_key       = "topology.kubernetes.io/zone"
          when_unsatisfiable = "ScheduleAnyway" # DoNotSchedule에서 변경
          label_selector {
            match_labels = { app = "on-race-api" }
          }
        }

        volume {
          name = "vqa-key-volume"
          secret { secret_name = kubernetes_secret_v1.vqa_signing_key.metadata[0].name }
        }
        
        volume {
          name = "stunnel-conf"
          config_map { name = kubernetes_config_map_v1.redis_stunnel_conf.metadata[0].name }
        }
      }
    }
  }
}

# 7. 내부 통신용 서비스 (ClusterIP)
resource "kubernetes_service_v1" "on_race_api" {
  metadata {
    name      = "t6-on-race-api"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    
    annotations = {
      "prometheus.io/scrape" = "true"
      "prometheus.io/path"   = "/actuator/prometheus"
      "prometheus.io/port"   = "8080"
    }
  }

  spec {
    selector = {
      app = "on-race-api"
    }
    port {
      port        = 80
      target_port = 8080
      protocol    = "TCP"
    }
    type = "ClusterIP"
  }
}

# 8. 가용성 보장 정책 (업데이트 시 최소 1개 가동 유지)
resource "kubernetes_pod_disruption_budget_v1" "api_pdb" {
  metadata {
    name      = "on-race-api-pdb"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    min_available = 1
    selector {
      match_labels = {
        app = "on-race-api"
      }
    }
  }
}