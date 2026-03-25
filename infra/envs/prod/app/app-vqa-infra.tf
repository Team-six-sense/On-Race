# 1. S3 접근 정책 (기존 유지)
resource "aws_iam_policy" "ai_s3_access" {
  name = "${var.project_name}-ai-s3-access"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
      Effect   = "Allow"
      Resource = ["arn:aws:s3:::t6-on-race-ai-vqa-data-prod", "arn:aws:s3:::t6-on-race-ai-vqa-data-prod/*"]
    }]
  })
}

# 2. EC2용 IAM 역할 생성 (IRSA 대체)
resource "aws_iam_role" "ai_ec2_role" {
  name = "${var.project_name}-ai-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
  })
}

# 3. 역할에 S3 정책 연결
resource "aws_iam_role_policy_attachment" "ai_s3_attach" {
  role       = aws_iam_role.ai_ec2_role.name
  policy_arn = aws_iam_policy.ai_s3_access.arn
}

# 4. EC2에 연결할 인스턴스 프로파일 생성
resource "aws_iam_instance_profile" "ai_ec2_profile" {
  name = "${var.project_name}-ai-ec2-profile"
  role = aws_iam_role.ai_ec2_role.name
}

# ==========================================
# 아래 CloudFront 관련 리소스는 기존 코드 유지
# ==========================================
resource "aws_cloudfront_origin_access_control" "ai_vqa_oac" {
  name                              = "${var.project_name}-ai-vqa-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "ai_vqa_cdn" {
  origin {
    domain_name              = "t6-on-race-ai-vqa-data-prod.s3.ap-northeast-2.amazonaws.com"
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
    geo_restriction {
      restriction_type = "none"
    }
  }
  viewer_certificate { cloudfront_default_certificate = true }
}