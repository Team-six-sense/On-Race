import { NextResponse } from 'next/server';

export async function POST(request: Request) {
  const body = await request.json();

  // Spring 백엔드 비즈니스 로직 시뮬레이션
  // console.log('Spring Mock Backend Received:', body);

  const mockResponse = {
    accessToken: 'mock-spring-jwt-access-token',
    refreshToken: 'mock-spring-jwt-refresh-token',
    user: {
      id: 1,
      email: body.email,
      name: body.name,
      role: 'USER',
    },
  };

  return NextResponse.json(mockResponse, { status: 200 });
}
