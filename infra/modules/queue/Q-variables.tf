variable "project_name" {
  description = "프로젝트 명"
  type        = string
}

variable "environment" {
  description = "배포 환경"
  type        = string
}

variable "queue_name" {
  description = "SQS 큐 이름"
  type        = string
}

variable "fifo_queue" {
  description = "FIFO 큐 여부"
  type        = bool
  default     = true
}

variable "visibility_timeout_seconds" {
  description = "메시지 가시성 타임아웃 (초)"
  type        = number
  default     = 30
}