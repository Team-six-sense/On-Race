# 1. AI 팀용 IAM 그룹 생성
resource "aws_iam_group" "ai_team" {
  name = "${var.project_name}-ai-team-group"
}

data "aws_caller_identity" "current" {}

# 2. AI 팀 SSM 전체 관리 권한
resource "aws_iam_group_policy_attachment" "ai_team_ssm" {
  group      = aws_iam_group.ai_team.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMFullAccess"
}

# 3. AI 팀원 계정 생성 및 그룹 할당
resource "aws_iam_user" "ai_dev1" {
  name = "on-race-ai-dev1"
}

resource "aws_iam_user_group_membership" "ai_team_membership" {
  user   = aws_iam_user.ai_dev1.name
  groups = [aws_iam_group.ai_team.name]
}

# 4. EC2 읽기 권한
resource "aws_iam_group_policy_attachment" "ai_team_ec2_view" {
  group      = aws_iam_group.ai_team.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess"
}

# 5. ECR 푸시 권한
resource "aws_iam_group_policy_attachment" "ai_team_ecr_push" {
  group      = aws_iam_group.ai_team.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryPowerUser"
}

# 6. EC2 제어 권한 (태그 기반)
resource "aws_iam_policy" "macro_team_ec2_control" {
  name        = "T6-MacroTeam-EC2-Control"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["ec2:StartInstances", "ec2:StopInstances"]
      Resource = "arn:aws:ec2:ap-northeast-2:${data.aws_caller_identity.current.account_id}:instance/*"
      Condition = {
        StringEquals = { "aws:ResourceTag/Team": "Macro" }
      }
    }]
  })
}

resource "aws_iam_group_policy_attachment" "ai_team_ec2_control_attach" {
  group      = aws_iam_group.ai_team.name
  policy_arn = aws_iam_policy.macro_team_ec2_control.arn
}

# 7. EKS 클러스터 정보 조회 권한
resource "aws_iam_policy" "ai_team_eks_view" {
  name = "T6-AITeam-EKS-View"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["eks:DescribeCluster"]
      Resource = "arn:aws:eks:ap-northeast-2:${data.aws_caller_identity.current.account_id}:cluster/${var.project_name}-${var.environment}-cluster"
    }]
  })
}

resource "aws_iam_group_policy_attachment" "ai_team_eks_view_attach" {
  group      = aws_iam_group.ai_team.name
  policy_arn = aws_iam_policy.ai_team_eks_view.arn
}

# 8. 콘솔 진단 권한 (GetConsoleOutput 등)
resource "aws_iam_policy" "ai_team_console_diagnostic" {
  name = "T6-AITeam-Console-Diagnostic"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = [
        "ec2:GetConsoleOutput",
        "ec2:GetConsoleScreenshot",
        "compute-optimizer:GetEnrollmentStatus"
      ]
      Resource = "*"
    }]
  })
}

resource "aws_iam_group_policy_attachment" "ai_team_console_attach" {
  group      = aws_iam_group.ai_team.name
  policy_arn = aws_iam_policy.ai_team_console_diagnostic.arn
}

# 9. VQA ECR 리포지토리
resource "aws_ecr_repository" "vqa_repo" {
  name                 = "on-race-vqa"
  image_tag_mutability = "MUTABLE"
  force_delete         = false
  image_scanning_configuration { scan_on_push = true }
}