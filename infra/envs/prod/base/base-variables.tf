# C:\Users\김서진\On-Race\infra\envs\prod\base\base-variables.tf

variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "프로젝트 명 (팀 식별자 t6 포함)"
  type        = string
  default     = "t6-on-race"
}

variable "environment" {
  description = "배포 환경 (dev/prod)"
  type        = string
  default     = "prod"
}

variable "vpc_cidr" {
  description = "VPC 전체 CIDR 대역"
  type        = string
  default     = "10.0.0.0/16"
}

variable "azs" {
  description = "사용할 가용 영역 리스트"
  type        = list(string)
  default     = ["ap-northeast-2a", "ap-northeast-2c"]
}

variable "public_subnets" {
  description = "ALB, NAT Gateway용 퍼블릭 서브넷"
  type        = list(string)
  default     = ["10.0.101.0/24", "10.0.102.0/24"]
}

variable "private_subnets" {
  description = "EKS Node, SQS Worker용 프라이빗 서브넷"
  type        = list(string)
  default     = ["10.0.16.0/20", "10.0.32.0/20"]
}

variable "database_subnets" {
  description = "RDS, Redis용 데이터베이스 서브넷"
  type        = list(string)
  default     = ["10.0.201.0/24", "10.0.202.0/24"]
}

variable "single_nat_gateway" {
  description = "단일 NAT Gateway 사용 여부"
  type        = bool
  default     = true
}