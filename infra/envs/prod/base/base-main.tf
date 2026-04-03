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

  redis_node_type            = "cache.t4g.micro" # [수정] 대폭 하향
  automatic_failover_enabled = false             # [수정] 단일 노드 운영을 위해 false
  num_cache_clusters         = 1                 # [수정] 2 -> 1
  /*
  redis_node_type            = "cache.t4g.medium"
  automatic_failover_enabled = true
  num_cache_clusters         = 2
  */
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

# [App에서 이동] S3 퍼블릭 액세스 완전 차단
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
    expiration {
      days = 35
    }
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

# 7. GitHub Actions용 OIDC Provider (Data 소스)
data "aws_iam_openid_connect_provider" "github" {
  url = "https://token.actions.githubusercontent.com"
}

# 8. GitHub Actions 전용 IAM 역할
resource "aws_iam_role" "github_actions_ecr_role" {
  name = "${var.project_name}-github-actions-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRoleWithWebIdentity"
      Effect = "Allow"
      Principal = {
        Federated = data.aws_iam_openid_connect_provider.github.arn
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

# 9. ECR Push 최소 권한 정책
resource "aws_iam_policy" "ecr_push_policy" {
  name = "${var.project_name}-ecr-push-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = ["ecr:GetAuthorizationToken"]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
        # [수정] 동일 파일 내 리소스를 직접 참조하여 의존성 명확화
        Resource = aws_ecr_repository.app_repo.arn 
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "github_actions_attach" {
  role       = aws_iam_role.github_actions_ecr_role.name
  policy_arn = aws_iam_policy.ecr_push_policy.arn
}

# [추가] 10. Terraform Backend(S3, DynamoDB) 접근 권한 정책
resource "aws_iam_policy" "terraform_state_policy" {
  name        = "${var.project_name}-tfstate-policy"
  description = "Allow GitHub Actions to manage Terraform state in S3 and DynamoDB"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:ListBucket",
          "s3:GetBucketLocation"
        ]
        Resource = "arn:aws:s3:::t6-on-race-terraform-state-prod"
      },
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject"
        ]
        Resource = "arn:aws:s3:::t6-on-race-terraform-state-prod/prod/*"
      },
      {
        # DynamoDB를 사용한 State Locking을 사용 중이라면 추가 (권장)
        Effect = "Allow"
        Action = [
          "dynamodb:DescribeTable",
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:DeleteItem"
        ]
        Resource = "arn:aws:dynamodb:ap-northeast-2:*:table/t6-on-race-terraform-lock-prod"
      }
    ]
  })
}

# 정책 연결 추가
resource "aws_iam_role_policy_attachment" "github_actions_state_attach" {
  role       = aws_iam_role.github_actions_ecr_role.name
  policy_arn = aws_iam_policy.terraform_state_policy.arn
}

# GitHub Actions 역할이 "모든 인프라"를 주무를 수 있게 마스터 권한을 줍니다.
resource "aws_iam_role_policy_attachment" "github_actions_admin_attach" {
  role       = aws_iam_role.github_actions_ecr_role.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}