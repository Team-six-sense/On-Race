# 1. EKS 노드 -> AI 모델 EC2 접근 허용 규칙
# [보안 권장] VPC 전체(CIDR)보다는 EKS 노드 보안 그룹 ID를 직접 참조하는 것이 좋으나, 
# 현재 통신 우선순위에 따라 CIDR 대역을 유지합니다.
resource "aws_security_group_rule" "eks_to_ai_model" {
  type              = "ingress"
  from_port         = 8000
  to_port           = 8000
  protocol          = "tcp"
  security_group_id = data.terraform_remote_state.base.outputs.ai_model_sg_id
  cidr_blocks       = [data.terraform_remote_state.base.outputs.vpc_cidr]
}

# 2. 최신 Amazon Linux 2023 AMI 조회
# base 계층과 중복될 수 있으나, app 계층의 독립적 인스턴스 생성을 위해 유지합니다.
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

# 3. AI 모델 인스턴스 (A/B 분산 배치)
resource "aws_instance" "ai_macro_detector" {
  count         = 2
  ami           = data.aws_ami.al2023.id
  instance_type = "t3.medium"
  
  # 가용 영역 분산을 위해 base의 private 서브넷 활용
  subnet_id              = data.terraform_remote_state.base.outputs.private_subnets[count.index % length(data.terraform_remote_state.base.outputs.private_subnets)]
  vpc_security_group_ids = [data.terraform_remote_state.base.outputs.ai_model_sg_id]

  # [중요] IAM 인스턴스 프로파일 연결
  iam_instance_profile = data.terraform_remote_state.base.outputs.ai_ec2_instance_profile_name

  # [SSH 접속용] 1단계에서 생성한 SSH 키 페어 이름을 지정합니다.
  key_name = "t6-on-race-ai-macro-key"

  # SSM 및 AL2023 메타데이터 필수 설정
  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required" # 보안을 위해 IMDSv2만 사용하도록 권장
    http_put_response_hop_limit = 2          # 컨테이너 환경을 위해 홉 제한을 2로 설정
  }

  # [진단용] 강사님 확인 및 SSM 에러 분석을 위한 User Data
  # 인스턴스 생성 후 2분 뒤 시스템 로그(Console)에 SSM 에러를 뿌려줍니다.
  user_data = <<-EOF
    #!/bin/bash
    (
      sleep 120
      echo "=======================================================" > /dev/console
      echo "========= [SSM AGENT INTERNAL ERROR LOG DUMP] =========" > /dev/console
      echo "=======================================================" > /dev/console
      
      # SSM 에이전트 로그 추출
      if [ -f /var/log/amazon/ssm/amazon-ssm-agent.log ]; then
        tail -n 50 /var/log/amazon/ssm/amazon-ssm-agent.log > /dev/console
      else
        echo "SSM Agent log file not found." > /dev/console
      fi
      
      echo "------------------- [ERRORS ONLY] ---------------------" > /dev/console
      
      if [ -f /var/log/amazon/ssm/errors.log ]; then
        tail -n 50 /var/log/amazon/ssm/errors.log > /dev/console
      else
        echo "SSM Error log file not found." > /dev/console
      fi
      
      echo "=======================================================" > /dev/console
    ) &
  EOF

  tags = { 
    Name = "${var.project_name}-ai-macro-${count.index == 0 ? "a" : "b"}" 
    Team = "Macro"
  }
}

# # 4. AI 매크로 서버 복구 인스턴스 (수동 복구 및 진단용)
# # 이 인스턴스는 특정 AMI를 사용하여 수동으로 복구하거나 진단할 때 사용됩니다.
# # 필요에 따라 `terraform destroy`를 통해 수동으로 제거해야 합니다.
# resource "aws_instance" "ai_macro_recovery" {
#   # 사용자 요청에 따라 특정 AMI ID를 사용합니다.
#   ami           = "ami-08c66605d0632ecfb" # t6-on-race-ai-macro-backup-20260414
#   instance_type = "t3.medium"
#   
#   # 첫 번째 프라이빗 서브넷에 배치하여 SSM 연결을 용이하게 합니다.
#   subnet_id              = data.terraform_remote_state.base.outputs.private_subnets[0]
#   vpc_security_group_ids = [data.terraform_remote_state.base.outputs.ai_model_sg_id]
# 
#   # [중요] IAM 인스턴스 프로파일 연결 (SSM 접근 권한 포함)
#   iam_instance_profile = data.terraform_remote_state.base.outputs.ai_ec2_instance_profile_name
# 
#   # SSM 및 AL2023 메타데이터 필수 설정
#   metadata_options {
#     http_endpoint               = "enabled"
#     http_tokens                 = "required" # IMDSv2 강제
#     http_put_response_hop_limit = 2          # SSM 에이전트 인증 토큰 획득을 위한 홉 상향
#   }
# 
#   # [진단용] 강사님 확인 및 SSM 에러 분석을 위한 User Data
#   # 인스턴스 생성 후 2분 뒤 시스템 로그(Console)에 SSM 에러를 뿌려줍니다.
#   user_data = <<-EOF
#     #!/bin/bash
#     (
#       sleep 120
#       echo "=======================================================" > /dev/console
#       echo "========= [SSM AGENT INTERNAL ERROR LOG DUMP] =========" > /dev/console
#       echo "=======================================================" > /dev/console
#       if [ -f /var/log/amazon/ssm/amazon-ssm-agent.log ]; then
#         tail -n 50 /var/log/amazon/ssm/amazon-ssm-agent.log > /dev/console
#       else
#         echo "SSM Agent log file not found." > /dev/console
#       fi
#       echo "------------------- [ERRORS ONLY] ---------------------" > /dev/console
#       if [ -f /var/log/amazon/ssm/errors.log ]; then
#         tail -n 50 /var/log/amazon/ssm/errors.log > /dev/console
#       else
#         echo "SSM Error log file not found." > /dev/console
#       fi
#       echo "=======================================================" > /dev/console
#     ) &
#   EOF
# 
#   tags = { 
#     Name = "${var.project_name}-ai-macro-recovery" 
#     Team = "Macro-Recovery"
#   }
# }

