output "redis_endpoint" {
  value = aws_elasticache_replication_group.this.primary_endpoint_address
}

# RDS Proxy 엔드포인트
output "rds_proxy_endpoint" {
  value       = aws_db_proxy.this.endpoint
  description = "The endpoint of the RDS Proxy (Application should connect here)"
}

# 원본 RDS 엔드포인트 (관리용)
output "rds_endpoint" {
  value       = aws_db_instance.this.endpoint
  description = "The endpoint of the raw RDS instance"
}