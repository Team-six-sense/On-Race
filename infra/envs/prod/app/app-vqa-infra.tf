# 1. CloudFront Signed URL용 공개키 및 키 그룹 (App 계층 유지)
resource "aws_cloudfront_public_key" "vqa_key" {
  name        = "${var.project_name}-vqa-public-key"
  encoded_key = file("${path.module}/certs/vqa_public_key.pem")
}

resource "aws_cloudfront_key_group" "vqa_key_group" {
  name  = "${var.project_name}-vqa-key-group"
  items = [aws_cloudfront_public_key.vqa_key.id]
}

# 2. S3 버킷 정책 (Base의 Output 참조)
resource "aws_s3_bucket_policy" "ai_vqa_bucket_policy" {
  # [수정] base에서 출력한 bucket_id 사용
  bucket = data.terraform_remote_state.base.outputs.ai_vqa_bucket_id 
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "AllowCloudFrontServicePrincipalReadOnly"
      Effect    = "Allow"
      Principal = { Service = "cloudfront.amazonaws.com" }
      Action    = "s3:GetObject"
      # [수정] base에서 출력한 bucket_arn 사용
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
  origin {
    # [수정] base에서 출력한 domain_name 사용
    domain_name              = data.terraform_remote_state.base.outputs.ai_vqa_bucket_domain_name
    origin_id                = "S3-VQA-Data"
    origin_access_control_id = data.terraform_remote_state.base.outputs.ai_vqa_oac_id
  }
  # ... (이하 동일)
}

# 4. AI/매크로 탐지용 공통 S3 접근 권한 (IRSA용)
resource "aws_iam_policy" "ai_s3_access" {
  name = "${var.project_name}-ai-s3-access"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
      Effect   = "Allow"
      # [수정] base의 ARN 참조
      Resource = [
        data.terraform_remote_state.base.outputs.ai_vqa_bucket_arn, 
        "${data.terraform_remote_state.base.outputs.ai_vqa_bucket_arn}/*"
      ]
    }]
  })
}

# 5. EKS IRSA (App 계층 유지)
module "ai_vqa_irsa" {
  source    = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  role_name = "${var.project_name}-ai-vqa-pod-role"
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${var.namespace}:ai-service-account"]
    }
  }
  role_policy_arns = { s3_access = aws_iam_policy.ai_s3_access.arn }
}

# [주의] 아래에 있던 aws_iam_role(ai_ec2_role)과 instance_profile은 삭제되었습니다.
# 해당 리소스들은 이미 Base 계층에서 생성되어 관리됩니다.