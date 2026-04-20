locals {
  # 1. Secrets Manager에서 DB 암호 가져오기
  # 이 데이터 소스는 app-api.tf, app-auth.tf, app-queue.tf에서 공유됩니다.
  db_secret = data.aws_secretsmanager_secret.db_secret
  db_secret_val = data.aws_secretsmanager_secret_version.db_secret_val
  db_creds = jsondecode(data.aws_secretsmanager_secret_version.db_secret_val.secret_string)

  # 2. Redis 전용 비밀번호를 base 환경의 remote_state에서 가져옵니다.
  # 이 값은 app-api.tf, app-auth.tf, app-queue.tf에서 공유됩니다.
  redis_creds    = jsondecode(data.aws_secretsmanager_secret_version.redis_password_secret_val.secret_string)
  redis_password = local.redis_creds.password

  # 3. 공통 애플리케이션 비밀 값
  # 이 값은 모든 서비스에서 공유될 수 있습니다.
  common_secrets = jsondecode(data.aws_secretsmanager_secret_version.on_race_common_secrets_val.secret_string)
}

# Secrets Manager에서 DB 암호 가져오기 (locals에서 참조하기 위해 여기에 정의)
data "aws_secretsmanager_secret" "db_secret" {
  name = "${var.project_name}-${var.environment}-db-password-v4"
}
data "aws_secretsmanager_secret_version" "db_secret_val" {
  secret_id = data.aws_secretsmanager_secret.db_secret.id
}

# Secrets Manager에서 Redis 암호 가져오기
data "aws_secretsmanager_secret" "redis_password_secret" {
  name = "${var.project_name}-${var.environment}-redis-password"
}

data "aws_secretsmanager_secret_version" "redis_password_secret_val" {
  secret_id = data.aws_secretsmanager_secret.redis_password_secret.id
}

# Secrets Manager에서 공통 애플리케이션 비밀 값 가져오기
data "aws_secretsmanager_secret" "on_race_common_secrets" {
  name = "${var.project_name}-${var.environment}-common-secrets"
}

data "aws_secretsmanager_secret_version" "on_race_common_secrets_val" {
  secret_id = data.aws_secretsmanager_secret.on_race_common_secrets.id
}