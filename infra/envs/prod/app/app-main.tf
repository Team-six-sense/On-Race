# C:\Users\김서진\On-Race\infra\envs\prod\app\app-main.tf

# 1. 원격 상태 데이터 (Base 계층 참조)
data "terraform_remote_state" "base" {
  backend = "s3"
  config = {
    bucket = "t6-on-race-terraform-state-prod"
    key    = "prod/base/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

# 2. 공통 데이터 소스 (중복 제거의 핵심)
data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

# 3. 공통 네임스페이스 생성
resource "kubernetes_namespace_v1" "app" {
  metadata {
    name = "${var.project_name}-${var.environment}"
  }
}