# [가용성 개선] 각 가용 영역(AZ)에 EC2 Instance Connect Endpoint를 생성하여 모든 프라이빗 인스턴스에 안정적으로 접속할 수 있도록 합니다.
resource "aws_ec2_instance_connect_endpoint" "az_a" {
  subnet_id          = module.vpc.private_subnets[0]
  # vpc 모듈에서 생성된 공용 엔드포인트 보안 그룹을 재사용합니다.
  security_group_ids = [module.vpc.vpc_endpoint_sg_id]

  tags = {
    Name = "${var.project_name}-eic-endpoint-a"
  }
}

resource "aws_ec2_instance_connect_endpoint" "az_b" {
  subnet_id          = module.vpc.private_subnets[1]
  security_group_ids = [module.vpc.vpc_endpoint_sg_id]

  tags = {
    Name = "${var.project_name}-eic-endpoint-b"
  }
}