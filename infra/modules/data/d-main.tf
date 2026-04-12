# ==========================================================================
# 1. Redis 계층 (ElastiCache)
# ==========================================================================

# Redis 보안 그룹
resource "aws_security_group" "redis" {
  name   = "${var.project_name}-redis-sg"
  vpc_id = var.vpc_id

  # Ingress는 app 계층에서 별도 규칙으로 추가 (EKS 노드 허용)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-redis-sg" }
}

# Redis 서브넷 그룹
resource "aws_elasticache_subnet_group" "this" {
  name       = "${var.project_name}-redis-subnet-group"
  subnet_ids = var.database_subnets
}

# Redis 파라미터 그룹
resource "aws_elasticache_parameter_group" "this" {
  name   = "${var.project_name}-redis7-params"
  family = "redis7"

  parameter {
    name  = "maxmemory-policy"
    value = "volatile-lru"
  }

  # 유휴 커넥션 정리 (60초 동안 응답 없으면 연결 해제)
  # 좀비 커넥션이 maxclients 한도를 차지하는 것을 방지합니다.
  parameter {
    name  = "timeout"
    value = "60"
  }

  # TCP Keepalive 설정 (300초마다 연결 상태 확인)
  parameter {
    name  = "tcp-keepalive"
    value = "300"
  }
}

# Redis 클러스터 (Replication Group)
resource "aws_elasticache_replication_group" "this" {
  replication_group_id = "${var.project_name}-redis"
  description          = "Redis for On-Race TPS control and session"

  node_type            = var.redis_node_type
  port                 = 6379
  engine               = "redis"
  engine_version       = "7.0"
  
  parameter_group_name = aws_elasticache_parameter_group.this.name
  subnet_group_name    = aws_elasticache_subnet_group.this.name
  security_group_ids   = [aws_security_group.redis.id]

  automatic_failover_enabled = var.automatic_failover_enabled
  num_cache_clusters         = var.num_cache_clusters
  multi_az_enabled           = true
  
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true # 필수: auth_token 사용을 위해 필요
  auth_token                 = var.db_password # RDS와 동일한 암호 주입
  apply_immediately          = true
}


# ==========================================================================
# 2. RDS 데이터베이스 계층
# ==========================================================================

# RDS 보안 그룹 (Proxy로부터의 접속만 허용)
resource "aws_security_group" "rds" {
  name   = "${var.project_name}-rds-sg"
  vpc_id = var.vpc_id

  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.rds_proxy.id]
  }

  tags = { Name = "${var.project_name}-rds-sg" }
}

# RDS 서브넷 그룹
resource "aws_db_subnet_group" "this" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = var.database_subnets

  tags = { Name = "${var.project_name}-db-subnet-group" }
}

# RDS 인스턴스
resource "aws_db_instance" "this" {
  identifier        = "${var.project_name}-db"
  engine            = "mysql"
  engine_version    = "8.0"
  instance_class    = "db.t3.micro" # [수정] m5.large/t3.medium -> t3.micro
  #instance_class    = "db.t3.medium"
  allocated_storage = 20

  username = "admin"
  password = var.db_password # 변수로 전달받은 암호 사용
  db_name  = "onrace"

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.rds.id]

  multi_az            = true
  #multi_az            = false       # [수정] 비용 절감을 위해 가용영역 이중화 해제
  skip_final_snapshot = true
}


# ==========================================================================
# 3. RDS Proxy 계층
# ==========================================================================

# RDS Proxy 보안 그룹
resource "aws_security_group" "rds_proxy" {
  name   = "${var.project_name}-rds-proxy-sg"
  vpc_id = var.vpc_id

  # Ingress는 app 계층에서 별도 규칙으로 추가 (EKS 노드 허용)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.project_name}-rds-proxy-sg" }
}

# RDS Proxy 설정
resource "aws_db_proxy" "this" {
  name                   = "${var.project_name}-rds-proxy"
  engine_family          = "MYSQL"
  idle_client_timeout    = 1800
  require_tls            = true
  role_arn               = aws_iam_role.rds_proxy_role.arn
  vpc_security_group_ids = [aws_security_group.rds_proxy.id]
  vpc_subnet_ids         = var.database_subnets
  debug_logging          = true

  auth {
    auth_scheme = "SECRETS"
    iam_auth    = "DISABLED"
    secret_arn  = var.db_secret_arn # 상위에서 넘겨받은 시크릿 ARN 참조
  }
}

# RDS Proxy 타겟 그룹 (기본값)
resource "aws_db_proxy_default_target_group" "this" {
  db_proxy_name = aws_db_proxy.this.name

  connection_pool_config {
    connection_borrow_timeout    = 120
    max_connections_percent      = 100
    max_idle_connections_percent = 50
  }
}

# RDS Proxy와 실제 DB 인스턴스 연결
resource "aws_db_proxy_target" "this" {
  db_proxy_name          = aws_db_proxy.this.name
  target_group_name      = aws_db_proxy_default_target_group.this.name
  db_instance_identifier = aws_db_instance.this.identifier
}


# ==========================================================================
# 4. IAM 권한 설정 (RDS Proxy용)
# ==========================================================================

# Proxy가 Assume할 IAM Role
resource "aws_iam_role" "rds_proxy_role" {
  name = "${var.project_name}-rds-proxy-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "rds.amazonaws.com" }
    }]
  })
}

# Secrets Manager 접근 정책
resource "aws_iam_policy" "rds_proxy_policy" {
  name = "${var.project_name}-rds-proxy-policy"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "secretsmanager:GetSecretValue",
          "secretsmanager:DescribeSecret",
          "kms:Decrypt"  # 열쇠(KMS) 권한 확보
        ]
        Effect   = "Allow"
        Resource = ["*"] # [수정] 특정 ARN 대신 전체 허용으로 우선 돌파
      }
    ]
  })
}

# 역할에 정책 연결
resource "aws_iam_role_policy_attachment" "rds_proxy_attach" {
  role       = aws_iam_role.rds_proxy_role.name
  policy_arn = aws_iam_policy.rds_proxy_policy.arn
}