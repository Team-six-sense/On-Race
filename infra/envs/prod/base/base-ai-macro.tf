# [데이터] 최신 Amazon Linux 2023 AMI 동적 조회
data "aws_ami" "al2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-x86_64"]
  }
}

# 1. EC2가 사용할 IAM 역할 (Role)
resource "aws_iam_role" "ai_ec2_role" {
  name = "${var.project_name}-ai-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })
}

# 2. EC2 인스턴스 프로파일
resource "aws_iam_instance_profile" "ai_ec2_profile" {
  name = "${var.project_name}-ai-ec2-profile"
  role = aws_iam_role.ai_ec2_role.name
}

# 3. AI VQA S3 버킷 접근 권한 정책 (Inline Policy)
resource "aws_iam_role_policy" "ai_ec2_s3_policy" {
  name = "${var.project_name}-ai-ec2-s3-policy"
  role = aws_iam_role.ai_ec2_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:ListBucket"
        ]
        Resource = [
          aws_s3_bucket.ai_vqa_data.arn,
          "${aws_s3_bucket.ai_vqa_data.arn}/*"
        ]
      }
    ]
  })
}

# 4. AI 모델용 보안 그룹 (껍데기)
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

# CloudFront S3 접근 제어(OAC) 리소스 추가
resource "aws_cloudfront_origin_access_control" "ai_vqa" {
  name                              = "${var.project_name}-vqa-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}