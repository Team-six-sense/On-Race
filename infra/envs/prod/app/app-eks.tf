# 1. EKS 모듈 설정 (에러 원인인 tags 제거)
module "eks" {
  source          = "../../../modules/eks"
  project_name    = var.project_name
  environment     = var.environment
  cluster_name    = "${var.project_name}-${var.environment}-cluster"
  vpc_id          = data.terraform_remote_state.base.outputs.vpc_id
  subnet_ids      = data.terraform_remote_state.base.outputs.private_subnets
  
  instance_types  = ["m5.large"] 
  min_size        = 2
  max_size        = 5
}

# [핵심] 모듈 외부에서 Karpenter용 태그를 직접 주입 (보안 그룹)
resource "aws_ec2_tag" "eks_sg_karpenter_tag" {
  resource_id = module.eks.node_security_group_id
  key         = "karpenter.sh/discovery"
  value       = module.eks.cluster_name
}

# [추가] Karpenter가 노드를 띄울 서브넷을 찾을 수 있도록 태그 주입
resource "aws_ec2_tag" "subnet_karpenter_tag" {
  for_each    = toset(data.terraform_remote_state.base.outputs.private_subnets)
  resource_id = each.value
  key         = "karpenter.sh/discovery"
  value       = module.eks.cluster_name
}

# 2. EBS CSI Add-on (중복 제거 및 문법 교정)
resource "aws_eks_addon" "ebs_csi" {
  cluster_name                = module.eks.cluster_name
  addon_name                  = "aws-ebs-csi-driver"
  resolve_conflicts_on_update = "PRESERVE" 
  depends_on                  = [module.eks]
}

# 3. LB Controller 설치
resource "helm_release" "lb_controller" {
  name       = "aws-load-balancer-controller"
  repository = "https://aws.github.io/eks-charts"
  chart      = "aws-load-balancer-controller"
  namespace  = "kube-system"

  set {
    name  = "clusterName"
    value = module.eks.cluster_name
  }
  set {
    name  = "serviceAccount.create"
    value = "true"
  }
  set {
    name  = "serviceAccount.name"
    value = "aws-load-balancer-controller"
  }
  set {
    name  = "serviceAccount.annotations.eks\\.amazonaws\\.com/role-arn"
    value = module.lb_controller_irsa.iam_role_arn
  }
  depends_on = [module.eks]
}

# [최적화] 웹훅 안정화를 위한 대기 시간 연장
resource "time_sleep" "wait_for_lb_controller" {
  depends_on      = [helm_release.lb_controller]
  create_duration = "180s" 
}