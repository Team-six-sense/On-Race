# 0. ECR 리포지토리 자동 생성
resource "aws_ecr_repository" "api_repo" {
  name                 = "t6-on-race-api"
  image_tag_mutability = "MUTABLE"
  force_delete         = true 

  image_scanning_configuration {
    scan_on_push = true
  }
}

# 1. [수정] GitHub Actions용 OIDC Provider (이미 존재하므로 data 소스로 참조)
data "aws_iam_openid_connect_provider" "github" {
  url = "https://token.actions.githubusercontent.com"
}

# 2. GitHub Actions 전용 IAM 역할
resource "aws_iam_role" "github_actions_ecr_role" {
  name = "${var.project_name}-github-actions-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRoleWithWebIdentity"
        Effect = "Allow"
        Principal = {
          # data 소스의 arn을 참조
          Federated = data.aws_iam_openid_connect_provider.github.arn
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

# 3. ECR Push 최소 권한 정책
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

output "ecr_repository_url" {
  value       = aws_ecr_repository.api_repo.repository_url
  description = "생성된 ECR 리포지토리 URL"
}