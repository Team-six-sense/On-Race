# ==========================================================================
# 1. 네트워크 계층 (VPC) 관련 출력
# ==========================================================================
output "vpc_id" {
  description = "생성된 VPC의 ID"
  value       = module.vpc.vpc_id
}

output "vpc_cidr" {
  description = "VPC의 CIDR 블록 (App 계층 보안 그룹 설정 시 참조)"
  value       = module.vpc.vpc_cidr
}

output "private_subnets" {
  description = "EKS 노드 및 파드가 배치될 프라이빗 서브넷 리스트"
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

output "vpc_endpoint_sg_id" {
  description = "VPC Interface Endpoint 전용 보안 그룹 ID"
  value       = module.vpc.vpc_endpoint_sg_id
}

output "nat_public_ips" {
  description = "외부 통신 시 사용되는 NAT Gateway의 퍼블릭 IP 리스트"
  value       = module.vpc.nat_public_ips
}

# ==========================================================================
# 2. 데이터 계층 (Redis, RDS Proxy) 관련 출력
# ==========================================================================
output "redis_endpoint" {
  description = "Redis 클러스터의 엔드포인트"
  value       = try(module.data.redis_endpoint, "pending")
}

output "redis_sg_id" {
  description = "Redis 보안 그룹 ID (App 계층 참조용)"
  value       = module.data.redis_sg_id
}

output "rds_proxy_endpoint" {
  description = "RDS Proxy 엔드포인트 (백엔드 애플리케이션 접속용)"
  value       = try(module.data.rds_proxy_endpoint, "pending")
}

output "rds_proxy_sg_id" {
  description = "RDS Proxy 보안 그룹 ID (App 계층 참조용)"
  value       = module.data.rds_proxy_sg_id
}

# ==========================================================================
# 3. 기타 자원 관련 출력
# ==========================================================================
output "queue_url" {
  description = "SQS 대기열 URL"
  value       = try(module.queue.queue_url, "pending")
}

output "ai_vqa_bucket_name" {
  description = "AI 팀용 VQA S3 버킷 이름"
  value       = aws_s3_bucket.ai_vqa_data.id
}

output "ai_model_sg_id" {
  description = "AI 매크로 탐지 EC2 보안 그룹 ID"
  value       = aws_security_group.ai_model_sg.id
}

output "ai_ec2_instance_profile_name" {
  description = "AI EC2용 IAM 인스턴스 프로파일 이름 (App 계층 인스턴스 생성 시 참조)"
  value       = aws_iam_instance_profile.ai_ec2_profile.name
}

output "ai_vqa_bucket_id" {
  value = aws_s3_bucket.ai_vqa_data.id
}

output "ai_vqa_bucket_arn" {
  value = aws_s3_bucket.ai_vqa_data.arn
}

output "ai_vqa_bucket_domain_name" {
  value = aws_s3_bucket.ai_vqa_data.bucket_regional_domain_name
}

output "ecr_repository_url" {
  description = "ECR 리포지토리 URL"
  value       = aws_ecr_repository.app_repo.repository_url
}

output "ecr_repository_arn" {
  description = "ECR 리포지토리 ARN"
  value       = aws_ecr_repository.app_repo.arn
}

output "github_actions_role_arn" {
  description = "ARN of the IAM role for GitHub Actions"
  value       = aws_iam_role.github_actions_ecr_role.arn
}