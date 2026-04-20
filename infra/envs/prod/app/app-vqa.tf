# # 1. VQA 전용 Service Account
# resource "kubernetes_service_account_v1" "vqa_sa" {
#   metadata {
#     name      = "ai-service-account"
#     namespace = kubernetes_namespace_v1.app.metadata[0].name
#     annotations = {
#       "eks.amazonaws.com/role-arn" = module.ai_vqa_irsa.iam_role_arn
#     }
#   }
# }

# # 2. VQA API Deployment
# resource "kubernetes_deployment_v1" "on_race_vqa" {
#   metadata {
#     name      = "on-race-vqa"
#     namespace = kubernetes_namespace_v1.app.metadata[0].name
#   }

#   wait_for_rollout = false

#   spec {
#     replicas = 2
#     selector {
#       match_labels = { app = "on-race-vqa" }
#     }

#     template {
#       metadata {
#         labels = { app = "on-race-vqa" }
#       }

#       spec {
#         service_account_name = kubernetes_service_account_v1.vqa_sa.metadata[0].name

#         topology_spread_constraint {
#           max_skew           = 1
#           topology_key       = "topology.kubernetes.io/zone"
#           when_unsatisfiable = "ScheduleAnyway" # 분석 결과 반영: 가용성 정책 완화
#           label_selector {
#             match_labels = { app = "on-race-vqa" }
#           }
#         }

#         container {
#           name  = "vqa-api"
#           image = "${data.terraform_remote_state.base.outputs.vqa_ecr_repository_url}:latest"

#           image_pull_policy = "Always"
#           port { container_port = 8000 }

#           resources {
#             requests = { cpu = "250m", memory = "800Mi" }
#             limits   = { cpu = "1200m", memory = "2Gi" }
#           }

#           # [환경 변수] DB/S3 연동 및 보안 키
#           env {
#             name  = "DB_ENDPOINT"
#             value = data.terraform_remote_state.base.outputs.rds_proxy_endpoint
#           }
#           env {
#             name  = "DB_PORT"
#             value = "3306"
#           }
#           env {
#             name  = "DB_NAME"
#             value = "onrace"
#           }
#           env {
#             name  = "DB_USERNAME"
#             value = local.db_creds.username
#           }
#           env {
#             name  = "DB_PASSWORD"
#             value = local.db_creds.password
#           }
#           env {
#             name  = "S3_BUCKET_NAME"
#             value = data.terraform_remote_state.base.outputs.ai_vqa_bucket_name
#           }
#           env {
#             name  = "AWS_REGION"
#             value = "ap-northeast-2"
#           }
#           env {
#             name = "JWT_SECRET"
#             value_from {
#               secret_key_ref {
#                 name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
#                 key  = "JWT_SECRET"
#               }
#             }
#           }
#           env {
#             name = "GATEWAY_INTERNAL_SECRET"
#             value_from {
#               secret_key_ref {
#                 name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name
#                 key  = "GATEWAY_INTERNAL_SECRET"
#               }
#             }
#           }

#           # [추가] AI 모델 로딩 시간을 고려한 상태 검사
#           startup_probe {
#             tcp_socket { port = 8000 }
#             initial_delay_seconds = 30 # 모델 로딩 대기
#             period_seconds        = 10
#             failure_threshold     = 30
#             timeout_seconds       = 10
#           }
#           readiness_probe {
#             tcp_socket { port = 8000 }
#             initial_delay_seconds = 10
#             period_seconds        = 10
#             timeout_seconds       = 5
#           }
#         }
#         volume {
#           name = "on-race-common-secrets"
#           secret { secret_name = kubernetes_secret_v1.on_race_common_secrets.metadata[0].name }
#         }
#       }
#     }
#   }

#   lifecycle {
#     ignore_changes = [
#       metadata[0].annotations,
#       metadata[0].labels,
#       spec[0].template[0].metadata[0].annotations,
#       spec[0].template[0].metadata[0].labels,
#     ]
#   }
# }

# # 3. VQA 내부 서비스 (ClusterIP)
# resource "kubernetes_service_v1" "on_race_vqa" {
#   metadata {
#     name      = "on-race-vqa"
#     namespace = kubernetes_namespace_v1.app.metadata[0].name
#   }
#   spec {
#     selector = { app = "on-race-vqa" }
#     port {
#       port        = 8000
#       target_port = 8000
#     }
#     type = "ClusterIP"
#   }
# }

# # 4. VQA 가용성 보장 정책 (PDB)
# resource "kubernetes_pod_disruption_budget_v1" "vqa_pdb" {
#   metadata {
#     name      = "on-race-vqa-pdb"
#     namespace = kubernetes_namespace_v1.app.metadata[0].name
#   }
#   spec {
#     min_available = 1
#     selector {
#       match_labels = { app = "on-race-vqa" }
#     }
#   }
# }

# # 5. AI 팀원 접근 권한
# resource "aws_eks_access_entry" "ai_team_eks_access" {
#   cluster_name  = module.eks.cluster_name
#   principal_arn = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:user/on-race-ai-dev1"
#   type          = "STANDARD"
# }

# resource "aws_eks_access_policy_association" "ai_team_eks_policy_assoc" {
#   cluster_name  = module.eks.cluster_name
#   principal_arn = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:user/on-race-ai-dev1"
#   policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSEditPolicy"

#   access_scope {
#     type       = "namespace"
#     namespaces = [kubernetes_namespace_v1.app.metadata[0].name]
#   }
# }