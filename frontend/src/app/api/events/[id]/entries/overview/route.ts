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
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    // 클라이언트로부터 전달받은 쿼리 파라미터 추출
    const { id } = await params;

    const { searchParams } = new URL(request.url);

    // 방법 A: 전체 쿼리 스트링을 그대로 가져오기 (추천)
    // const queryString = searchParams.toString();

    // 방법 B: 특정 값만 개별적으로 가져오기
    const courseId = searchParams.get('courseId');
    const paceId = searchParams.get('paceId');

    // 실제 외부 백엔드 서버로 요청 전달
    const response = await backendClient.get(`/events/${id}/rate`, {
      params: {
        courseId,
        paceId,
      },
    });

    // 백엔드로부터 받은 데이터를 그대로 클라이언트에 반환
    return NextResponse.json(response.data);
  } catch (error: any) {
    return handleApiError(error);
  }
}
