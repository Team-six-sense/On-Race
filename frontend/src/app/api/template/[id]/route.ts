import { NextResponse } from 'next/server';

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const id = searchParams.get('id');

  // API 요청
  // TEMPLATE 조회 API 실행

  // 데이터 전처리
  const result = {
    message: `TEMPLATE[${id}] 정보 조회`,
    data: 'data',
    timestamp: new Date().toISOString(),
  };

  // 결과 반환
  return NextResponse.json(result);
}

export async function POST(request: Request) {
  const body = await request.json();
  const { name, type, description } = body;

  const createData = {
    name: name,
    type: type,
    description: description,
  };

  // API 요청
  // TEMPLATE 생성 API 실행

  // Response 데이터 생성
  const result = {
    message: `TEMPLATE 정보 생성`,
    data: body,
    timestamp: new Date().toISOString(),
  };

  // 결과 반환
  return NextResponse.json(result);
}

export async function PATCH(request: Request) {
  const { searchParams } = new URL(request.url);
  const body = await request.json();

  const id = searchParams.get('id');
  const { name, type, description } = body;

  // updateData
  const updateData = {
    name: name,
    type: type,
    description: description,
  };

  // API 요청
  // TEMPLATE 업데이트 API 실행

  // Response 데이터 생성
  const result = {
    message: `TEMPLATE[${id}] 정보 업데이트`,
    data: body,
    timestamp: new Date().toISOString(),
  };

  // 결과 반환
  return NextResponse.json(result);
}

export async function DELETE(request: Request) {
  const { searchParams } = new URL(request.url);
  const id = searchParams.get('id');

  // API 요청
  // TEMPLATE 삭제 API 실행

  // Response 데이터 생성
  const result = {
    message: `TEMPLATE[${id}] 정보 삭제`,
    data: null,
    timestamp: new Date().toISOString(),
  };

  // 결과 반환
  return NextResponse.json(result);
}
