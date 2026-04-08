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
  type              = "ingress"
  from_port         = 3306
  to_port           = 3306
  protocol          = "tcp"
  security_group_id = data.terraform_remote_state.base.outputs.rds_proxy_sg_id
  cidr_blocks       = [data.terraform_remote_state.base.outputs.vpc_cidr] # [수정] 보안 그룹 ID 대신 VPC CIDR 대역 사용
}

resource "aws_security_group_rule" "eks_to_redis" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.redis_sg_id
  source_security_group_id = module.eks.node_security_group_id
}
  
# 7. 메인 API Deployment (Java 21 최적화 및 RDS Proxy 연동)
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
          name  = "api"
          image = "${data.terraform_remote_state.base.outputs.ecr_repository_url}:${var.image_tag}"
          
          image_pull_policy = "Always"
          port { container_port = 8080 }

          env {
            name  = "SPRING_PROFILES_ACTIVE"
            value = "prod"
          }

          # [진단 반영] JVM 시스템 프로퍼티를 통한 JDBC URL 강제 주입
          env {
            name  = "JAVA_TOOL_OPTIONS"
            value = "-Dspring.datasource.url=jdbc:mysql://${data.terraform_remote_state.base.outputs.rds_proxy_endpoint}:3306/onrace?sslMode=REQUIRED&useSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true -Dspring.profiles.active=prod -XX:InitialRAMPercentage=75.0 -XX:MaxRAMPercentage=75.0 -XX:MinRAMPercentage=75.0"
          }

          # [진단 반영] YAML 요구 변수 (${DB_ENDPOINT} 등)
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

          # [진단 반영] 앱 코드/로그 요구 변수 (${MAIN_DB_HOST} 등)
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

          # [문법 수정] 세미콜론 제거 및 줄바꿈 적용
          env {
            name  = "SPRING_REDIS_HOST"
            value = "127.0.0.1"
          }
          env {
            name  = "SPRING_REDIS_PORT"
            value = "6379"
          }
          env {
            name  = "SQS_QUEUE_URL"
            value = data.terraform_remote_state.base.outputs.queue_url
          }
          env {
            name  = "AI_MODEL_URL"
            value = "http://ai-model.on-race.local:8000"
          }

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

          env {
            name  = "SPRING_DATA_REDIS_SENTINEL_MASTER"
            value = "mymaster"
          }
          env {
            name  = "SPRING_DATA_REDIS_SENTINEL_NODES"
            value = "127.0.0.1:6379"
          }

          resources {
            requests = { cpu = "250m", memory = "800Mi" } # 500m -> 250m으로 하향
            limits   = { cpu = "1200m", memory = "2Gi" }
          }

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
        }

        topology_spread_constraint {
          max_skew           = 1
          topology_key       = "topology.kubernetes.io/zone" # AZ 기준 분산
          when_unsatisfiable = "DoNotSchedule"
          label_selector {
            match_labels = { app = "on-race-api" }
          }
        }

        # Redis TLS 보안 통신 사이드카
        container {
          name  = "stunnel"
          image = "dweomer/stunnel:latest"
          port  { container_port = 6379 }
          
          # [핵심 수정 3] Stunnel 필수 환경 변수 보강
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
  /*provisioner "local-exec" {
    when    = destroy
    # Heredoc 방식을 사용하여 따옴표 꼬임을 방지합니다.
    command = <<EOT
      $patchJson = '{"metadata":{"finalizers":null}}'
      kubectl patch svc t6-on-race-api -n t6-on-race-prod -p $patchJson --type merge
    EOT

    # 윈도우 환경에서 변수 처리를 위해 PowerShell 필수 지정
    interpreter = ["PowerShell", "-Command"]
  }*/
}

# 9. Stunnel ConfigMap
resource "kubernetes_config_map_v1" "redis_stunnel_conf" {
  metadata {
    name      = "redis-stunnel-conf"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  data = {
    "stunnel.conf" = <<-EOF
foreground = yes
delay = yes
[redis-tls]
client = yes
accept = 127.0.0.1:6379
connect = ${data.terraform_remote_state.base.outputs.redis_endpoint}:6379
EOF
  }
}

# 10. API 가용성 보장 정책
resource "kubernetes_pod_disruption_budget_v1" "api_pdb" {
  metadata {
    name      = "on-race-api-pdb"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    min_available = 1  # 어떤 상황에서도 최소 파드 1개는 가동 상태를 유지함
    selector {
      match_labels = { app = "on-race-api" }
    }
  }
}