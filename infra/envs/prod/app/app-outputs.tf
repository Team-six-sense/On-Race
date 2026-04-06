# C:\Users\김서진\On-Race\infra\envs\prod\app\app-outputs.tf

output "cluster_name" {
  description = "EKS 클러스터 이름"
  value       = module.eks.cluster_name
}

output "cluster_endpoint" {
  description = "EKS 클러스터 엔드포인트 주소"
  value       = module.eks.cluster_endpoint
}

output "cluster_security_group_id" {
  description = "EKS 클러스터 보안 그룹 ID"
  value       = module.eks.cluster_security_group_id
}

output "node_security_group_id" {
  description = "EKS 워커 노드 보안 그룹 ID"
  value       = module.eks.node_security_group_id
}