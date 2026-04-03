output "ecr_repository_url" {
  value       = data.terraform_remote_state.base.outputs.ecr_repository_url
  description = "base 계층에서 생성된 ECR 리포지토리 URL (Relay)"
}

output "github_actions_role_arn" {
  value       = data.terraform_remote_state.base.outputs.github_actions_role_arn
  description = "GitHub Actions Role ARN (Relay)"
}