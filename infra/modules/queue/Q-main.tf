resource "aws_sqs_queue" "this" {
  name                        = var.queue_name
  fifo_queue                  = var.fifo_queue
  content_based_deduplication = var.fifo_queue # FIFO일 경우 중복 제거 활성화
  
  visibility_timeout_seconds = var.visibility_timeout_seconds
  
  # 메시지 보관 주기 (기본 4일)
  message_retention_seconds = 345600
  
  # KEDA가 큐 길이를 감시할 때 부하를 줄이기 위한 지연 시간 설정
  delay_seconds = 0

  tags = {
    Name = var.queue_name
  }
}