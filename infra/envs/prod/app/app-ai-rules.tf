# EKS 노드 -> AI 모델 EC2 접근 허용 규칙
resource "aws_security_group_rule" "eks_to_ai_model" {
  type                     = "ingress"
  from_port                = 8000
  to_port                  = 8000
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.ai_model_sg_id
  source_security_group_id = module.eks.node_security_group_id
}

# 최신 Amazon Linux 2023 AMI를 동적으로 가져오기
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

# AI 모델 인스턴스 2대 (A/B 분산)
resource "aws_instance" "ai_macro_detector" {
  count         = 2
  ami           = data.aws_ami.al2023.id
  instance_type = "t3.medium"
  #instance_type = "t3.medium"
  
  # 가용 영역 분산을 위해 base의 서브넷 리스트 활용
  subnet_id              = data.terraform_remote_state.base.outputs.private_subnets[count.index % length(data.terraform_remote_state.base.outputs.private_subnets)]
  vpc_security_group_ids = [data.terraform_remote_state.base.outputs.ai_model_sg_id]

  # [수정 완료] Base 계층의 Output 참조
  iam_instance_profile = data.terraform_remote_state.base.outputs.ai_ec2_instance_profile_name

  tags = { 
    Name = "${var.project_name}-ai-macro-${count.index == 0 ? "a" : "b"}" 
    # [핵심 추가] IAM 정책(Condition)과 매칭시키기 위해 반드시 필요합니다.
    Team = "Macro"
  }
}