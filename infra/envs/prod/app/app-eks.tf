# 1. EKS 모듈 설정 (Managed Node Group 포함)
module "eks" {
  source          = "../../../modules/eks"
  project_name    = var.project_name
  environment     = var.environment
  cluster_name    = "${var.project_name}-${var.environment}-cluster"
  vpc_id          = data.terraform_remote_state.base.outputs.vpc_id
  subnet_ids      = data.terraform_remote_state.base.outputs.private_subnets
  
  # 초기 노드 설정 (인프라용 파드가 올라갈 '땅')
  instance_types = ["m5.large"] 
  min_size        = 2
  max_size        = 5 # 초기 노드는 많이 필요 없습니다. 나머지는 Karpenter가 처리합니다.

  # [중요] Karpenter가 이 노드 그룹을 인식할 수 있도록 태그가 모듈 내부에 포함되어야 합니다.
  # 만약 모듈이 태그를 지원한다면 아래와 같이 추가하세요.
  tags = {
    "karpenter.sh/discovery" = "${var.project_name}-${var.environment}-cluster"
  }
}

# 2. EBS CSI Add-on (문법 오류 수정됨)
resource "aws_eks_addon" "ebs_csi" {
  cluster_name                = module.eks.cluster_name
  addon_name                  = "aws-ebs-csi-driver"
  
  # [수정] resolve_conflicts_on_update가 두 번 선언되어 에러가 났었습니다. 하나로 합칩니다.
  resolve_conflicts_on_update = "PRESERVE" 
  
  depends_on                  = [module.eks]
}

# 3. LB Controller 설치 (변경 없음, 대기 시간 유지)
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

# [핵심] 컨트롤러가 노드에 안착하고 웹훅을 준비할 충분한 시간 (90초 -> 180초 추천)
resource "time_sleep" "wait_for_lb_controller" {
  depends_on      = [helm_release.lb_controller]
  create_duration = "180s" 
}