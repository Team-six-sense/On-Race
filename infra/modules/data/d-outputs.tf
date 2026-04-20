# [infra/modules/data/outputs.tf]

# Redis 관련
output "redis_endpoint" {
  value = aws_elasticache_replication_group.this.primary_endpoint_address
}

output "redis_security_group_id" {
  value = aws_security_group.redis.id # main.tf의 'redis' 보안 그룹 참조
}

# RDS Proxy 관련
output "rds_proxy_endpoint" {
  value       = aws_db_proxy.this.endpoint
  description = "The endpoint of the RDS Proxy"
}

output "rds_proxy_security_group_id" {
  value = aws_security_group.rds_proxy.id # main.tf의 'rds_proxy' 보안 그룹 참조
}

# RDS 원본 (관리용)
output "rds_endpoint" {
  value       = aws_db_instance.this.endpoint
  description = "The endpoint of the raw RDS instance"
}

output "redis_sg_id" {
  description = "Redis 보안 그룹 ID"
  value       = aws_security_group.redis.id
}

output "rds_proxy_sg_id" {
  description = "RDS Proxy 보안 그룹 ID"
  value       = aws_security_group.rds_proxy.id
}