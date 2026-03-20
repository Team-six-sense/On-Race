output "vpc_id" {
  value = module.vpc.vpc_id
}

output "private_subnets" {
  value = module.vpc.private_subnets
}

output "public_subnets" {
  value = module.vpc.public_subnets
}

output "redis_endpoint" {
  value = try(module.data.redis_endpoint, "pending")
}

# try()를 써야만 Step 1 배포 시 에러가 나지 않습니다.
output "rds_proxy_endpoint" {
  value       = try(module.data.rds_proxy_endpoint, "pending")
  description = "DB Connection String for Backend Application"
}

output "queue_url" {
  value = try(module.queue.queue_url, "pending")
}