variable "aws_region" {
  description = "AWS 리전"
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "프로젝트 명 (팀 식별자 t6 포함)"
  type        = string
  # [수정] 기본값 자체를 t6-on-race로 설정하여 모든 리소스에 전파
  default     = "t6-on-race" 
}

variable "environment" {
  description = "배포 환경 (dev/prod)"
  type        = string
  default     = "prod"
}

variable "vpc_cidr" {
  description = "VPC 전체 CIDR"
  type        = string
  default     = "10.0.0.0/16"
}

variable "azs" {
  description = "사용할 가용 영역"
  type        = list(string)
  default     = ["ap-northeast-2a", "ap-northeast-2c"]
}

variable "private_subnets" {
  description = "EKS Node, SQS Worker 용 프라이빗 서브넷"
  type        = list(string)
  default     = ["10.0.16.0/20", "10.0.32.0/20"]
}

variable "public_subnets" {
  description = "ALB, NAT Gateway 용 퍼블릭 서브넷"
  type        = list(string)
  default     = ["10.0.101.0/24", "10.0.102.0/24"]
}

variable "database_subnets" {
  description = "RDS, Redis 용 데이터베이스 서브넷"
  type        = list(string)
  default     = ["10.0.201.0/24", "10.0.202.0/24"]
}