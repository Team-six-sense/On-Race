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

# 2. 데이터 계층 모듈 호출 (Redis)
# 이 계층은 '기초 인프라'이므로 아직 생성되지 않은 EKS에 대한 의존성을 제거합니다.
module "data" {
  source = "../../../modules/data"

  project_name     = var.project_name
  environment      = var.environment
  vpc_id           = module.vpc.vpc_id
  database_subnets = module.vpc.database_subnets
  
  redis_node_type  = "cache.m7g.large" 
  
  automatic_failover_enabled = true
  num_cache_clusters          = 2

  # [수정] EKS 모듈 참조 제거
  # base 계층에서는 EKS가 없으므로 이 줄은 삭제하거나 주석 처리해야 배포가 가능합니다.
  # eks_node_security_group_id = module.eks.node_security_group_id 
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

# 6. 30일 후 자동 삭제 정책
resource "aws_s3_bucket_lifecycle_configuration" "ai_vqa_lifecycle" {
  bucket = aws_s3_bucket.ai_vqa_data.id

  rule {
    id     = "auto-delete-30-days"
    status = "Enabled"

    filter {}

    expiration {
      days = 35
    }
  }
}