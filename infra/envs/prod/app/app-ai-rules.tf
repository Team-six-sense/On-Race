# EKS 노드 -> AI 모델 EC2 접근 허용 규칙
resource "aws_security_group_rule" "eks_to_ai_model" {
  type                     = "ingress"
  from_port                = 8000
  to_port                  = 8000
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.ai_model_sg_id
  source_security_group_id = module.eks.node_security_group_id
}
/*
# [수정] 최신 Amazon Linux 2023 AMI를 동적으로 가져오기
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

# AI 모델용 보안 그룹
resource "aws_security_group" "ai_model_sg" {
  name   = "${var.project_name}-ai-model-sg"
  vpc_id = data.terraform_remote_state.base.outputs.vpc_id

  ingress {
    from_port       = 8000 # AI 모델 API 포트 (AI 팀에 최종 확인 필요)
    to_port         = 8000
    protocol        = "tcp"
    security_groups = [module.eks.node_security_group_id] # EKS 노드에서만 접근 허용
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# AI 모델 인스턴스 2대 (A/B 분산)
resource "aws_instance" "ai_macro_detector" {
  count         = 2
  ami           = data.aws_ami.al2023.id # [수정] 데이터 소스에서 가져온 ID 참조
  instance_type = "t3.medium"
  
  # 가용 영역 분산을 위해 base의 서브넷 리스트 활용
  subnet_id     = data.terraform_remote_state.base.outputs.private_subnets[count.index]
  vpc_security_group_ids = [aws_security_group.ai_model_sg.id]

  # [추가] 생성한 IAM 인스턴스 프로파일 연결
  iam_instance_profile = aws_iam_instance_profile.ai_ec2_profile.name

  tags = { 
    Name = "${var.project_name}-ai-macro-${count.index == 0 ? "a" : "b"}" 
  }
}
*/