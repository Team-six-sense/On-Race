# 1. VPC 모듈 호출
module "vpc" {
  source = "../../modules/vpc"

  project_name = var.project_name
  environment  = var.environment
  vpc_cidr     = var.vpc_cidr
  azs          = var.azs
  
  private_subnets  = var.private_subnets
  public_subnets   = var.public_subnets
  database_subnets = var.database_subnets
  
  single_nat_gateway = true
}

# 2. EKS 모듈 호출
module "eks" {
  source = "../../modules/eks"

  project_name = var.project_name
  environment  = var.environment
  cluster_name = "${var.project_name}-${var.environment}-cluster"
  
  vpc_id      = module.vpc.vpc_id
  subnet_ids  = module.vpc.private_subnets
  
  instance_types = ["m5.large"]
  min_size       = 2
  max_size       = 5  
  
  depends_on = [module.vpc] 
}

# 3. 데이터 계층 모듈 호출 (Redis)
module "data" {
  source = "../../modules/data"

  project_name     = var.project_name
  environment      = var.environment
  vpc_id           = module.vpc.vpc_id
  database_subnets = module.vpc.database_subnets
  
  redis_node_type  = "cache.m5.large" 
  
  automatic_failover_enabled = true
  num_cache_clusters         = 2
}

# 4. SQS 대기열 모듈 호출 
module "queue" {
  source = "../../modules/queue"

  project_name = var.project_name
  environment  = var.environment
  
  queue_name = "${var.project_name}-waiting-queue.fifo"
  fifo_queue = true
  
  visibility_timeout_seconds = 60
}

# 5. Loki(로키) 엔진 인프라 호출
module "loki" {
  source            = "../../modules/loki"
  project_name      = var.project_name
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

# 6. EBS CSI 드라이버 및 스토리지 권한 설정 (IaC 통합)
resource "aws_eks_addon" "ebs_csi" {
  # 하드코딩 대신 EKS 모듈의 출력값을 참조합니다.
  cluster_name = module.eks.cluster_name
  addon_name   = "aws-ebs-csi-driver"
  
  # 수동 설치된 리소스와 충돌 시 테라폼 설정을 우선합니다.
  resolve_conflicts_on_update = "OVERWRITE"

  depends_on = [module.eks]
}

resource "aws_iam_role_policy_attachment" "node_ebs_policy" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
  
  # 수동 확인했던 노드 역할 이름을 기입합니다. 
  # 만약 EKS 모듈에서 노드 역할명을 출력한다면 module.eks.node_iam_role_name 등으로 대체 가능합니다.
  role       = "t6-on-race-eks-node-role"

  depends_on = [module.eks]
}