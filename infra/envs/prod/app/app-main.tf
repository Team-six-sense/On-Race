data "terraform_remote_state" "base" {
  backend = "s3"
  config = {
    bucket = "t6-on-race-terraform-state-prod"
    key    = "prod/base/terraform.tfstate"
    region = "ap-northeast-2"
  }
}

resource "aws_security_group_rule" "eks_to_redis" {
  type                     = "ingress"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.redis_security_group_id
  source_security_group_id = module.eks.node_security_group_id
}