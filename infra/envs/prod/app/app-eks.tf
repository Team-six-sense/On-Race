# 1. EKS 모듈 설정
module "eks" {
  source       = "../../../modules/eks"
  project_name = var.project_name
  environment  = var.environment
  cluster_name = "${var.project_name}-${var.environment}-cluster"
  vpc_id       = data.terraform_remote_state.base.outputs.vpc_id
  subnet_ids   = data.terraform_remote_state.base.outputs.private_subnets

  # 비용 효율이 좋고 CPU 성능이 높은 최신 C계열 및 M계열 우선 배치
  instance_types = ["c6i.large", "c5.large", "m6i.large", "m5.large", "t3.large"]

  # 고정 유지 비용 절감을 위해 시스템 파드용 최소 거점만 유지 (나머지는 Karpenter가 Spot으로 처리)
  min_size = 2
  max_size = 5
}

# 2. [에러 해결 1] LB Controller용 IAM 역할
module "lb_controller_irsa" {
  source                                 = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version                                = "~> 5.0"
  role_name                              = "${var.project_name}-lb-controller"
  attach_load_balancer_controller_policy = true
  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:aws-load-balancer-controller"]
    }
  }
}

# [핵심 추가] 2-1. EBS CSI 드라이버용 IAM 역할 (IRSA)
module "ebs_csi_irsa" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name             = "${var.project_name}-ebs-csi-role"
  attach_ebs_csi_policy = true # EBS 관리 권한 부여

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:ebs-csi-controller-sa"]
    }
  }
}

# 5. Karpenter용 태그 주입 (서브넷)
resource "aws_ec2_tag" "subnet_karpenter_tag" {
  for_each    = toset(data.terraform_remote_state.base.outputs.private_subnets)
  resource_id = each.value
  key         = "karpenter.sh/discovery"
  value       = module.eks.cluster_name
}

# 6. [수정] EBS CSI Add-on (IAM 역할 연동)
resource "aws_eks_addon" "ebs_csi" {
  cluster_name                = module.eks.cluster_name
  addon_name                  = "aws-ebs-csi-driver"
  resolve_conflicts_on_update = "PRESERVE"

  # 생성한 IAM 역할의 ARN을 여기에 연결해야 드라이버가 정상 작동합니다.
  service_account_role_arn = module.ebs_csi_irsa.iam_role_arn

  depends_on = [module.eks, module.ebs_csi_irsa]
}

# 7. LB Controller 설치
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
  depends_on = [module.eks, module.lb_controller_irsa]
}

# 8. 컨트롤러 안정화 대기 시간
resource "time_sleep" "wait_for_lb_controller" {
  depends_on      = [helm_release.lb_controller]
  create_duration = "180s"
}