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
          image = "274130523831.dkr.ecr.ap-northeast-2.amazonaws.com/on-race-vqa:latest"
          
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