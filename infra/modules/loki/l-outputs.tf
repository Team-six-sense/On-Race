# Loki 전용 IAM 역할(IRSA) ARN 출력
output "loki_role_arn" {
  description = "Loki 파드가 S3 접근을 위해 사용하는 IAM 역할 ARN"
  # [해결] aws_iam_role.loki 대신 실제 모듈에서 생성된 arn을 참조
  value       = module.loki_irsa.iam_role_arn
}

# Loki용 S3 버킷 이름 출력
output "loki_bucket_name" {
  description = "Loki 로그 데이터가 저장되는 S3 버킷 이름"
  value       = aws_s3_bucket.loki_logs.id
}