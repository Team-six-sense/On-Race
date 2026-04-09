# 1. VQA 전용 Service Account
resource "kubernetes_service_account_v1" "vqa_sa" {
  metadata {
    name      = "ai-service-account"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
    annotations = {
      "eks.amazonaws.com/role-arn" = module.ai_vqa_irsa.iam_role_arn
    }
  }
}

# 2. VQA API Deployment
resource "kubernetes_deployment_v1" "on_race_vqa" {
  metadata {
    name      = "on-race-vqa"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }

  spec {
    replicas = 2 
    selector {
      match_labels = { app = "on-race-vqa" }
    }

    template {
      metadata {
        labels = { app = "on-race-vqa" }
      }

      spec {
        service_account_name = kubernetes_service_account_v1.vqa_sa.metadata[0].name

        # [고가용성 추가] 여러 가용 영역(AZ)에 파드를 고르게 분산 배치
        topology_spread_constraint {
          max_skew           = 1
          topology_key       = "topology.kubernetes.io/zone"
          when_unsatisfiable = "DoNotSchedule"
          label_selector {
            match_labels = { app = "on-race-vqa" }
          }
        }

        container {
          name  = "vqa-api"
          image = "${data.terraform_remote_state.base.outputs.vqa_ecr_repository_url}:latest"

          # [추가] :latest 태그 덮어쓰기 배포 시 새 이미지 다운로드 강제
          image_pull_policy = "Always"
          
          port { container_port = 8000 }

          resources {
            requests = {
              cpu    = "200m"
              memory = "512Mi" 
            }
            limits = {
              cpu    = "1000m"
              memory = "1Gi"
            }
          }

          env {
            name  = "DB_ENDPOINT"
            value = data.terraform_remote_state.base.outputs.rds_proxy_endpoint
          }
          env {
            name  = "DB_PORT"
            value = "3306"
          }
          env {
            name  = "DB_NAME"
            value = "onrace"
          }
          env {
            name  = "DB_USERNAME"
            value = local.db_creds.username
          }
          env {
            name  = "DB_PASSWORD"
            value = local.db_creds.password
          }
          env {
            name  = "S3_BUCKET_NAME"
            value = data.terraform_remote_state.base.outputs.ai_vqa_bucket_name
          }
          env {
            name  = "AWS_REGION"
            value = "ap-northeast-2"
          }
        }
      }
    }
  }
}

# 3. VQA 내부 서비스 (ClusterIP)
resource "kubernetes_service_v1" "on_race_vqa" {
  metadata {
    name      = "on-race-vqa-service"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    selector = { app = "on-race-vqa" }
    port {
      port        = 8000
      target_port = 8000
    }
    type = "ClusterIP"
  }
}

# 4. VQA 가용성 보장 정책 (PDB)
resource "kubernetes_pod_disruption_budget_v1" "vqa_pdb" {
  metadata {
    name      = "on-race-vqa-pdb"
    namespace = kubernetes_namespace_v1.app.metadata[0].name
  }
  spec {
    min_available = 1 # 업데이트나 노드 점검 시에도 최소 1개 파드 가동 유지
    selector {
      match_labels = { app = "on-race-vqa" }
    }
  }
}

# AI 팀원이 EKS에 접근할 수 있도록 클러스터 레벨 권한 부여
resource "aws_eks_access_entry" "ai_team_eks_access" {
  cluster_name  = module.eks.cluster_name
  principal_arn = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:user/on-race-ai-dev1"
  type          = "STANDARD"
}

# AI 팀원에게 VQA가 있는 네임스페이스 한정으로 파드 편집/재시작 권한 부여
resource "aws_eks_access_policy_association" "ai_team_eks_policy_assoc" {
  cluster_name  = module.eks.cluster_name
  principal_arn = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:user/on-race-ai-dev1"
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSEditPolicy"

  access_scope {
    type       = "namespace"
    namespaces = [kubernetes_namespace_v1.app.metadata[0].name] # t6-on-race-prod 자동 참조
  }
}