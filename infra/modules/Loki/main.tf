# [수정] 상단 variable 선언부 삭제 (variables.tf로 통합됨)

# 1. Loki 저장용 S3 버킷
resource "aws_s3_bucket" "loki_logs" {
  bucket = "${var.project_name}-loki-logs-202020842"
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