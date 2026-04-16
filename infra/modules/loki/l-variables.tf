variable "project_name" {
  description = "프로젝트 명"
  type        = string
}

variable "environment" {
  description = "배포 환경 (prod, dev 등)"
  type        = string
}

variable "cluster_name" {
  description = "EKS 클러스터 이름"
  type        = string
}

variable "oidc_provider_arn" {
  description = "EKS OIDC Provider ARN"
  type        = string
}