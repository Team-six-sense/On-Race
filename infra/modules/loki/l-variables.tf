variable "project_name" {
  description = "프로젝트 명 (t6-on-race)"
  type        = string
}

variable "cluster_name" {
  description = "EKS 클러스터 이름"
  type        = string
}

variable "oidc_provider_arn" {
  description = "EKS OIDC Provider ARN (IRSA용)"
  type        = string
}