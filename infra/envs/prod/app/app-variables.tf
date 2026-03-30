# C:\Users\김서진\On-Race\infra\envs\prod\app\app-variables.tf

variable "aws_region" {
  description = "AWS 리전 (공통 적용)"
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "프로젝트 명 (t6-on-race)"
  type        = string
  default     = "t6-on-race"
}

variable "environment" {
  description = "배포 환경 (prod)"
  type        = string
  default     = "prod"
}

variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
  default     = "t6-on-race-prod" # 실제 사용 중인 네임스페이스 명칭으로 수정
}