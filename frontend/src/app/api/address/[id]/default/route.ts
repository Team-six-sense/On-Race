import { NextResponse } from 'next/server';
import axios from 'axios';
import { handleApiError } from '@/utils/api';

// 서버 측 전용 Axios 인스턴스
const backendClient = axios.create({
  baseURL: process.env.MAIN_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

export async function PATCH(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  try {
    const { id } = await params;

    // 누락되었던 body 추가
    const response = await backendClient.patch(`/address/${id}/default`);

    return NextResponse.json(response.data);
  } catch (error) {
    return handleApiError(error);
  }
}
