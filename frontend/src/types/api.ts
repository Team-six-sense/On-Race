export interface ApiResponse<T = null> {
  // 기본값을 null로 설정
  success: boolean;
  code: string;
  message: string;
  data: T;
  timestamp: string;
}
