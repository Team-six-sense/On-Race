output "vpc_id" {
  description = "생성된 VPC의 ID"
  value       = aws_vpc.this.id
}

output "private_subnets" {
  description = "프라이빗 서브넷 ID 리스트 (EKS 등에서 사용)"
  value       = aws_subnet.private[*].id
}

output "public_subnets" {
  description = "퍼블릭 서브넷 ID 리스트"
  value       = aws_subnet.public[*].id
}

output "database_subnets" {
  description = "데이터베이스 서브넷 ID 리스트 (Redis 등에서 사용)"
  value       = aws_subnet.database[*].id
}