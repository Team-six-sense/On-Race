terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws        = { source = "hashicorp/aws", version = "~> 5.0" }
    helm       = { source = "hashicorp/helm", version = "~> 2.12" }
    kubernetes = { source = "hashicorp/kubernetes", version = "~> 2.24" }
    time       = { source = "hashicorp/time", version = "~> 0.9.1" }
  }

  backend "s3" {
    bucket       = "t6-on-race-terraform-state-prod"
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

# 1. 테라폼 내장 AWS 프로바이더를 통해 안전하게 EKS 인증 토큰 발급
data "aws_eks_cluster_auth" "eks" {
  name = module.eks.cluster_name
}

# 2. Kubernetes 프로바이더 (exec 블록 제거, token 직접 주입)
provider "kubernetes" {
  host                   = module.eks.cluster_endpoint
  cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
  token                  = data.aws_eks_cluster_auth.eks.token
}

# 3. Helm 프로바이더 (exec 블록 제거, token 직접 주입)
provider "helm" {
  kubernetes {
    host                   = module.eks.cluster_endpoint
    cluster_ca_certificate = base64decode(module.eks.cluster_certificate_authority_data)
    token                  = data.aws_eks_cluster_auth.eks.token
  }
}