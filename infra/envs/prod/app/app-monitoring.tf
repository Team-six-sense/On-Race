# =====================================================================
# [모니터링] Loki & Promtail 로깅 스택
# =====================================================================

# 1. Loki 전용 IAM 역할(IRSA) 및 S3 리소스 모듈 호출
module "loki" {
  source            = "../../../modules/loki"
  project_name      = var.project_name
  environment       = var.environment
  cluster_name      = module.eks.cluster_name
  oidc_provider_arn = module.eks.oidc_provider_arn
}

# 2. Loki 배포 (템플릿 파일을 사용해 ARN 및 버킷명 동적 주입)
resource "helm_release" "loki" {
  name             = "loki"
  repository       = "https://grafana.github.io/helm-charts"
  chart            = "loki"
  version          = "6.6.1" # 버전 고정을 통해 안정성 확보
  namespace        = "loki"
  create_namespace = true

  wait            = false
  cleanup_on_fail = true
  timeout         = 300

  values = [
    templatefile("${path.module}/helm-values/loki-values.yaml", {
      loki_role_arn    = module.loki.loki_role_arn
      loki_bucket_name = module.loki.loki_bucket_name
    })
  ]

  depends_on = [module.eks, module.loki]
}

# 3. Promtail 배포 (Loki로 로그를 전송하는 에이전트)
resource "helm_release" "promtail" {
  name       = "promtail"
  repository = "https://grafana.github.io/helm-charts"
  chart      = "promtail"
  version    = "6.15.5" # 버전 고정
  namespace  = "loki"   # Loki와 같은 네임스페이스에 배포하여 관리 용이성 증대

  values = [
    file("${path.module}/helm-values/promtail-config-snippet.yaml")
  ]

  # DaemonSet으로 동작하며 모든 노드의 로그를 수집하여 Loki Gateway로 쏩니다.
  set {
    name  = "config.clients[0].url"
    value = "http://loki-gateway.loki.svc.cluster.local/loki/api/v1/push"
  }

  depends_on = [helm_release.loki]
}

# =====================================================================
# [모니터링] Prometheus & Grafana 메트릭 스택
# =====================================================================

# [보안 강화] AWS Secrets Manager에서 Grafana 비밀번호를 안전하게 조회합니다.
# 이 방법을 사용하려면 미리 'on-race-grafana-admin-password'라는 이름으로 보안 암호를 생성해야 합니다.
data "aws_secretsmanager_secret" "grafana_admin_password" {
  name = "on-race-grafana-admin-password"
}

data "aws_secretsmanager_secret_version" "grafana_admin_password" {
  secret_id = data.aws_secretsmanager_secret.grafana_admin_password.id
}

