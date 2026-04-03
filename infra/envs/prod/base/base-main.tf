# 1. VPC 모듈 호출 (700개 이상의 파드 수용을 위한 네트워크 기초)
module "vpc" {
  source = "../../../modules/vpc"

  project_name = var.project_name
  environment  = var.environment
  vpc_cidr     = var.vpc_cidr
  azs          = var.azs

  # 700~1000개 파드 확장을 위해 /22(1,024개 IP) 이상의 대역 주입 권장
  private_subnets  = var.private_subnets
  public_subnets   = var.public_subnets
  database_subnets = var.database_subnets

  # 운영 환경 안정성을 위해 가용영역별 NAT Gateway 생성을 권장하나, 현재는 비용 절감(Single) 유지
  single_nat_gateway = var.single_nat_gateway
}

# 2. DB 암호 자동 생성 및 관리 (하드코딩 완전 제거)
resource "random_password" "db_password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# Secret Manager 설정
resource "aws_secretsmanager_secret" "db_secret" {
  name                    = "${var.project_name}-${var.environment}-db-password-v4"
  description             = "On-Race RDS Root Password Managed by Terraform"
  recovery_window_in_days = 0 

  tags = {
    Project = var.project_name
    Usage   = "Database-Credentials"
  }
}

resource "aws_secretsmanager_secret_version" "db_secret_val" {
  secret_id     = aws_secretsmanager_secret.db_secret.id
  secret_string = jsonencode({
    username = "admin"
    password = random_password.db_password.result
    engine   = "mysql"
    port     = 3306
  })
}

# 3. 데이터 계층 모듈 호출 (Secret ARN 주입 확인)
module "data" {
  source = "../../../modules/data"

  project_name      = var.project_name
  environment       = var.environment
  vpc_id            = module.vpc.vpc_id
  database_subnets  = module.vpc.database_subnets

  # 비밀번호 직접 주입과 ARN 주입을 병행하여 모듈 내부의 유연성 확보
  db_password       = random_password.db_password.result
  db_secret_arn     = aws_secretsmanager_secret.db_secret.arn

  redis_node_type            = "cache.m7g.large" 
  automatic_failover_enabled = true
  num_cache_clusters         = 2
}

# 4. SQS 대기열 모듈 호출 (KEDA 스케일링 소스)
module "queue" {
  source = "../../../modules/queue"

  project_name = var.project_name
  environment  = var.environment

  queue_name = "${var.project_name}-waiting-queue.fifo"
  fifo_queue = true

  visibility_timeout_seconds = 60
}

# 5. AI VQA 전용 S3 버킷 및 수명 주기 설정
resource "aws_s3_bucket" "ai_vqa_data" {
  bucket = "t6-on-race-ai-vqa-data-prod"

  tags = {
    Project = var.project_name
    Team    = "AI"
    Usage   = "VQA-Storage"
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "ai_vqa_lifecycle" {
  bucket = aws_s3_bucket.ai_vqa_data.id

  rule {
    id     = "auto-delete-35-days"
    status = "Enabled"
    filter {}
    expiration {
      days = 35
    }
  }
}