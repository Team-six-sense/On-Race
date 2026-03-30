variable "project_name" {
  description = "프로젝트 이름 (예: on-race)"
  type        = string
}

variable "environment" {
  description = "배포 환경 (예: prod, dev, staging)"
  type        = string
}

variable "vpc_cidr" {
  description = "VPC 전체 CIDR 블록 (예: 10.0.0.0/16)"
  type        = string
}

variable "azs" {
  description = "사용할 가용 영역(Availability Zones) 리스트"
  type        = list(string)
}

variable "public_subnets" {
  description = "퍼블릭 서브넷 CIDR 리스트 (ALB 및 NAT Gateway용)"
  type        = list(string)
}

variable "private_subnets" {
  description = "프라이빗 서브넷 CIDR 리스트 (EKS 노드/파드용). 700~1000개 파드 수용을 위해 각 서브넷당 최소 /22(1,024개 IP) 이상 권장"
  type        = list(string)
}

variable "database_subnets" {
  description = "데이터베이스 전용 서브넷 CIDR 리스트 (RDS, Redis용)"
  type        = list(string)
}

variable "single_nat_gateway" {
  description = "비용 절감을 위해 단일 NAT Gateway만 사용할지 여부"
  type        = bool
  default     = false
}