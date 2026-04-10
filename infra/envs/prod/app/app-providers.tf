terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws        = { source = "hashicorp/aws", version = "~> 5.0" }
    helm       = { source = "hashicorp/helm", version = "~> 2.12" }
    kubernetes = { source = "hashicorp/kubernetes", version = "~> 2.24" }
    time       = { source = "hashicorp/time", version = "~> 0.9.1" }
  }
  backend "s3" {
    bucket       = "t6-on-race-tfstate-916228846377-ap-northeast-2-an"
    key          = "prod/app/terraform.tfstate"
    region       = "ap-northeast-2"
    encrypt      = true
    use_lockfile = true
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "Terraform"
      Layer       = "App"
      Owner       = "t6-on-race"
    }
  }
}

# 깃액션 환경에서 가장 안정적인 인증 방식 (Native Exec)
# 테라폼이 K8s, Helm 작업을 수행할 때마다 AWS CLI를 통해 실시간으로 토큰을 발급받습니다.
provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
  
  # [핵심 추가] Server-Side Apply 활성화
  # 테라폼이 아닌 K8s API 서버가 필드 충돌을 관리하게 하여 Identity Change 오류를 원천 차단합니다.

  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name, "--region", var.aws_region]
  }
}

provider "helm" {
  kubernetes {
    host                   = module.eks.cluster_endpoint
    cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name, "--region", var.aws_region]
    }
  }
}