# 1. Environment Settings
$AWS_ACCOUNT_ID = "916228846377"
$REGION = "ap-northeast-2"
$ECR_BASE_URL = "$AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/t6-on-race-repo"
$NAMESPACE = "t6-on-race-prod"

# Service Mapping (Service Name = Deployment Name)
$SERVICE_MAP = @{
    "main"    = "on-race-api"
    "auth"    = "on-race-auth"
    "gateway" = "on-race-scg"
    "queue"   = "on-race-queue"
}

Write-Host ">>> [1/4] Starting AWS ECR Login..." -ForegroundColor Cyan
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin "$AWS_ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

if ($LastExitCode -ne 0) { Write-Error "ECR Login Failed"; exit }

# 2. Build and Push per Service
foreach ($service in $SERVICE_MAP.Keys) {
    $deployment = $SERVICE_MAP[$service]
    # Clarify variable name and colon(:) using ${}
    $image_tag = "${ECR_BASE_URL}:${service}-latest"

    Write-Host "`n>>> [2/4] Building Image: $service (Target: $deployment)" -ForegroundColor Yellow
    docker build --build-arg SERVICE=$service -t $image_tag .

    if ($LastExitCode -ne 0) { Write-Error "Build Failed for $service"; exit }

    Write-Host ">>> [3/4] Pushing Image to ECR: $image_tag" -ForegroundColor Magenta
    docker push $image_tag
}

# 3. Refresh EKS Deployments
Write-Host "`n>>> [4/4] Refreshing EKS Deployments (Rollout Restart)..." -ForegroundColor Cyan
foreach ($deployment in $SERVICE_MAP.Values) {
    kubectl rollout restart deployment/$deployment -n $NAMESPACE
}

Write-Host "`n>>> All deployments have been completed successfully!" -ForegroundColor Green

# 4. Status Check
Write-Host "`n>>> Checking Pod Status..." -ForegroundColor Cyan
kubectl get pods -n $NAMESPACE