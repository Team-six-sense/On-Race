# 1. CloudFront Signed URL용 공개키 및 키 그룹
resource "aws_cloudfront_public_key" "vqa_key" {
  name        = "${var.project_name}-vqa-public-key"
  encoded_key = file("${path.module}/certs/vqa_public_key.pem")
}

resource "aws_cloudfront_key_group" "vqa_key_group" {
  name  = "${var.project_name}-vqa-key-group"
  items = [aws_cloudfront_public_key.vqa_key.id]
}

# 2. S3 버킷 정책 (Base 레이어의 Output 참조)
resource "aws_s3_bucket_policy" "ai_vqa_bucket_policy" {
  # [해결] base 레이어에서 가져온 bucket_id 사용
  bucket = data.terraform_remote_state.base.outputs.ai_vqa_bucket_id 
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "AllowCloudFrontServicePrincipalReadOnly"
      Effect    = "Allow"
      Principal = { Service = "cloudfront.amazonaws.com" }
      Action    = "s3:GetObject"
      Resource  = "${data.terraform_remote_state.base.outputs.ai_vqa_bucket_arn}/*"
      Condition = {
        StringEquals = {
          "AWS:SourceArn" = aws_cloudfront_distribution.ai_vqa_cdn.arn
        }
      }
    }]
  })
}

# 3. CloudFront 배포 설정
resource "aws_cloudfront_distribution" "ai_vqa_cdn" {
  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"

  origin {
    # [해결] base 레이어에서 가져온 도메인 및 OAC ID 사용
    domain_name              = data.terraform_remote_state.base.outputs.ai_vqa_bucket_domain_name
    origin_id                = "S3-VQA-Data"
    origin_access_control_id = data.terraform_remote_state.base.outputs.ai_vqa_oac_id
  }

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3-VQA-Data"

    forwarded_values {
      query_string = false
      cookies { forward = "none" }
    }

    viewer_protocol_policy = "redirect-to-https"
    # Signed URL 사용 시 아래 주석 해제
    # trusted_key_groups = [aws_cloudfront_key_group.vqa_key_group.id]
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }

  tags = {
    Name = "${var.project_name}-vqa-cdn"
  }
}

# 4. AI/매크로 탐지용 공통 S3 접근 권한 (IRSA용)
resource "aws_iam_policy" "ai_s3_access" {
  name = "${var.project_name}-ai-s3-access"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
      Effect   = "Allow"
      Resource = [
        data.terraform_remote_state.base.outputs.ai_vqa_bucket_arn, 
        "${data.terraform_remote_state.base.outputs.ai_vqa_bucket_arn}/*"
      ]
    }]
  })
}

# 5. EKS IRSA (서브 모듈 경로 수정)
module "ai_vqa_irsa" {
  # [해결] // 를 사용하여 서브 디렉토리 경로 명시 (Unreadable module subdirectory 에러 방지)
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"
  
  role_name = "${var.project_name}-ai-vqa-pod-role"
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${var.namespace}:ai-service-account"]
    }
  }
  role_policy_arns = { s3_access = aws_iam_policy.ai_s3_access.arn }
}