# 5. 개발 인스턴스 복구용 보안 그룹
resource "aws_security_group" "dev_instance_sg" {
  name        = "${var.project_name}-dev-instance-sg"
  description = "Allow SSH access to development instance"
  vpc_id      = data.terraform_remote_state.base.outputs.vpc_id

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    # [보안 강화] SSH로 배스천 호스트에 접속할 현재 컴퓨터의 공인 IP 주소를 입력하세요.
    cidr_blocks = ["203.0.113.5/32"] # 중요: 이 IP를 실제 접속할 PC의 공인 IP로 반드시 변경해야 합니다.
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-dev-instance-sg"
    Team = "Dev"
  }
}

# 6. 개발 인스턴스 복구 (수동 복구 및 진단용)
resource "aws_instance" "dev_instance_recovery" {
  ami           = "ami-032ccb610acf08329" # t6-on-race-dev-instance
  instance_type = "t3.small"
  
  # 첫 번째 퍼블릭 서브넷에 배치
  subnet_id                   = data.terraform_remote_state.base.outputs.public_subnets[0]
  vpc_security_group_ids      = [aws_security_group.dev_instance_sg.id]
  associate_public_ip_address = true # 퍼블릭 IP 자동 할당 활성화


  tags = {
    Name = "${var.project_name}-dev-instance-recovery"
    Team = "Dev"
  }
}

# 4. Cloud Map 서비스 디스커버리 (내부 통신용 DNS)

# 7. 배스천 호스트 -> AI 인스턴스 SSH 접속 허용 규칙
resource "aws_security_group_rule" "allow_ssh_from_bastion_to_ai" {
  type                     = "ingress"
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
  security_group_id        = data.terraform_remote_state.base.outputs.ai_model_sg_id
  source_security_group_id = aws_security_group.dev_instance_sg.id
  description              = "Allow SSH from bastion host (dev_instance_recovery)"
}
resource "aws_service_discovery_private_dns_namespace" "on_race" {
  name        = "on-race.local"
  description = "On-Race 내부 서비스 디스커버리용"
  vpc         = data.terraform_remote_state.base.outputs.vpc_id
}

resource "aws_service_discovery_service" "ai_service" {
  name = "ai-model"

  dns_config {
    namespace_id = aws_service_discovery_private_dns_namespace.on_race.id
    
    dns_records {
      ttl  = 10
      type = "A"
    }

    routing_policy = "MULTIVALUE"
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

# 생성된 AI 인스턴스를 서비스에 자동 등록
resource "aws_service_discovery_instance" "ai_instances" {
  count       = 2
  instance_id = aws_instance.ai_macro_detector[count.index].id
  service_id  = aws_service_discovery_service.ai_service.id

  attributes = {
    AWS_INSTANCE_IPV4 = aws_instance.ai_macro_detector[count.index].private_ip
    AWS_INSTANCE_PORT = "8000"
  }
}

# 복구 인스턴스는 서비스 디스커버리에 등록하지 않습니다.
# 만약 복구 인스턴스도 서비스 디스커버리에 등록해야 한다면 아래 주석을 해제하고 사용하세요.
# resource "aws_service_discovery_instance" "ai_recovery_instance" {
#   instance_id = aws_instance.ai_macro_recovery.id
#   service_id  = aws_service_discovery_service.ai_service.id

#   attributes = {
#     AWS_INSTANCE_IPV4 = aws_instance.ai_macro_recovery.private_ip
#     AWS_INSTANCE_PORT = "8000"
#   }
# }