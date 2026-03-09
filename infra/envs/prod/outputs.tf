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
  value = module.data.redis_endpoint
}

output "queue_url" {
  value = module.queue.queue_url
}