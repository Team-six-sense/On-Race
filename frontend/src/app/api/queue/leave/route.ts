import { NextResponse } from 'next/server';
import axios from 'axios';
import { handleApiError } from '@/utils/api';

// 서버 측 전용 Axios 인스턴스
const backendClient = axios.create({
  baseURL: process.env.MAIN_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

export async function DELETE(request: Request) {
  try {
    const { searchParams } = new URL(request.url);

    const queryData = Object.fromEntries(searchParams.entries());

    const response = await backendClient.delete(`/queue/leave`, {
      params: queryData,
    });

    return NextResponse.json(response.data);
  } catch (error) {
    return handleApiError(error);
  }
}
