output "controller_role_arn" {
  value = aws_iam_role.karpenter_controller.arn
}

# Karpenter가 띄운 '노드'들이 사용할 IAM Role 이름
# 이 이름이 있어야 EC2NodeClass에서 노드에게 권한을 부여할 수 있습니다.
output "node_iam_role_name" {
  value       = aws_iam_role.node.name # 모듈 내의 노드용 IAM Role 리소스 명칭 확인 필요
  description = "The name of the IAM role for the Karpenter nodes"
}