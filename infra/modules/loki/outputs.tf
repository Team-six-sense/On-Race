output "loki_role_arn" {
  description = "Loki IRSA를 위한 IAM Role의 ARN"
  # 아래 리소스 이름(aws_iam_role.this)은 
  # infra/modules/loki/main.tf에 정의된 실제 이름과 일치해야 합니다.
  value       = module.loki_irsa.iam_role_arn
}