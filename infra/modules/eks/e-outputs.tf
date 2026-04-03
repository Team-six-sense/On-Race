output "cluster_name" {
  value = aws_eks_cluster.this.name
}

output "cluster_endpoint" {
  value = aws_eks_cluster.this.endpoint
}

output "cluster_certificate_authority_data" {
  value = aws_eks_cluster.this.certificate_authority[0].data
}

output "oidc_provider_arn" {
  value = aws_iam_openid_connect_provider.this.arn
}

output "node_iam_role_name" {
  description = "EKS 워커 노드 그룹이 사용하는 IAM 역할 이름"
  # 만약 modules/eks/main.tf에서 iam_role 리소스 이름이 'nodes'가 아니라면 수정이 필요합니다.
  value = aws_iam_role.node.name
}

# 클러스터 자체 보안 그룹 ID (EKS가 자동 생성하는 SG)
output "cluster_security_group_id" {
  description = "EKS 클러스터가 생성한 기본 보안 그룹 ID"
  value       = aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
}

output "node_security_group_id" {
  description = "Common Security Group ID used by all nodes (Managed & Karpenter)"
  value       = aws_security_group.nodes.id
}