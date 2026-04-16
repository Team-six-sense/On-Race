# 1. IMDSv2 홉 제한 수정을 위한 런치 템플릿
resource "aws_launch_template" "node" {
  name_prefix = "${var.project_name}-node-lt-"

  # 프라이빗 서브넷이므로 공인 IP 할당을 명시적으로 비활성화합니다.
  network_interfaces {
    associate_public_ip_address = false
    delete_on_termination       = true
    security_groups             = [aws_security_group.nodes.id]
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                  = "required" 
    http_put_response_hop_limit = 2          
  }

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
    subnet_ids              = var.subnet_ids
    # 프라이빗 서브넷의 노드가 컨트롤 플레인과 통신할 수 있도록 프라이빗 엔드포인트를 활성화합니다.
    # 퍼블릭 엔드포인트는 외부(예: 로컬 PC, Github Actions)에서의 kubectl 접근을 위해 유지합니다.
    endpoint_private_access = true
    endpoint_public_access  = true
  }

  access_config {
    authentication_mode                         = "API_AND_CONFIG_MAP"
    bootstrap_cluster_creator_admin_permissions = true
  }

  depends_on = [aws_iam_role_policy_attachment.cluster_policy]
}

# 3. OIDC Provider
data "tls_certificate" "this" {
  url = aws_eks_cluster.this.identity[0].oidc[0].issuer
}

resource "aws_iam_openid_connect_provider" "this" {
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.this.certificates[0].sha1_fingerprint]
  url             = aws_eks_cluster.this.identity[0].oidc[0].issuer
}

# 4. 공용 보안 그룹 (Karpenter discovery용 태그 포함)
resource "aws_security_group" "nodes" {
  name        = "${var.cluster_name}-nodes-common-sg"
  description = "Shared security group for all EKS nodes"
  vpc_id      = var.vpc_id

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.cluster_name}-nodes-common-sg"
    "karpenter.sh/discovery" = var.cluster_name
  }
}

# 5. Managed Node Group (에러 유발하던 vpc_config 제거됨)
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

  labels = { "role" = "system" }

  depends_on = [
    aws_iam_role_policy_attachment.node_policy,
    aws_iam_role_policy_attachment.cni_policy,
    aws_iam_role_policy_attachment.registry_policy
  ]
}

# 6. 보안 그룹 규칙 통합 (API 서버 통신 결계 해제)


resource "aws_security_group_rule" "cluster_to_node" {
  description              = "Control plane to nodes"
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "-1"
  source_security_group_id = aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
  security_group_id        = aws_security_group.nodes.id
}

resource "aws_security_group_rule" "node_to_cluster" {
  description              = "Nodes to API server (443)"
  type                     = "ingress"
  from_port                = 443
  to_port                  = 443
  protocol                 = "tcp"
  security_group_id        = aws_eks_cluster.this.vpc_config[0].cluster_security_group_id
  source_security_group_id = aws_security_group.nodes.id
}

resource "aws_security_group_rule" "node_to_node" {
  description              = "Node to node"
  type                     = "ingress"
  from_port                = 0
  to_port                  = 65535
  protocol                 = "-1"
  security_group_id        = aws_security_group.nodes.id
  source_security_group_id = aws_security_group.nodes.id
}