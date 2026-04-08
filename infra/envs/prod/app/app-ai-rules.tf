# EKS 노드 -> AI 모델 EC2 접근 허용 규칙
resource "aws_security_group_rule" "eks_to_ai_model" {
  type              = "ingress"
  from_port         = 8000
  to_port           = 8000
  protocol          = "tcp"
  security_group_id = data.terraform_remote_state.base.outputs.ai_model_sg_id
  cidr_blocks       = [data.terraform_remote_state.base.outputs.vpc_cidr] # [수정] 보안 그룹 ID 대신 VPC CIDR 대역 사용
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
    Team = "Macro"
  }
}

# [추가] 1. Cloud Map 프라이빗 DNS 네임스페이스 생성
resource "aws_service_discovery_private_dns_namespace" "on_race" {
  name        = "on-race.local"
  description = "On-Race 내부 서비스 디스커버리용"
  vpc         = data.terraform_remote_state.base.outputs.vpc_id
}

# [추가] 2. AI 모델 전용 서비스 정의
resource "aws_service_discovery_service" "ai_service" {
  name = "ai-model"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.on_race.id
    
    dns_records {
      ttl  = 10
      type = "A" # 다중 인스턴스 IP를 A 레코드로 관리
    }

    # 다중 응답 설정: 조회 시 살아있는 모든 IP를 반환합니다.
    routing_policy = "MULTIVALUE"
  }

  # 인스턴스 상태 확인 설정 (선택 사항이나 권장)
  health_check_custom_config {
    failure_threshold = 1
  }
}

# [추가] 3. 생성된 AI 인스턴스(count=2)를 서비스에 등록
resource "aws_service_discovery_instance" "ai_instances" {
  count       = 2
  instance_id = aws_instance.ai_macro_detector[count.index].id
  service_id  = aws_service_discovery_service.ai_service.id

  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.ai_macro_detector[count.index].private_ip
    # [주의] AI 모델의 포트가 8000이라면 명시
    AWS_INSTANCE_PORT = "8000"
  }
}