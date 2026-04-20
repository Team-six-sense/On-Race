# # 2. CloudFront 키 그룹
# resource "aws_cloudfront_key_group" "vqa_key_group" {
#   name  = "${var.project_name}-vqa-key-group-v2" # 이름을 변경하여 리소스 재생성 유도
#   items = [aws_cloudfront_public_key.vqa_key_v2.id]
# }

# # 3. S3 버킷 정책
# resource "aws_s3_bucket_policy" "ai_vqa_bucket_policy" {
#   bucket = data.terraform_remote_state.base.outputs.ai_vqa_bucket_id 
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [{
#       Sid       = "AllowCloudFrontServicePrincipalReadOnly"
#       Effect    = "Allow"
#       Principal = { Service = "cloudfront.amazonaws.com" }
#       Action    = "s3:GetObject"
#       Resource  = "${data.terraform_remote_state.base.outputs.ai_vqa_bucket_arn}/*"
#       Condition = {
#         StringEquals = {
#           "AWS:SourceArn" = aws_cloudfront_distribution.ai_vqa_cdn.arn
#         }
#       }
#     }]
#   })
# }

# # 4. CloudFront 배포 설정
# resource "aws_cloudfront_distribution" "ai_vqa_cdn" {
#   enabled             = true
#   is_ipv6_enabled     = true
#   default_root_object = "index.html"

#   origin {
#     domain_name              = data.terraform_remote_state.base.outputs.ai_vqa_bucket_domain_name
#     origin_id                = "S3-VQA-Data"
#     origin_access_control_id = data.terraform_remote_state.base.outputs.ai_vqa_oac_id
#   }

#   default_cache_behavior {
#     allowed_methods  = ["GET", "HEAD"]
#     cached_methods   = ["GET", "HEAD"]
#     target_origin_id = "S3-VQA-Data"
#     forwarded_values {
#       query_string = false
#       cookies { forward = "none" }
#     }
#     # [수정] API가 생성한 서명된 URL을 검증하기 위해 키 그룹을 신뢰하도록 설정합니다.
#     # 이 설정을 추가하면 배포 리소스가 업데이트되어 의존성 오류가 해결됩니다.
#     trusted_key_groups = [
#       aws_cloudfront_key_group.vqa_key_group.id
#     ]
#     viewer_protocol_policy = "redirect-to-https"
#   }

#   restrictions {
#     geo_restriction { restriction_type = "none" }
#   }

#   viewer_certificate {
#     cloudfront_default_certificate = true
#   }

#   tags = { Name = "${var.project_name}-vqa-cdn" }
# }

# # 5. S3 접근 IAM 정책
# resource "aws_iam_policy" "ai_s3_access" {
#   name = "${var.project_name}-ai-s3-access"
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [{
#       Action   = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
#       Effect   = "Allow"
#       Resource = [
#         data.terraform_remote_state.base.outputs.ai_vqa_bucket_arn, 
#         "${data.terraform_remote_state.base.outputs.ai_vqa_bucket_arn}/*"
#       ]
#     }]
#   })
# }

# # 6. EKS IRSA (VQA 파드 전용 역할)
# module "ai_vqa_irsa" {
#   source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
#   version = "~> 5.0"
  
#   role_name = "${var.project_name}-ai-vqa-pod-role"
#   oidc_providers = {
#     main = {
#       provider_arn               = module.eks.oidc_provider_arn
#       # [교정] 네임스페이스를 동적으로 직접 참조
#       namespace_service_accounts = ["${kubernetes_namespace_v1.app.metadata[0].name}:ai-service-account"]
#     }
#   }
#   role_policy_arns = { s3_access = aws_iam_policy.ai_s3_access.arn }
# }