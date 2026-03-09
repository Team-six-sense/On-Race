terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # 인프라 팀 식별자 t6 적용
  backend "s3" {
    bucket       = "t6-on-race-terraform-state-prod" # 수정됨
    key          = "prod/terraform.tfstate"
    region       = "ap-northeast-2"
    encrypt      = true
    use_lockfile = true 
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      # var.project_name이 "t6-on-race"라면 자동으로 적용됩니다.
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}