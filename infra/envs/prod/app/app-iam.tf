# =====================================================================
# [중앙 관리] 서비스별 IAM 역할(IRSA) 및 서비스 어카운트
# =====================================================================

# 1. 공용 IAM 정책 (Secrets Manager 읽기 전용)
resource "aws_iam_policy" "secrets_read_policy" {
  name = "${var.project_name}-${var.environment}-secrets-read-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["secretsmanager:GetSecretValue", "secretsmanager:DescribeSecret"]
      Resource = data.aws_secretsmanager_secret.db_secret.arn
    }]
  })
}

# [이동] API 서비스가 사용할 S3 접근 정책
resource "aws_iam_policy" "api_s3_policy" {
  name = "${var.project_name}-${var.environment}-api-s3-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
      Resource = [
        "arn:aws:s3:::${data.terraform_remote_state.base.outputs.ai_vqa_bucket_name}",
        "arn:aws:s3:::${data.terraform_remote_state.base.outputs.ai_vqa_bucket_name}/*"
      ]
    }]
  })
}

# 2. API 서비스용 IRSA 및 SA
module "api_irsa" {
  source    = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version   = "~> 5.0"
  role_name = "${var.project_name}-${var.environment}-api-role"
  role_policy_arns = {
    secrets = aws_iam_policy.secrets_read_policy.arn
    s3      = aws_iam_policy.api_s3_policy.arn
  }
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${kubernetes_namespace_v1.app.metadata[0].name}:on-race-api-sa"]
    }
  }
}
resource "kubernetes_service_account_v1" "api_sa" {
  metadata {
    name        = "on-race-api-sa"
    namespace   = kubernetes_namespace_v1.app.metadata[0].name
    annotations = { "eks.amazonaws.com/role-arn" = module.api_irsa.iam_role_arn }
  }
}

# 3. Auth 서비스용 IRSA 및 SA
module "auth_irsa" {
  source           = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version          = "~> 5.0"
  role_name        = "${var.project_name}-${var.environment}-auth-role"
  role_policy_arns = { secrets = aws_iam_policy.secrets_read_policy.arn }
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${kubernetes_namespace_v1.app.metadata[0].name}:on-race-auth-sa"]
    }
  }
}
resource "kubernetes_service_account_v1" "auth_sa" {
  metadata {
    name        = "on-race-auth-sa"
    namespace   = kubernetes_namespace_v1.app.metadata[0].name
    annotations = { "eks.amazonaws.com/role-arn" = module.auth_irsa.iam_role_arn }
  }
}

# 4. Gateway 서비스용 IRSA 및 SA
module "gateway_irsa" {
  source           = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version          = "~> 5.0"
  role_name        = "${var.project_name}-${var.environment}-gateway-role"
  role_policy_arns = { secrets = aws_iam_policy.secrets_read_policy.arn }
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${kubernetes_namespace_v1.app.metadata[0].name}:on-race-gateway-sa"]
    }
  }
}
resource "kubernetes_service_account_v1" "gateway_sa" {
  metadata {
    name        = "on-race-gateway-sa"
    namespace   = kubernetes_namespace_v1.app.metadata[0].name
    annotations = { "eks.amazonaws.com/role-arn" = module.gateway_irsa.iam_role_arn }
  }
}

# 5. Queue 서비스용 IRSA 및 SA
module "queue_irsa" {
  source           = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version          = "~> 5.0"
  role_name        = "${var.project_name}-${var.environment}-queue-role"
  role_policy_arns = { secrets = aws_iam_policy.secrets_read_policy.arn }
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["${kubernetes_namespace_v1.app.metadata[0].name}:on-race-queue-sa"]
    }
  }
}
resource "kubernetes_service_account_v1" "queue_sa" {
  metadata {
    name        = "on-race-queue-sa"
    namespace   = kubernetes_namespace_v1.app.metadata[0].name
    annotations = { "eks.amazonaws.com/role-arn" = module.queue_irsa.iam_role_arn }
  }
}