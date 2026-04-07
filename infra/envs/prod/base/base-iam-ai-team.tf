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

# AI 팀이 EC2 인스턴스 목록을 볼 수 있도록 읽기 전용 권한 추가
resource "aws_iam_group_policy_attachment" "ai_team_ec2_view" {
  group      = aws_iam_group.ai_team.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess"
}

# 인스턴스 제어(중지/시작) 권한 정책
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

# 정책을 AI 팀 그룹에 연결
resource "aws_iam_group_policy_attachment" "ai_team_ec2_control_attach" {
  group      = aws_iam_group.ai_team.name
  policy_arn = aws_iam_policy.macro_team_ec2_control.arn
}