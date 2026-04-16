# C:\Users\김서진\On-Race\infra\envs\prod\app\app-outputs.tf

# 1. EKS 클러스터 기본 정보
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

# 2. 애플리케이션 권한 및 환경 정보
output "api_iam_role_arn" {
  description = "API 파드에 부여된 IAM 역할 ARN (S3, Secrets Manager 권한 확인용)"
  value       = module.api_irsa.iam_role_arn
}

output "api_service_account_name" {
  description = "API용 쿠버네티스 서비스 어카운트 이름"
  value       = kubernetes_service_account_v1.api_sa.metadata[0].name
}

output "app_namespace" {
  description = "애플리케이션이 배포된 네임스페이스"
  value       = kubernetes_namespace_v1.app.metadata[0].name
}

# 3. 서비스 접속 정보 (추후 Ingress 설정 시 사용)
# Ingress 리소스(ALB)가 생성된 후 주석을 해제하면 외부 접속 주소를 바로 확인할 수 있습니다.
# output "ingress_hostname" {
#   description = "서비스 접속용 LoadBalancer DNS 이름"
#   value       = kubernetes_ingress_v1.on_race_ingress.status[0].load_balancer[0].ingress[0].hostname
# }