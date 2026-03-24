module "eks" {
  source          = "../../../modules/eks"
  project_name    = var.project_name
  environment     = var.environment
  cluster_name    = "${var.project_name}-${var.environment}-cluster"
  vpc_id          = data.terraform_remote_state.base.outputs.vpc_id
  subnet_ids      = data.terraform_remote_state.base.outputs.private_subnets
  instance_types = ["m5.large"]
  min_size        = 2
  max_size        = 15
}

# 1. Load Balancer Controller용 IAM 역할 (IRSA)
module "lb_controller_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name                              = "${var.project_name}-lb-controller"
  attach_load_balancer_controller_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:aws-load-balancer-controller"]
    }
  }
}

# 2. Helm을 이용한 Load Balancer Controller 설치
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

# 3. 컨트롤러 안정화를 위한 대기 시간
resource "time_sleep" "wait_for_lb_controller" {
  depends_on      = [helm_release.lb_controller]
  create_duration = "90s"
}

resource "aws_eks_addon" "ebs_csi" {
  cluster_name                = module.eks.cluster_name
  addon_name                  = "aws-ebs-csi-driver"
  resolve_conflicts_on_update = "OVERWRITE"
  depends_on                  = [module.eks]
}

resource "aws_iam_role_policy_attachment" "node_ebs_policy" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
  role        = module.eks.node_iam_role_name
  depends_on = [module.eks]
}

resource "kubernetes_storage_class_v1" "gp3_default" {
  metadata {
    name        = "gp3"
    annotations = { "storageclass.kubernetes.io/is-default-class" = "true" }
  }
  storage_provisioner    = "ebs.csi.aws.com"
  reclaim_policy         = "Delete"
  volume_binding_mode    = "WaitForFirstConsumer"
  allow_volume_expansion = true
  parameters             = { type = "gp3" }
  depends_on             = [module.eks, aws_eks_addon.ebs_csi]
}

resource "kubernetes_namespace_v1" "app" {
  metadata { name = "${var.project_name}-${var.environment}" }
  depends_on = [module.eks]
}