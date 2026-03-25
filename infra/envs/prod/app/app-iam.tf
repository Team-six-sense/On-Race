# 1. GitHub Actions용 OIDC Provider (중복 생성 에러 시 삭제 가능)
resource "aws_iam_openid_connect_provider" "github" {
  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1", "1c58a3a8518e8759bf075b76b750d4f2df264fcd"]
}

# 2. GitHub Actions 전용 IAM 역할 (특정 브랜치 제한 적용)
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
          StringEquals = {
            "token.actions.githubusercontent.com:aud": "sts.amazonaws.com",
            # [수정] 오직 feat/infra/init-monitoring 브랜치에서만 권한 획득 가능
            "token.actions.githubusercontent.com:sub": "repo:Team-six-sense/On-Race:ref:refs/heads/feat/infra/init-monitoring"
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
        Resource = "arn:aws:ecr:${var.aws_region}:${data.aws_caller_identity.current.account_id}:repository/t6-on-race-api"
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
  description = "GitHub Actions Workflow에 넣을 Role ARN"
}