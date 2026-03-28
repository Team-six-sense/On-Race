# 현재 AWS 계정 및 리전 정보 조회를 위한 데이터 소스
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# [추가] 0. ECR 리포지토리 자동 생성
resource "aws_ecr_repository" "api_repo" {
  name                 = "t6-on-race-api"
  image_tag_mutability = "MUTABLE"
  force_delete         = true # 인프라 일괄 삭제 시 편의를 위해 추가

  image_scanning_configuration {
    scan_on_push = true
  }
}

# 1. GitHub Actions용 OIDC Provider
resource "aws_iam_openid_connect_provider" "github" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1", "1c58a3a8518e8759bf075b76b750d4f2df264fcd"]
}

# 2. GitHub Actions 전용 IAM 역할 (브랜치 유연성 확보)
resource "aws_iam_role" "github_actions_ecr_role" {
  name = "${var.project_name}-github-actions-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRoleWithWebIdentity"
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.github.arn
        }
        Condition = {
          StringLike = {
            "token.actions.githubusercontent.com:sub": "repo:Team-six-sense/On-Race:ref:refs/heads/*"
          }
          StringEquals = {
            "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
          }
        }
      }
    ]
  })
}

# 3. ECR Push 최소 권한 정책 (리소스 ARN 동적화)
resource "aws_iam_policy" "ecr_push_policy" {
  name = "${var.project_name}-ecr-push-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = ["ecr:GetAuthorizationToken"]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload"
        ]
        # [수정] 위에서 생성한 ECR 리포지토리의 ARN을 동적으로 참조
        Resource = aws_ecr_repository.api_repo.arn
      }
    ]
  })
}

# 4. 정책 연결
resource "aws_iam_role_policy_attachment" "github_actions_attach" {
  role       = aws_iam_role.github_actions_ecr_role.name
  policy_arn = aws_iam_policy.ecr_push_policy.arn
}

# 5. 출력
output "github_actions_role_arn" {
  value       = aws_iam_role.github_actions_ecr_role.arn
  description = "GitHub Actions Workflow의 'role-to-assume'에 넣을 ARN"
}

# [추가] ECR URL 출력 (app-api.tf 등에서 참조 가능)
output "ecr_repository_url" {
  value       = aws_ecr_repository.api_repo.repository_url
  description = "생성된 ECR 리포지토리 URL"
}