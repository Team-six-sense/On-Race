variable "project_name" {
  description = "프로젝트 명"
  type        = string
}

variable "environment" {
  description = "배포 환경 (dev/prod)"
  type        = string
}

variable "vpc_cidr" {
  description = "VPC CIDR 블록"
  type        = string
}

variable "azs" {
  description = "사용할 가용 영역 리스트"
  type        = list(string)
}

variable "public_subnets" {
  description = "퍼블릭 서브넷 CIDR 리스트"
  type        = list(string)
}

variable "private_subnets" {
  description = "프라이빗 서브넷 CIDR 리스트"
  type        = list(string)
}

variable "database_subnets" {
  description = "데이터베이스 서브넷 CIDR 리스트"
  type        = list(string)
}

variable "single_nat_gateway" {
  description = "단일 NAT Gateway 사용 여부 (true/false)"
  type        = bool
  default     = false
}