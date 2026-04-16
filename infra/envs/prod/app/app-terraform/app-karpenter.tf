# [해결] kubernetes_manifest 대신 kubectl_manifest를 사용하여 Plan 타임의 API 검증을 우회합니다.
resource "kubectl_manifest" "karpenter_nodeclass" {
  yaml_body = <<-YAML
apiVersion: karpenter.k8s.aws/v1
kind: EC2NodeClass
metadata:
  name: default
spec:
  amiFamily: AL2023
  # [수정] 하드코딩된 역할 이름 대신, 모듈에서 생성된 역할의 ARN을 참조합니다.
  role: "${module.karpenter.node_iam_role_name}"
  # [추가] Karpenter가 생성하는 EKS 노드에 Name 태그 부여
  tags:
    Name: "${var.project_name}-${var.environment}-node"
  subnetSelectorTerms:
    - tags:
        karpenter.sh/discovery: "${module.eks.cluster_name}"
  securityGroupSelectorTerms:
    - tags:
        karpenter.sh/discovery: "${module.eks.cluster_name}"
  amiSelectorTerms:
    - alias: al2023@latest
  metadataOptions:
    httpTokens: required
    httpPutResponseHopLimit: 2
    httpEndpoint: enabled
  YAML

  depends_on = [kubernetes_namespace_v1.app, time_sleep.wait_60_seconds_for_karpenter]
}

resource "kubectl_manifest" "karpenter_nodepool" {
  yaml_body = <<-YAML
apiVersion: karpenter.sh/v1
kind: NodePool
metadata:
  name: default
spec:
  template:
    spec:
      nodeClassRef:
        group: karpenter.k8s.aws
        kind: EC2NodeClass
        name: default
      requirements:
        - key: karpenter.sh/capacity-type
          operator: In
          values: ["spot", "on-demand"]
        - key: kubernetes.io/arch
          operator: In
          values: ["amd64"]
        - key: karpenter.k8s.aws/instance-category
          operator: In
          values: ["c", "m", "r"]
  limits:
    cpu: "200"
  disruption:
    consolidationPolicy: WhenEmptyOrUnderutilized
    consolidateAfter: 1m
  YAML

  depends_on = [kubectl_manifest.karpenter_nodeclass]
}