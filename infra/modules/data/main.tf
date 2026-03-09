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
    from_port   = 6379
    to_port     = 6379
    protocol    = "tcp"
    cidr_blocks = ["10.0.0.0/16"] # VPC 내부 통신 허용
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Redis 클러스터 (Replication Group)
resource "aws_elasticache_replication_group" "this" {
  replication_group_id = "${var.project_name}-redis"
  
  # [수정] replication_group_description을 description으로 변경
  description          = "Redis for On-Race Queue and Session"
  
  node_type            = var.redis_node_type
  port                 = 6379
  parameter_group_name = "default.redis7"
  
  automatic_failover_enabled = var.automatic_failover_enabled
  num_cache_clusters         = var.num_cache_clusters
  subnet_group_name          = aws_elasticache_subnet_group.this.name
  security_group_ids         = [aws_security_group.redis.id]

  engine         = "redis"
  engine_version = "7.0"
}