# 1. AI 팀용 IAM 그룹 생성
resource "aws_iam_group" "ai_team" {
  name = "${var.project_name}-ai-team-group"
}

# 현재 로그인된 AWS 계정 정보를 가져오는 데이터 소스
data "aws_caller_identity" "current" {}

# 2. AI 팀에게 필요한 최소 권한 (콘솔 로그인 + SSM 접속)
resource "aws_iam_group_policy_attachment" "ai_team_ssm" {
  group      = aws_iam_group.ai_team.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMFullAccess" # 실제로는 더 좁게 설정 권장
}

# 3. AI 팀원 계정 생성 (예: ai-dev1)
resource "aws_iam_user" "ai_dev1" {
  name = "on-race-ai-dev1"
}

resource "aws_iam_user_group_membership" "ai_team_membership" {
  user   = aws_iam_user.ai_dev1.name
  groups = [aws_iam_group.ai_team.name]
}

# 4. AI 팀이 EC2 인스턴스 목록을 볼 수 있도록 읽기 전용 권한 추가
resource "aws_iam_group_policy_attachment" "ai_team_ec2_view" {
  group      = aws_iam_group.ai_team.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess"
}

# 5. [추가됨] ECR 이미지 푸시 권한 추가 (VQA 이미지 업로드용)
resource "aws_iam_group_policy_attachment" "ai_team_ecr_push" {
  group      = aws_iam_group.ai_team.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryPowerUser"
}

# 6. 인스턴스 제어(중지/시작) 권한 정책
resource "aws_iam_policy" "macro_team_ec2_control" {
  name        = "T6-MacroTeam-EC2-Control"
  description = "매크로 탐지용 EC2 인스턴스 제어 권한"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["ec2:StartInstances", "ec2:StopInstances"]
        Resource = "arn:aws:ec2:ap-northeast-2:${data.aws_caller_identity.current.account_id}:instance/*"
        # Team: Macro 태그가 붙은 인스턴스만 제어 가능하도록 제한
        Condition = {
          StringEquals = {
            "aws:ResourceTag/Team": "Macro"
          }
        }
      }
    ]
  })
}

# VQA 전용 ECR 리포지토리
resource "aws_ecr_repository" "vqa_repo" {
  name                 = "on-race-vqa"
  image_tag_mutability = "MUTABLE"
  force_delete         = false

  image_scanning_configuration {
    scan_on_push = true
  }
}

# 정책을 AI 팀 그룹에 연결
resource "aws_iam_group_policy_attachment" "ai_team_ec2_control_attach" {
  group      = aws_iam_group.ai_team.name
  policy_arn = aws_iam_policy.macro_team_ec2_control.arn
}

# 7. EKS 클러스터 접속 정보(kubeconfig) 다운로드 권한
# 이 정책이 있어야 AI 팀원이 'aws eks update-kubeconfig' 명령어를 실행할 수 있습니다.
resource "aws_iam_policy" "ai_team_eks_view" {
  name        = "T6-AITeam-EKS-View"
  description = "Allow AI team to update kubeconfig for VQA deployment"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["eks:DescribeCluster"]
        # 클러스터 ARN을 변수를 사용하여 동적으로 지정합니다.
        Resource = "arn:aws:eks:ap-northeast-2:${data.aws_caller_identity.current.account_id}:cluster/${var.project_name}-${var.environment}-cluster"
      }
    ]
  })
}

# 생성한 정책을 AI 팀 그룹에 연결합니다.
resource "aws_iam_group_policy_attachment" "ai_team_eks_view_attach" {
  group      = aws_iam_group.ai_team.name
  policy_arn = aws_iam_policy.ai_team_eks_view.arn
}