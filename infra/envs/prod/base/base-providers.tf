# C:\Users\김서진\On-Race\infra\envs\prod\base\base-providers.tf

terraform {
  required_version = ">= 1.5.0" # 기존 버전 유지

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # [보완] S3 백엔드 설정 (base 전용 경로)
  backend "s3" {
    bucket       = "t6-on-race-terraform-state-prod"
    key          = "prod/base/terraform.tfstate" # base 전용 경로로 수정
    region       = "ap-northeast-2"
    encrypt      = true
    use_lockfile = true
  }
}

provider "aws" {
  region = "ap-northeast-2"

  # [보완] 공통 태그 적용 (t6-on-race 식별자)
  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Layer       = "Base" # 계층 식별 추가
    }
  }
}