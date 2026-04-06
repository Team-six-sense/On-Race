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

# 깃허브 액션에서 주입하는 이미지 SHA 값을 담는 변수
variable "image_tag" {
  description = "ECR에 푸시된 고유 이미지 태그 (Git SHA)"
  type        = string
  default     = "latest" # 수동 실행 시를 위한 기본값
}

variable "namespace" {
  description = "Kubernetes namespace"
  type        = string
  default     = "t6-on-race-prod"
}