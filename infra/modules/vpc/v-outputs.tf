output "vpc_id" {
  description = "생성된 VPC의 ID"
  value       = aws_vpc.this.id
}

output "vpc_cidr" {
  description = "VPC의 CIDR 블록 (보안 그룹 규칙 설정 시 참조)"
  value       = aws_vpc.this.cidr_block
}

output "private_subnets" {
  description = "프라이빗 서브넷 ID 리스트 (EKS, Worker Nodes 등에서 사용)"
  value       = aws_subnet.private[*].id
}

output "public_subnets" {
  description = "퍼블릭 서브넷 ID 리스트 (ALB, NAT Gateway 등에서 사용)"
  value       = aws_subnet.public[*].id
}

output "database_subnets" {
  description = "데이터베이스 서브넷 ID 리스트 (RDS, Redis 등에서 사용)"
  value       = aws_subnet.database[*].id
}

output "vpc_endpoint_sg_id" {
  description = "VPC Endpoint 전용 보안 그룹 ID"
  value       = aws_security_group.vpc_endpoint.id
}

output "nat_public_ips" {
  description = "외부 통신 시 사용되는 NAT Gateway의 퍼블릭 IP (외부 화이트리스팅용)"
  value       = aws_eip.nat[*].public_ip
}

# S3 Gateway Endpoint ID 출력
output "s3_gateway_endpoint_id" {
  description = "S3 Gateway Endpoint의 ID"
  value       = aws_vpc_endpoint.s3.id
}