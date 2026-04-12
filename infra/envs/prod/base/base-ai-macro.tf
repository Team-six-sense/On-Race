# 1. AI 인스턴스 전용 IAM 역할 (기존 중복 리소스 통합)
resource "aws_iam_role" "ai_ec2_role" {
  name = "${var.project_name}-ai-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

# 2. EC2 인스턴스 프로파일 (app 계층에서 참조)
resource "aws_iam_instance_profile" "ai_ec2_profile" {
  name = "${var.project_name}-ai-ec2-profile"
  role = aws_iam_role.ai_ec2_role.name
}

# 3. AI VQA S3 버킷 접근 권한 정책 (Inline)
resource "aws_iam_role_policy" "ai_ec2_s3_policy" {
  name = "${var.project_name}-ai-ec2-s3-policy"
  role = aws_iam_role.ai_ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = ["s3:GetObject", "s3:PutObject", "s3:ListBucket"]
      Resource = [
        aws_s3_bucket.ai_vqa_data.arn,
        "${aws_s3_bucket.ai_vqa_data.arn}/*"
      ]
    }]
  })
}

# 4. ⭐ SSM 연결을 위한 필수 관리형 정책 연결 (중복 제거 후 통합)
resource "aws_iam_role_policy_attachment" "ai_ec2_ssm_core" {
  role       = aws_iam_role.ai_ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# 5. AI 모델용 보안 그룹
resource "aws_security_group" "ai_model_sg" {
  name   = "${var.project_name}-ai-model-sg"
  vpc_id = module.vpc.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-ai-model-sg" }
}

# 6. CloudFront OAC 설정
resource "aws_cloudfront_origin_access_control" "ai_vqa" {
  name                              = "${var.project_name}-vqa-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# SSM 세션 매니저 접속을 위한 핵심 정책 연결
resource "aws_iam_role_policy_attachment" "ai_ec2_ssm_policy" {
  role       = aws_iam_role.ai_ec2_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}


# EIC 엔드포인트를 통한 SSH 접속 허용 규칙
resource "aws_security_group_rule" "allow_ssh_from_eic" {
  type                     = "ingress"
  from_port                = 22
  to_port                  = 22
  protocol                 = "tcp"
  security_group_id        = aws_security_group.ai_model_sg.id
  # 엔드포인트 보안 그룹을 소스로 지정하여 보안을 강화합니다. [cite: 8182]
  source_security_group_id = aws_security_group.vpc_endpoints_sg.id 
}