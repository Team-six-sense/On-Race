# 1. IAM 정책 (Policies)
import {
  to = aws_iam_policy.keda_sqs_policy
  id = "arn:aws:iam::274130523831:policy/t6-on-race-prod-keda-sqs-policy"
}

import {
  to = aws_iam_policy.ai_s3_access
  id = "arn:aws:iam::274130523831:policy/t6-on-race-ai-s3-access"
}

import {
  to = module.loki.aws_iam_policy.loki_s3_policy
  id = "arn:aws:iam::274130523831:policy/t6-on-race-prod-cluster-LokiS3Access"
}

# 2. CloudFront 리소스
import {
  to = aws_cloudfront_public_key.vqa_key
  id = "K2POEQMPBW4Z72"
}

import {
  to = aws_cloudfront_key_group.vqa_key_group
  id = "이곳에_키그룹_ID를_입력하세요" 
}

# 3. 보안 그룹 (Security Group)
import {
  to = module.eks.aws_security_group.nodes
  id = "sg-084d84e79255a4ac7"
}

# 4. IAM 역할 (Roles)
import {
  to = module.karpenter.aws_iam_role.karpenter_controller
  id = "t6-on-race-karpenter-controller"
}

import {
  to = module.api_irsa.aws_iam_role.this[0]
  id = "t6-on-race-prod-api-role"
}

import {
  to = module.ai_vqa_irsa.aws_iam_role.this[0]
  id = "t6-on-race-ai-vqa-pod-role"
}

import {
  to = module.ebs_csi_irsa.aws_iam_role.this[0]
  id = "t6-on-race-ebs-csi-role"
}

import {
  to = module.keda_irsa.aws_iam_role.this[0]
  id = "t6-on-race-prod-keda-operator-role"
}

import {
  to = module.lb_controller_irsa.aws_iam_role.this[0]
  id = "t6-on-race-lb-controller"
}

import {
  to = module.loki.module.loki_irsa.aws_iam_role.this[0]
  id = "t6-on-race-prod-cluster-loki-irsa"
}