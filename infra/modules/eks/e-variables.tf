variable "project_name" { type = string }
variable "environment" { type = string }
variable "cluster_name" { type = string }
variable "subnet_ids" { type = list(string) }
variable "instance_types" { type = list(string) }
variable "min_size" { type = number }
variable "max_size" { type = number }
variable "vpc_id" {
  description = "EKS가 배포될 VPC ID"
  type        = string
}