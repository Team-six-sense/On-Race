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

module "ai_irsa" {
  source    = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version   = "~> 5.0"
  role_name = "${var.project_name}-ai-irsa"
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${kubernetes_namespace_v1.app.metadata[0].name}:ai-service-account"]
    }
  }
  role_policy_arns = { s3 = aws_iam_policy.ai_s3_access.arn }
}

resource "kubernetes_service_account_v1" "ai_sa" {
  metadata {
    name        = "ai-service-account"
    namespace   = kubernetes_namespace_v1.app.metadata[0].name
    annotations = { "eks.amazonaws.com/role-arn" = module.ai_irsa.iam_role_arn }
  }
}

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