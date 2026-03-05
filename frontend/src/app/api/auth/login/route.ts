import { NextResponse } from 'next/server';
import axios from 'axios';

// 서버 측 전용 Axios 인스턴스
const backendClient = axios.create({
  baseURL: process.env.BACKEND_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

export async function POST(request: Request) {
  // 클라이언트로부터 전달받은 쿼리 파라미터 추출
  const { searchParams } = new URL(request.url);
  const params = Object.fromEntries(searchParams.entries());

  try {
    // 실제 외부 백엔드 서버로 요청 전달
    const response = await backendClient.post('/login', { params });

    // 백엔드로부터 받은 데이터를 그대로 클라이언트에 반환
    return NextResponse.json(response.data);
  } catch (error: any) {
    console.error('API Route Error:', error.response?.data || error.message);

    return NextResponse.json(
      { success: false, message: '서버 통신 중 오류가 발생했습니다.' },
      { status: error.response?.status || 500 },
    );
  }
}
