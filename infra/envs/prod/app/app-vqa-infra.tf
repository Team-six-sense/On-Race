# 1. VQA 전용 S3 버킷 생성 (정책에 따라 수동 생성 대신 코드로 관리 권장)
resource "aws_s3_bucket" "vqa_data" {
  bucket = "t6-on-race-ai-vqa-data-prod"

  tags = {
    Name = "${var.project_name}-vqa-data"
  }
}

# 2. S3 수명 주기 정책 (임시 파일 35일 후 자동 삭제)
resource "aws_s3_bucket_lifecycle_configuration" "vqa_lifecycle" {
  bucket = aws_s3_bucket.vqa_data.id

  rule {
    id     = "vqa-temp-cleanup"
    status = "Enabled"

    filter {
      prefix = "vqa/temp/"
    }

    expiration {
      days = 35
    }
  }
}

# 3. 공통 S3 접근 정책 (EC2와 EKS 파드가 공유)
resource "aws_iam_policy" "ai_s3_access" {
  name        = "${var.project_name}-ai-s3-access"
  description = "Policy for AI models to access VQA S3 bucket"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
      Effect   = "Allow"
      Resource = [
        aws_s3_bucket.vqa_data.arn,
        "${aws_s3_bucket.vqa_data.arn}/*"
      ]
    }]
  })
}

# 4. [매크로 탐지용] EC2 IAM 역할 및 인스턴스 프로파일
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

# 5. [VQA 파드용] EKS IRSA (Service Account 연동)
module "ai_vqa_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.30"

  role_name = "${var.project_name}-ai-vqa-pod-role"
  
  oidc_providers = {
    main = {
      provider_arn = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${var.namespace}:ai-service-account"]
    }
  }

  role_policy_arns = {
    s3_access = aws_iam_policy.ai_s3_access.arn
  }
}

# 6. [보안] CloudFront OAC 전용 S3 버킷 정책
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
}

# 7. CloudFront 설정 (OAC 적용)
resource "aws_cloudfront_origin_access_control" "ai_vqa_oac" {
  name                              = "${var.project_name}-ai-vqa-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "ai_vqa_cdn" {
  origin {
    domain_name              = aws_s3_bucket.vqa_data.bucket_regional_domain_name
    origin_id                = "S3-VQA-Data"
    origin_access_control_id = aws_cloudfront_origin_access_control.ai_vqa_oac.id
  }

  enabled = true
  comment = "CDN for AI VQA Video and Audio"

  default_cache_behavior {
    allowed_methods        = ["GET", "HEAD", "OPTIONS"]
    cached_methods         = ["GET", "HEAD"]
    target_origin_id       = "S3-VQA-Data"
    viewer_protocol_policy = "redirect-to-https"

    forwarded_values {
      query_string = false
      cookies { forward = "none" }
    }
  }

  restrictions {
    geo_restriction { restriction_type = "none" }
  }

  viewer_certificate { cloudfront_default_certificate = true }
}