# =====================================================================
# [보안] 팀원 접속 권한 관리
# =====================================================================

# AI 팀원을 위한 EC2 Instance Connect 접속 정책
resource "aws_iam_policy" "ai_team_eic_access" {
  name        = "${var.project_name}-ai-team-eic-access-policy"
  description = "Allows team members to connect to AI instances via EC2 Instance Connect"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = "ec2:DescribeInstances"
        Resource = "*"
      },
      {
        Effect   = "Allow"
        Action   = "ec2-instance-connect:OpenTunnel"
        Resource = "arn:aws:ec2:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:instance-connect-endpoint/*"
      },
      {
        Effect   = "Allow"
        Action   = "ec2-instance-connect:SendSSHPublicKey"
        Resource = "arn:aws:ec2:${data.aws_region.current.name}:${data.aws_caller_identity.current.account_id}:instance/*"
        Condition = {
          StringEquals = {
            "ec2:osuser"           = "ec2-user"
            "aws:ResourceTag/Team" = "Macro"
          }
        }
      }
    ]
  })
}