# 1. VPC 모듈 호출
module "vpc" {
  source = "../../../modules/vpc"

  project_name = var.project_name
  environment  = var.environment
  vpc_cidr     = var.vpc_cidr
  azs          = var.azs

  private_subnets  = var.private_subnets
  public_subnets   = var.public_subnets
  database_subnets = var.database_subnets

  single_nat_gateway = true
}

# 1. 무작위 보안 암호 생성 (16자, 특수문자 포함)
resource "random_password" "db_password" {
  length           = 16
  special          = true
  # DB 연결 시 이슈가 될 수 있는 일부 특수문자 제외 가능
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# 2. Secrets Manager 시크릿 생성
resource "aws_secretsmanager_secret" "db_secret" {
  name        = "${var.project_name}-${var.environment}-db-password-v3"
  description = "On-Race RDS Root Password Managed by Terraform"
  
  # 테스트 환경이므로 삭제 시 대기 기간 없이 즉시 삭제 허용 (운영 시 주의)
  recovery_window_in_days = 0 

  tags = {
    Project = var.project_name
    Usage   = "Database-Credentials"
  }
}

# 3. 생성된 암호를 JSON 형태로 시크릿에 저장
resource "aws_secretsmanager_secret_version" "db_secret_val" {
  secret_id     = aws_secretsmanager_secret.db_secret.id
  secret_string = jsonencode({
    username = "admin"
    password = random_password.db_password.result
    engine   = "mysql"
    port     = 3306
  })
}

# 4. [수정] 데이터 계층 모듈 호출 시 동적 암호 주입
# [통합] 데이터 계층 모듈 호출
module "data" {
  source = "../../../modules/data"

  project_name      = var.project_name
  environment       = var.environment
  vpc_id            = module.vpc.vpc_id
  database_subnets  = module.vpc.database_subnets

  # Secrets Manager에서 생성된 암호 주입
  db_password       = random_password.db_password.result
  
  # RDS Proxy가 참조할 시크릿 ARN 전달 (이게 있어야 Proxy가 DB에 접속함)
  db_secret_arn     = aws_secretsmanager_secret.db_secret.arn

  redis_node_type            = "cache.m7g.large"
  automatic_failover_enabled = true
  num_cache_clusters         = 2
}

# 3. SQS 대기열 모듈 호출 
module "queue" {
  source = "../../../modules/queue"

  project_name = var.project_name
  environment  = var.environment

  queue_name = "${var.project_name}-waiting-queue.fifo"
  fifo_queue = true

  visibility_timeout_seconds = 60
}

# 5. AI VQA 전용 S3 버킷
resource "aws_s3_bucket" "ai_vqa_data" {
  bucket = "t6-on-race-ai-vqa-data-prod"

  tags = {
    Project = "t6-on-race"
    Team    = "AI"
    Usage   = "VQA-Storage"
  }
}

# 6. 35일 후 자동 삭제 정책
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