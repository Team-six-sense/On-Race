# C:\Users\김서진\On-Race\infra\modules\karpenter\k-outputs.tf

# 1. Karpenter 노드용 IAM 역할 이름 (EC2NodeClass에서 참조)
output "node_iam_role_name" {
  value       = aws_iam_role.karpenter_node.name
  description = "The name of the IAM role for the Karpenter nodes"
}

# 2. Karpenter 컨트롤러용 IAM 역할 ARN (Helm Chart에서 참조)
output "controller_role_arn" {
  value       = aws_iam_role.karpenter_controller.arn
  description = "The ARN of the IAM role for the Karpenter controller"
}