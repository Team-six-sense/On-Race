# 1. Loki 저장용 S3 버킷
resource "aws_s3_bucket" "loki_logs" {
  # 환경(prod/dev)을 이름에 포함하여 중복 및 혼선을 방지합니다.
  bucket = "${var.project_name}-${var.environment}-loki-logs-202020842"
  
  # 실수로 버킷이 삭제되는 것을 방지하려면 아래 주석을 해제하세요.
  # lifecycle { prevent_destroy = true }
}

# [추가] S3 퍼블릭 액세스 차단 (보안 필수)
resource "aws_s3_bucket_public_access_block" "loki_logs_limit" {
  bucket = aws_s3_bucket.loki_logs.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# 2. 30일 후 자동 삭제 (비용 관리)
resource "aws_s3_bucket_lifecycle_configuration" "loki_logs_lifecycle" {
  bucket = aws_s3_bucket.loki_logs.id
  rule {
    id     = "log-retention-30-days"
    status = "Enabled"
    filter {}
    expiration {
      days = 30
    }
  }
}

# 3. Loki 전용 IAM Role (IRSA)
module "loki_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name = "${var.cluster_name}-loki-irsa"

  oidc_providers = {
    main = {
      provider_arn               = var.oidc_provider_arn
      # loki-values.yaml에서 설정한 namespace:serviceaccount와 일치해야 합니다.
      namespace_service_accounts = ["loki:loki"]
    }
  }

  role_policy_arns = {
    s3_access = aws_iam_policy.loki_s3_policy.arn
  }
}

# 4. S3 접근 정책
resource "aws_iam_policy" "loki_s3_policy" {
  name        = "${var.cluster_name}-LokiS3Access"
  description = "Allow Loki to read/write logs to S3"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "s3:ListBucket",
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject"
      ]
      Resource = [
        aws_s3_bucket.loki_logs.arn,
        "${aws_s3_bucket.loki_logs.arn}/*"
      ]
    }]
  })
}