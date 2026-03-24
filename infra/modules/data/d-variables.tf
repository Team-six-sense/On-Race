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