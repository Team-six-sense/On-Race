# C:\Users\김서진\On-Race\infra\envs\prod\app\app-main.tf

data "terraform_remote_state" "base" {
  backend = "s3"
  config = {
    bucket = "t6-on-race-terraform-state-prod"
    key    = "prod/base/terraform.tfstate"
    region = "ap-northeast-2"
  }
}
