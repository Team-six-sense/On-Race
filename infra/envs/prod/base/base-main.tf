# 0. RDS 서비스 연결 역할 (신규 계정 RDS Proxy 생성 에러 해결)
resource "aws_iam_service_linked_role" "rds" {
  aws_service_name = "rds.amazonaws.com"
}

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

  # enable_nat_gateway = true
  single_nat_gateway = false 
}

# 2. DB 암호 자동 생성 및 Secrets Manager 설정
resource "random_password" "db_password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

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

# 2-1. Grafana 관리자 암호 생성 및 Secrets Manager 저장
resource "aws_secretsmanager_secret" "grafana_admin_secret" {
  name        = "on-race-grafana-admin-password"
  description = "Grafana Admin Password Managed by Terraform"
  recovery_window_in_days = 0 # 운영 환경에서는 복구 기간을 설정하는 것을 권장합니다.

  tags = {
    Project = var.project_name
    Usage   = "Grafana-Credentials"
  }
}

resource "aws_secretsmanager_secret_version" "grafana_admin_secret_val" {
  secret_id     = aws_secretsmanager_secret.grafana_admin_secret.id
  secret_string = jsonencode({
    password = "OnRace-Grafana-Password-2026!"
  })
}

# 3. 데이터 계층 모듈 (RDS Proxy 의존성 위해 depends_on 권장)
module "data" {
  source = "../../../modules/data"

  project_name      = var.project_name
  environment       = var.environment
  vpc_id            = module.vpc.vpc_id
  database_subnets  = module.vpc.database_subnets
  db_password       = random_password.db_password.result
  db_secret_arn     = aws_secretsmanager_secret.db_secret.arn
  
  redis_node_type            = "cache.t4g.small"
  automatic_failover_enabled = true
  num_cache_clusters         = 2

  depends_on = [aws_iam_service_linked_role.rds]
}

# 4. SQS 대기열
module "queue" {
  source = "../../../modules/queue"

  project_name = var.project_name
  environment  = var.environment
  queue_name   = "${var.project_name}-waiting-queue.fifo"
  fifo_queue   = true
  visibility_timeout_seconds = 60
}

# 5. AI VQA S3 버킷 (계정 ID 추가하여 이름 중복 방지)
resource "aws_s3_bucket" "ai_vqa_data" {
  bucket = "t6-on-race-ai-vqa-data-916228846377" # 계정 ID 추가

  tags = {
    Project = var.project_name
    Team    = "AI"
    Usage   = "VQA-Storage"
  }
}

resource "aws_s3_bucket_public_access_block" "ai_vqa_block" {
  bucket = aws_s3_bucket.ai_vqa_data.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "ai_vqa_lifecycle" {
  bucket = aws_s3_bucket.ai_vqa_data.id

  rule {
    id     = "vqa-temp-cleanup"
    status = "Enabled"
    filter { prefix = "vqa/temp/" }
    expiration { days = 35 }
  }
}

# 6. ECR 리포지토리
resource "aws_ecr_repository" "app_repo" {
  name                 = "${var.project_name}-repo"
  image_tag_mutability = "MUTABLE"
  force_delete         = false

  image_scanning_configuration {
    scan_on_push = true
  }
}

# 7. GitHub Actions용 OIDC Provider (data에서 resource로 변경)
resource "aws_iam_openid_connect_provider" "github" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1", "1c58a3a8518e8759bf075b76b750d4f2df264fcd"]
}

# 8. GitHub Actions 전용 IAM 역할
resource "aws_iam_role" "github_actions_ecr_role" {
  name                 = "${var.project_name}-github-actions-role"
  max_session_duration = 14400

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRoleWithWebIdentity"
      Effect = "Allow"
      Principal = {
        Federated = aws_iam_openid_connect_provider.github.arn
      }
      Condition = {
        StringLike = {
          "token.actions.githubusercontent.com:sub": "repo:Team-six-sense/On-Race:ref:refs/heads/*"
        }
        StringEquals = {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        }
      }
    }]
  })
}

# 9. GitHub Actions 정책 (S3 ARN 업데이트)
resource "aws_iam_policy" "ecr_push_policy" {
  name = "${var.project_name}-github-actions-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = ["eks:DescribeCluster", "eks:ListClusters", "eks:AccessKubernetesApi"]
        Resource = "*"
      },
      {
        Effect   = "Allow"
        Action   = ["ec2:DescribeInstances", "ec2:DescribeTags"]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = ["s3:PutObject", "s3:GetObject", "s3:ListBucket", "s3:DeleteObject"]
        Resource = [
          "arn:aws:s3:::t6-on-race-tfstate-916228846377-ap-northeast-2-an",
          "arn:aws:s3:::t6-on-race-tfstate-916228846377-ap-northeast-2-an/*"
        ]
      },
      {
        Effect = "Allow"
        Action = ["ecr:GetAuthorizationToken", "ecr:BatchCheckLayerAvailability", "ecr:PutImage", "ecr:InitiateLayerUpload", "ecr:UploadLayerPart", "ecr:CompleteLayerUpload"]
        Resource = aws_ecr_repository.app_repo.arn
      },
      {
        Effect   = "Allow"
        Action   = ["sts:GetCallerIdentity"]
        Resource = "*"
      }
    ]
  })
}

# 10. Terraform Backend (S3/DynamoDB) 접근 권한 정책 (ARN 수정)
resource "aws_iam_policy" "terraform_state_policy" {
  name        = "${var.project_name}-tfstate-policy"
  description = "Allow GitHub Actions to manage Terraform state in S3 and DynamoDB"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = ["s3:ListBucket", "s3:GetBucketLocation"]
        Resource = "arn:aws:s3:::t6-on-race-tfstate-916228846377-ap-northeast-2-an"
      },
      {
        Effect = "Allow"
        Action = ["s3:GetObject", "s3:PutObject", "s3:DeleteObject"]
        Resource = "arn:aws:s3:::t6-on-race-tfstate-916228846377-ap-northeast-2-an/prod/*"
      },
      {
        Effect = "Allow"
        Action = ["dynamodb:DescribeTable", "dynamodb:GetItem", "dynamodb:PutItem", "dynamodb:DeleteItem"]
        Resource = "arn:aws:dynamodb:ap-northeast-2:916228846377:table/t6-on-race-tfstate-lock"
      }
    ]
  })
}

# 정책 연결
resource "aws_iam_role_policy_attachment" "github_actions_attach" {
  role       = aws_iam_role.github_actions_ecr_role.name
  policy_arn = aws_iam_policy.ecr_push_policy.arn
}

resource "aws_iam_role_policy_attachment" "github_actions_state_attach" {
  role       = aws_iam_role.github_actions_ecr_role.name
  policy_arn = aws_iam_policy.terraform_state_policy.arn
}