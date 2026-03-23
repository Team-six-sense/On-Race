# [Redis 영역] -----------------------------------------------------------

# Redis용 서브넷 그룹
resource "aws_elasticache_subnet_group" "this" {
  name       = "${var.project_name}-redis-subnet-group"
  subnet_ids = var.database_subnets
}

# Redis 보안 그룹 (내용 수정됨: 인라인 ingress 제거)
resource "aws_security_group" "redis" {
  name        = "${var.project_name}-redis-sg"
  vpc_id      = var.vpc_id

  # [수정] ingress 블록 삭제: EKS가 아직 없어 null 에러가 발생하므로, 
  # 나중에 app 계층에서 aws_security_group_rule로 따로 추가합니다.

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Redis 파라미터 그룹
resource "aws_elasticache_parameter_group" "this" {
  name   = "${var.project_name}-redis7-params"
  family = "redis7"

  parameter {
    name  = "maxmemory-policy"
    value = "volatile-lru"
  }
}

# Redis 클러스터
resource "aws_elasticache_replication_group" "this" {
  replication_group_id = "${var.project_name}-redis"
  description          = "Redis for On-Race TPS control and session"
  
  node_type            = var.redis_node_type
  port                 = 6379
  parameter_group_name = aws_elasticache_parameter_group.this.name
  
  automatic_failover_enabled = var.automatic_failover_enabled
  num_cache_clusters         = var.num_cache_clusters
  subnet_group_name          = aws_elasticache_subnet_group.this.name
  security_group_ids         = [aws_security_group.redis.id]

  engine          = "redis"
  engine_version  = "7.0"

  at_rest_encryption_enabled = true
  transit_encryption_enabled = true
  multi_az_enabled           = true
  apply_immediately          = true
}

# [RDS 영역] -------------------------------------------------------------

# 1. DB 비밀번호 관리
resource "aws_secretsmanager_secret" "db_password" {
  name = "${var.project_name}-db-password-${var.environment}"
  recovery_window_in_days = 0
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = jsonencode({
    username = "admin"
    password = "ktcloudteam6!" 
    engine   = "mysql"
    host     = aws_db_instance.this.address
    port     = 3306
  })
}

# 2. RDS 보안 그룹 (Proxy 통해서만 접속)
resource "aws_security_group" "rds" {
  name   = "${var.project_name}-rds-sg"
  vpc_id = var.vpc_id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.rds_proxy.id] 
  }
}

# 3. RDS 인스턴스
resource "aws_db_instance" "this" {
  identifier        = "${var.project_name}-db"
  engine            = "mysql"
  engine_version    = "8.0"
  instance_class    = "db.m5.large"
  allocated_storage = 20
   
  username          = "admin"
  password          = "ktcloudteam6!" 
  db_name           = "onrace" 
  
  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  
  skip_final_snapshot = true
  multi_az            = true
}

resource "aws_db_subnet_group" "this" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = var.database_subnets
}

# 4. RDS Proxy 보안 그룹 (내용 수정됨: 인라인 ingress 제거)
resource "aws_security_group" "rds_proxy" {
  name   = "${var.project_name}-rds-proxy-sg"
  vpc_id = var.vpc_id

  # [수정] ingress 블록 삭제: EKS가 아직 없어 null 에러가 발생하므로, 
  # 나중에 app 계층에서 aws_security_group_rule로 따로 추가합니다.
  
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# 5. RDS Proxy
resource "aws_db_proxy" "this" {
  name                   = "${var.project_name}-rds-proxy"
  engine_family          = "MYSQL"
  idle_client_timeout    = 1800
  require_tls            = true
  role_arn               = aws_iam_role.rds_proxy_role.arn
  vpc_security_group_ids = [aws_security_group.rds_proxy.id]
  vpc_subnet_ids          = var.database_subnets
  debug_logging          = true

  auth {
    auth_scheme = "SECRETS"
    iam_auth    = "DISABLED"
    secret_arn  = aws_secretsmanager_secret.db_password.arn
  }
}

resource "aws_db_proxy_default_target_group" "this" {
  db_proxy_name = aws_db_proxy.this.name

  connection_pool_config {
    connection_borrow_timeout    = 120
    max_connections_percent      = 100
    max_idle_connections_percent = 50
  }
}

resource "aws_db_proxy_target" "this" {
  db_proxy_name          = aws_db_proxy.this.name
  target_group_name      = aws_db_proxy_default_target_group.this.name
  db_instance_identifier = aws_db_instance.this.identifier
}

# 6, 7. IAM 관련 설정 (변동 없음)
resource "aws_iam_role" "rds_proxy_role" {
  name = "${var.project_name}-rds-proxy-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = { Service = "rds.amazonaws.com" }
    }]
  })
}

resource "aws_iam_policy" "rds_proxy_policy" {
  name = "${var.project_name}-rds-proxy-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret"
        ]
        Effect   = "Allow"
        Resource = [aws_secretsmanager_secret.db_password.arn]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "rds_proxy_attach" {
  role       = aws_iam_role.rds_proxy_role.name
  policy_arn = aws_iam_policy.rds_proxy_policy.arn
}