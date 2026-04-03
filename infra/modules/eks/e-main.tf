# 1. IMDSv2 홉 제한 수정을 위한 런치 템플릿
resource "aws_launch_template" "node" {
  name = "${var.project_name}-node-lt"

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required" # IMDSv2 사용 강제
    http_put_response_hop_limit = 2          # 파드 내에서 노드 IAM 권한 접근 허용
  }

  description = "EKS Managed Node Group Launch Template for IMDSv2 Hop Limit"

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "${var.project_name}-node"
    }
  }
}

# 2. EKS 클러스터
resource "aws_eks_cluster" "this" {
  name     = var.cluster_name
  role_arn = aws_iam_role.cluster.arn
  version  = "1.30"

  enabled_cluster_log_types = ["api", "audit", "authenticator", "controllerManager", "scheduler"]

  vpc_config {
    subnet_ids = var.subnet_ids
  }

  access_config {
    authentication_mode                         = "API_AND_CONFIG_MAP"
    bootstrap_cluster_creator_admin_permissions = true
  }

  depends_on = [aws_iam_role_policy_attachment.cluster_policy]
}

# 3. OIDC Provider (Karpenter/KEDA 필수)
data "tls_certificate" "this" {
  url = aws_eks_cluster.this.identity[0].oidc[0].issuer
}

resource "aws_iam_openid_connect_provider" "this" {
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.this.certificates[0].sha1_fingerprint]
  url             = aws_eks_cluster.this.identity[0].oidc[0].issuer
}

# 4. [수정] 모든 워커 노드(Managed + Karpenter)가 공유할 공용 보안 그룹
resource "aws_security_group" "nodes" {
  name        = "${var.cluster_name}-nodes-common-sg"
  description = "Shared security group for all EKS nodes (Managed and Karpenter)"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.cluster_name}-nodes-common-sg"
    # Karpenter가 이 보안 그룹을 찾아 노드에 입히기 위한 핵심 태그
    "karpenter.sh/discovery" = var.cluster_name
  }
}

# 5. [수정] Managed Node Group
resource "aws_eks_node_group" "system" {
  cluster_name    = aws_eks_cluster.this.name
  node_group_name = "${var.project_name}-system-nodes"
  node_role_arn   = aws_iam_role.node.arn
  subnet_ids      = var.subnet_ids

  ami_type       = "AL2023_x86_64_STANDARD"
  instance_types = var.instance_types

  launch_template {
    id      = aws_launch_template.node.id
    version = aws_launch_template.node.latest_version
  }

  scaling_config {
    desired_size = var.min_size
    min_size     = var.min_size
    max_size     = var.max_size
  }

  labels = {
    "role" = "system"
  }

  depends_on = [
    aws_iam_role_policy_attachment.node_policy,
    aws_iam_role_policy_attachment.cni_policy,
    aws_iam_role_policy_attachment.registry_policy
  ]
}

# 6. [수정] EKS 컨트롤 플레인 -> 노드 전체 통신 허용
resource "aws_security_group_rule" "cluster_to_node" {
  description              = "Allow cluster control plane to communicate with nodes"
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "-1"
  source_security_group_id = aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
  security_group_id        = aws_security_group.nodes.id
}

# 6-1. [수정] 노드 전체 -> EKS 컨트롤 플레인(API 서버) 통신 허용 (타임아웃 해결)
resource "aws_security_group_rule" "node_to_cluster" {
  description              = "Allow all nodes to communicate with control plane API"
  type                     = "ingress"
  from_port                = 443
  to_port                  = 443
  protocol                 = "tcp"
  security_group_id        = aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
  source_security_group_id = aws_security_group.nodes.id
}

# 7. [수정] 노드 상호 간 통신 허용
resource "aws_security_group_rule" "node_to_node" {
  description              = "Allow nodes to communicate with each other"
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "-1"
  security_group_id        = aws_security_group.nodes.id
  source_security_group_id = aws_security_group.nodes.id
}