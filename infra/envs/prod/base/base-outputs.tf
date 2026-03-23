# C:\Users\김서진\On-Race\infra\envs\prod\base\base-outputs.tf

output "vpc_id" {
  description = "생성된 VPC의 ID"
  value       = module.vpc.vpc_id
}

output "private_subnets" {
  description = "EKS 노드가 배치될 프라이빗 서브넷 리스트"
  value       = module.vpc.private_subnets
}

output "public_subnets" {
  description = "퍼블릭 서브넷 리스트 (ALB 등 외부 연결용)"
  value       = module.vpc.public_subnets
}

output "database_subnets" {
  description = "데이터 계층용 서브넷 리스트"
  value       = module.vpc.database_subnets
}

output "redis_endpoint" {
  description = "Redis 클러스터의 엔드포인트"
  value       = try(module.data.redis_endpoint, "pending")
}

output "redis_security_group_id" {
  description = "Redis 보안 그룹 ID (나중에 EKS 노드 허용 규칙 추가용)"
  value       = module.data.redis_security_group_id
}

output "rds_proxy_endpoint" {
  description = "DB Connection String for Backend Application (RDS Proxy)"
  value       = try(module.data.rds_proxy_endpoint, "pending")
}

output "queue_url" {
  description = "SQS 대기열 URL"
  value       = try(module.queue.queue_url, "pending")
}

output "ai_vqa_bucket_name" {
  description = "AI 팀용 VQA S3 버킷 이름"
  value       = aws_s3_bucket.ai_vqa_data.id
}

# RDS Proxy 보안 그룹 ID 추가 (나중에 EKS -> RDS Proxy 통신용)
output "rds_proxy_security_group_id" {
  description = "RDS Proxy 보안 그룹 ID"
  value       = module.data.rds_proxy_security_group_id
}