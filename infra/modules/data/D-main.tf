# Redis용 서브넷 그룹
resource "aws_elasticache_subnet_group" "this" {
  name       = "${var.project_name}-redis-subnet-group"
  subnet_ids = var.database_subnets
}

# Redis 보안 그룹 (EKS 노드에서만 접근 허용)
resource "aws_security_group" "redis" {
  name        = "${var.project_name}-redis-sg"
  vpc_id      = var.vpc_id

  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [var.eks_node_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Redis 파라미터 그룹 (유입 제어 키 보호용)
resource "aws_elasticache_parameter_group" "this" {
  name   = "${var.project_name}-redis7-params"
  family = "redis7"

  parameter {
    name  = "maxmemory-policy"
    value = "volatile-lru" # TTL이 설정된 키(유입제어 카운터 등) 우선 삭제
  }
}

# Redis 클러스터 (Replication Group)
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

  engine         = "redis"
  engine_version = "7.0"

  # 보안 및 가용성 설정
  at_rest_encryption_enabled = true  # 저장 시 암호화
  transit_encryption_enabled = true  # 전송 시 암호화 (TLS)
  multi_az_enabled           = true  # 자동 페일오버 효율 극대화

  # 성능 및 데이터 정책
  apply_immediately          = true
}