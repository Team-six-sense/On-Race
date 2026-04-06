import { NextResponse } from 'next/server';
import axios from 'axios';
import { handleApiError } from '@/utils/api';

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
    const { id } = await params;

    const response = await backendClient.get(`/address/${id}`);

    return NextResponse.json(response.data);
  } catch (error) {
    return handleApiError(error);
  }
}

export async function PUT(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const { id } = await params;
    const body = await request.json();

    const response = await backendClient.put(`/address/${id}`, body);

    return NextResponse.json(response.data);
  } catch (error) {
    return handleApiError(error);
  }
}

export async function DELETE(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    // 클라이언트로부터 전달받은 쿼리 파라미터 추출
    const { id } = await params;

    // 실제 외부 백엔드 서버로 요청 전달
    const response = await backendClient.delete(`/address/${id}`);

    // 백엔드로부터 받은 데이터를 그대로 클라이언트에 반환
    return NextResponse.json(response.data);
  } catch (error: any) {
    return handleApiError(error);
  }
}
