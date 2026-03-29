# C:\Users\김서진\On-Race\infra\modules\karpenter\k-outputs.tf

# 1. Karpenter 노드용 IAM 역할 이름 (EC2NodeClass에서 참조)
output "node_iam_role_name" {
  value       = aws_iam_role.karpenter_node.name
  description = "The name of the IAM role for the Karpenter nodes"
}

# 2. Karpenter 컨트롤러용 IAM 역할 ARN (app-scaling.tf의 irsa_arn과 매칭)
output "irsa_arn" {
  value       = aws_iam_role.karpenter_controller.arn
  description = "The ARN of the IAM role for the Karpenter controller"
}

# 3. Interruption 큐 이름 (app-scaling.tf의 queue_name과 매칭)
output "queue_name" {
  value       = aws_sqs_queue.karpenter.name
  description = "The name of the SQS queue for Karpenter interruptions"
}