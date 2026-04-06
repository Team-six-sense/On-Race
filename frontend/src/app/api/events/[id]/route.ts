import { NextResponse } from 'next/server';
import { handleApiError } from '@/utils/api';
import axios from 'axios';

// 서버 측 전용 Axios 인스턴스
const backendClient = axios.create({
  baseURL: process.env.MAIN_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: number }> },
) {
  try {
    // 클라이언트로부터 전달받은 쿼리 파라미터 추출
    const { id } = await params;

    // 실제 외부 백엔드 서버로 요청 전달
    const response = await backendClient.get(`/events/${id}`);

    // 백엔드로부터 받은 데이터를 그대로 클라이언트에 반환
    return NextResponse.json(response.data);
  } catch (error: any) {
    return handleApiError(error);
  }
}
