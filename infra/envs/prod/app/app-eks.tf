module "eks" {
  source         = "../../../modules/eks"
  project_name   = var.project_name
  environment    = var.environment
  cluster_name   = "${var.project_name}-${var.environment}-cluster"
  vpc_id         = data.terraform_remote_state.base.outputs.vpc_id
  subnet_ids     = data.terraform_remote_state.base.outputs.private_subnets
  instance_types = ["m5.large"]
  min_size       = 2
  max_size       = 15
}

resource "aws_eks_addon" "ebs_csi" {
  cluster_name                = module.eks.cluster_name
  addon_name                  = "aws-ebs-csi-driver"
  resolve_conflicts_on_update = "OVERWRITE"
  depends_on                  = [module.eks]
}

resource "aws_iam_role_policy_attachment" "node_ebs_policy" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
  role       = module.eks.node_iam_role_name
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

resource "time_sleep" "wait_for_lb_controller" {
  depends_on      = [helm_release.lb_controller]
  create_duration = "90s" # 컨트롤러 파드 초기화 대기
}