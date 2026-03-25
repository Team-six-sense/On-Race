# C:\Users\김서진\On-Race\infra\envs\prod\app\app-main.tf

data "terraform_remote_state" "base" {
  backend = "s3"
  config = {
    bucket = "t6-on-race-terraform-state-prod"
    key    = "prod/base/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

# [최적화 1] EKS 노드 -> Redis 접속 허용 규칙
resource "aws_security_group_rule" "eks_to_redis" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.redis_security_group_id
  source_security_group_id = module.eks.node_security_group_id
}

# [최적화 4] EKS 노드 -> RDS Proxy 접속 허용 규칙
resource "aws_security_group_rule" "eks_to_rds_proxy" {
  type                     = "ingress"
  from_port                = 3306
  to_port                  = 3306
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.rds_proxy_security_group_id
  source_security_group_id = module.eks.node_security_group_id
}