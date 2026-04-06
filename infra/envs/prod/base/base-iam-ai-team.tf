# 1. AI 팀용 IAM 그룹 생성
resource "aws_iam_group" "ai_team" {
  name = "${var.project_name}-ai-team-group"
}

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