# 4. kube-prometheus-stack Helm 차트 배포
# 이 차트는 Prometheus, Grafana, Alertmanager 등 모니터링에 필요한 대부분의 구성요소를 포함합니다.
# 기존의 개별 prometheus, grafana 릴리즈를 통합하여 중복과 충돌을 방지합니다.
resource "helm_release" "prometheus_stack" {
  name             = "t6-on-race-prometheus"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "kube-prometheus-stack"
  namespace        = "monitoring" # 모니터링 관련 리소스는 'monitoring' 네임스페이스에 격리합니다.
  create_namespace = true
  version          = "58.2.0" # 버전 고정을 통해 예기치 않은 변경을 방지합니다.

  # Helm 차트의 기본값을 재정의(override)하는 설정입니다.
  values = [
    <<-EOF
    # Grafana 설정
    grafana:
      # [보안 강화] 하드코딩된 비밀번호 대신 Secrets Manager에서 가져온 값을 사용합니다.
      adminPassword: ${jsondecode(data.aws_secretsmanager_secret_version.grafana_admin_password.secret_string)["password"]}
      
      # LoadBalancer 타입으로 서비스를 생성하여 외부에서 Grafana 대시보드에 접근할 수 있도록 합니다.
      service:
        type: LoadBalancer
        port: 80
        targetPort: 3000
        # 로드밸런서 체계를 'internet-facing'으로 지정하여 외부 접속을 허용합니다.
        annotations:
          service.beta.kubernetes.io/aws-load-balancer-scheme: internet-facing
      
      # Grafana 대시보드 및 설정 데이터 영속성을 위한 Persistent Volume Claim 설정
      persistence:
        enabled: true
        storageClassName: "gp3" # EKS의 기본 스토리지 클래스인 gp3를 사용합니다.
        size: 10Gi

      # [개선] Grafana에 Loki 데이터소스를 자동으로 추가하여 로그를 바로 조회할 수 있도록 합니다.
      additionalDataSources:
      - name: Loki
        type: loki
        url: http://loki-read.loki.svc.cluster.local:3100
        access: proxy
        isDefault: false

    # Prometheus 설정
    prometheus:
      prometheusSpec:
        # Prometheus 메트릭 데이터 영속성을 위한 PVC 설정
        storageSpec:
          volumeClaimTemplate:
            spec:
              storageClassName: gp3
              accessModes: ["ReadWriteOnce"]
              resources:
                requests:
                  storage: 50Gi
        
        # 서비스 어노테이션 기반으로 메트릭 수집 대상을 자동으로 탐색하도록 설정합니다.
        # 이 설정을 통해 app-api.tf, app-auth.tf에서 추가한 어노테이션을 Prometheus가 인식하게 됩니다.
        serviceMonitorSelectorNilUsesHelmValues: false

        # 서비스 어노테이션 기반 스크래핑 설정을 명시적으로 추가합니다.
        # 이 설정을 통해 Prometheus가 'prometheus.io/scrape: "true"' 어노테이션이 있는 서비스를 자동으로 탐색하게 됩니다.
        additionalScrapeConfigs:
          - job_name: 'kubernetes-service-annotations'
            kubernetes_sd_configs:
              - role: service
            relabel_configs:
              - source_labels: [__meta_kubernetes_service_annotation_prometheus_io_scrape]
                action: keep
                regex: true
              - source_labels: [__meta_kubernetes_service_annotation_prometheus_io_path]
                action: replace
                target_label: __metrics_path__
                regex: (.+)
              - source_labels: [__address__, __meta_kubernetes_service_annotation_prometheus_io_port]
                action: replace
                target_label: __address__
                regex: ([^:]+)(?::\d+)?;(\d+)
                replacement: $1:$2
        
        # [추가] 개발팀 요청 알림 규칙 (0408 정리 기준)
        additionalPrometheusRules:
          - name: on-race-custom-alerts
            groups:
              - name: http-alerts
                rules:
                  - alert: HighHttp5xxErrorRate
                    expr: |
                      sum(rate(http_server_requests_seconds_count{status=~"5.*"}[5m])) by (job, application)
                      /
                      sum(rate(http_server_requests_seconds_count[5m])) by (job, application)
                      > 0.05
                    for: 1m
                    labels:
                      severity: critical
                    annotations:
                      summary: "High HTTP 5xx Error Rate on {{ $labels.application }}"
                      description: "The service {{ $labels.application }} is experiencing a 5xx error rate above 5% for the last 5 minutes."
              - name: jvm-alerts
                rules:
                  - alert: JvmHeapUsageHigh
                    expr: |
                      sum(jvm_memory_used_bytes{area="heap"}) by (job, application)
                      /
                      sum(jvm_memory_max_bytes{area="heap"}) by (job, application)
                      > 0.85
                    for: 5m
                    labels:
                      severity: warning
                    annotations:
                      summary: "High JVM Heap Usage on {{ $labels.application }}"
                      description: "The application {{ $labels.application }} has been using over 85% of its allocated heap memory for the last 5 minutes."


    # Alertmanager 설정 (이번 단계에서는 기본 설정 유지)
    alertmanager:
      alertmanagerSpec:
        storage:
          volumeClaimTemplate:
            spec:
              storageClassName: gp3
              accessModes: ["ReadWriteOnce"]
              resources:
                requests:
                  storage: 10Gi
    EOF
  ]

  depends_on = [
    helm_release.loki, # Loki가 먼저 배포된 후 Grafana에 데이터소스로 등록되도록 의존성 설정
  ]
}