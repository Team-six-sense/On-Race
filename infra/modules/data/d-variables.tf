variable "project_name" { type = string }
variable "environment" { type = string }
variable "vpc_id" { type = string }
variable "database_subnets" { type = list(string) }
variable "redis_node_type" { type = string }
variable "automatic_failover_enabled" { type = bool }
variable "num_cache_clusters" { type = number }
variable "eks_node_security_group_id" {
  description = "EKS 노드 보안 그룹 ID"
  type        = string
  default     = null
}

variable "db_password" {
  description = "RDS 마스터 암호"
  type        = string
  sensitive   = true # 플랜 출력 시 암호 숨김
}

variable "db_secret_arn" {
  description = "Secrets Manager ARN (RDS Proxy용)"
  type        = string
}