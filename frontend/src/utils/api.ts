import { NextResponse } from 'next/server';
import { ApiResponse } from '@/types/api';

export const wrapMockResponse = <T = null>(
  data: T = null as unknown as T,
): ApiResponse<T> => ({
  success: true,
  code: 'SUCCESS',
  message: 'Mock data success',
  data: data, // 인자가 없으면 null이 들어감
  timestamp: new Date().toString(),
});

export function handleApiError(error: any) {
  // 상태 코드 추출
  const status = error.response?.status || 500;

  // 백엔드 에러 데이터 추출
  const backendError = error.response?.data;

  // 로그 기록
  console.error('API Route Error:', {
    message: error.message,
    backendError: backendError,
    status: status,
  });

  // 클라이언트에 전달할 표준 에러 응답 생성
  const errorResponse = {
    success: false,
    message:
      backendError?.message ||
      (typeof backendError === 'string'
        ? backendError
        : '서버 통신 중 오류가 발생했습니다.'),
    code: backendError?.code || 'UNKNOWN_ERROR',
    timestamp: backendError?.timestamp || new Date().toISOString(),
  };

  return NextResponse.json(errorResponse, { status });
}
