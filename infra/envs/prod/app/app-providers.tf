terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws        = { source = "hashicorp/aws", version = "~> 5.0" }
    helm       = { source = "hashicorp/helm", version = "~> 2.12" }
    kubernetes = { source = "hashicorp/kubernetes", version = "~> 2.24" }
    # [추가] kubernetes_manifest의 plan-time 검증 한계를 우회하기 위한 프로바이더
    kubectl = {
      source  = "gavinbunney/kubectl"
      version = ">= 1.14.0"
    }
    time = { source = "hashicorp/time", version = "~> 0.9.1" }
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

# [추가] kubectl 프로바이더 설정
# Plan 단계에서 접속을 시도하지 않고, Apply 단계에서만 연결을 수행합니다.
provider "kubectl" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
  load_config_file       = false

  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", module.eks.cluster_name, "--region", var.aws_region]
  }
}

# 깃액션 환경에서 가장 안정적인 인증 방식 (Native Exec)
# 테라폼이 K8s, Helm 작업을 수행할 때마다 AWS CLI를 통해 실시간으로 토큰을 발급받습니다.
provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)

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