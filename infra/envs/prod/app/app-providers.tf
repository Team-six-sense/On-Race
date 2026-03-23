# C:\Users\김서진\On-Race\infra\envs\prod\app\app-providers.tf

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    # [보완] app 계층은 Helm 배포가 있으므로 반드시 포함
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.12"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.24"
    }
  }

  # [보완] S3 백엔드 설정 (app 전용 경로)
  backend "s3" {
    bucket       = "t6-on-race-terraform-state-prod"
    key          = "prod/app/terraform.tfstate" # app 전용 경로로 수정
    region       = "ap-northeast-2"
    encrypt      = true
    use_lockfile = true 
  }
}

provider "aws" {
  region = "ap-northeast-2"

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Layer       = "App" # 계층 식별 추가
    }
  }
}

# 참고: kubernetes와 helm 프로바이더 본체 설정은 
# 서진님의 app-main.tf 내부에 이미 모듈의 출력값을 참조하도록 잘 작성되어 있습니다.