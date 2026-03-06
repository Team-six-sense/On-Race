import { NextResponse } from 'next/server';
import axios, { AxiosError } from 'axios';

// 서버 측 전용 Axios 인스턴스
const backendClient = axios.create({
  baseURL: process.env.BACKEND_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

/**
 * 공통 에러 핸들러
 */
function handleError(error: any, defaultMessage: string) {
  const axiosError = error as AxiosError<any>;
  console.error(
    `API Route Error:`,
    axiosError.response?.data || axiosError.message,
  );

  const status = axiosError.response?.status || 500;
  const message = axiosError.response?.data?.message || defaultMessage;

  return NextResponse.json({ success: false, message }, { status });
}

export async function GET(request: Request) {
  try {
    // 실제 외부 백엔드 서버로 요청 전달
    const response = await backendClient.get('/address');

    // 백엔드로부터 받은 데이터를 그대로 클라이언트에 반환
    return NextResponse.json(response.data);
  } catch (error) {
    return handleError(error, '데이터를 불러오는 중 오류가 발생했습니다.');
  }
}

export async function POST(request: Request) {
  try {
    // 실제 외부 백엔드 서버로 요청 전달
    const response = await backendClient.post('/address');

    // 백엔드로부터 받은 데이터를 그대로 클라이언트에 반환
    return NextResponse.json(response.data);
  } catch (error) {
    return handleError(error, '데이터를 불러오는 중 오류가 발생했습니다.');
  }
}
