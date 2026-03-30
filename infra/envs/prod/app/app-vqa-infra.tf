# 0. 기존 버킷 임포트 (배포 성공 후 삭제 가능)
import {
  to = aws_s3_bucket.vqa_data
  id = "t6-on-race-ai-vqa-data-prod"
}

# 1. CloudFront Signed URL용 공개키 및 키 그룹 설정
# 로컬 certs 폴더에 vqa_public_key.pem 파일이 있어야 합니다.
resource "aws_cloudfront_public_key" "vqa_key" {
  name        = "${var.project_name}-vqa-public-key"
  comment     = "VQA Signed URL Public Key"
  encoded_key = file("${path.module}/certs/vqa_public_key.pem")
}

resource "aws_cloudfront_key_group" "vqa_key_group" {
  name    = "${var.project_name}-vqa-key-group"
  items   = [aws_cloudfront_public_key.vqa_key.id]
  comment = "VQA Key Group for Signed URLs"
}

# 2. VQA 전용 S3 버킷 및 보안 설정
resource "aws_s3_bucket" "vqa_data" {
  bucket = "t6-on-race-ai-vqa-data-prod"
  tags   = { Name = "${var.project_name}-vqa-data" }
}

# [추가] S3 퍼블릭 액세스 완전 차단
resource "aws_s3_bucket_public_access_block" "vqa_data_block" {
  bucket = aws_s3_bucket.vqa_data.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "vqa_lifecycle" {
  bucket = aws_s3_bucket.vqa_data.id
  rule {
    id     = "vqa-temp-cleanup"
    status = "Enabled"
    filter { prefix = "vqa/temp/" }
    expiration { days = 35 }
  }
}

# 3. CloudFront OAC 전용 S3 버킷 정책
resource "aws_s3_bucket_policy" "ai_vqa_bucket_policy" {
  bucket = aws_s3_bucket.vqa_data.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "AllowCloudFrontServicePrincipalReadOnly"
      Effect    = "Allow"
      Principal = { Service = "cloudfront.amazonaws.com" }
      Action    = "s3:GetObject"
      Resource  = "${aws_s3_bucket.vqa_data.arn}/*"
      Condition = {
        StringEquals = {
          "AWS:SourceArn" = aws_cloudfront_distribution.ai_vqa_cdn.arn
        }
      }
    }]
  })
  depends_on = [aws_s3_bucket_public_access_block.vqa_data_block]
}

# 4. CloudFront 배포 설정 (Signed URL 적용)
resource "aws_cloudfront_distribution" "ai_vqa_cdn" {
  origin {
    domain_name              = aws_s3_bucket.vqa_data.bucket_regional_domain_name
    origin_id                = "S3-VQA-Data"
    origin_access_control_id = aws_cloudfront_origin_access_control.ai_vqa_oac.id
  }

  enabled = true
  comment = "CDN for AI VQA Video and Audio"

  default_cache_behavior {
    # [핵심] Signed URL 인증을 거친 요청만 허용
    trusted_key_groups = [aws_cloudfront_key_group.vqa_key_group.id]

    target_origin_id       = "S3-VQA-Data"
    viewer_protocol_policy = "redirect-to-https"
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD"]

    forwarded_values {
      query_string = false
      cookies      { forward = "none" }
    }
    
    min_ttl     = 0
    default_ttl = 3600
    max_ttl     = 86400
  }

  restrictions {
    geo_restriction { restriction_type = "none" }
  }
  viewer_certificate { cloudfront_default_certificate = true }
}

resource "aws_cloudfront_origin_access_control" "ai_vqa_oac" {
  name                              = "${var.project_name}-ai-vqa-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# 5. 프라이빗 키 보안 관리 (Secrets Manager)
resource "aws_secretsmanager_secret" "vqa_private_key" {
  name        = "${var.project_name}/vqa/cloudfront-private-key"
  description = "CloudFront Private Key for VQA Signed URLs"
}

# 6. 백엔드 IRSA용 권한 정책 (Secrets Manager 접근 허용)
resource "aws_iam_policy" "backend_secrets_policy" {
  name = "${var.project_name}-backend-secrets-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action   = "secretsmanager:GetSecretValue"
      Effect   = "Allow"
      Resource = aws_secretsmanager_secret.vqa_private_key.arn
    }]
  })
}

# 7. AI/매크로 탐지용 공통 S3 접근 권한 및 IRSA (기존 유지)
resource "aws_iam_policy" "ai_s3_access" {
  name = "${var.project_name}-ai-s3-access"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
      Effect   = "Allow"
      Resource = [aws_s3_bucket.vqa_data.arn, "${aws_s3_bucket.vqa_data.arn}/*"]
    }]
  })
}

module "ai_vqa_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.30"
  role_name = "${var.project_name}-ai-vqa-pod-role"
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${var.namespace}:ai-service-account"]
    }
  }
  role_policy_arns = { s3_access = aws_iam_policy.ai_s3_access.arn }
}

# 매크로 탐지용 EC2 IAM 역할 및 인스턴스 프로파일
resource "aws_iam_role" "ai_ec2_role" {
  name = "${var.project_name}-ai-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "ai_s3_attach" {
  role       = aws_iam_role.ai_ec2_role.name
  policy_arn = aws_iam_policy.ai_s3_access.arn
}

resource "aws_iam_instance_profile" "ai_ec2_profile" {
  name = "${var.project_name}-ai-ec2-profile"
  role = aws_iam_role.ai_ec2_role.name
}