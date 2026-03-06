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
  const message = defaultMessage;

  return NextResponse.json({ success: false, message }, { status });
}

export async function GET(
  request: Request,
  { params }: { params: { id: number } },
) {
  try {
    const { id } = params;

    // const { searchParams } = new URL(request.url);

    // // 개별적으로 가져오기
    // const type = searchParams.get('type'); // ?type=home
    // const isDetail = searchParams.get('detail'); // ?detail=true

    // // 백엔드로 전달 (Axios의 params 옵션 사용)
    // // Axios의 params는 객체를 자동으로 ?key=value 형태의 쿼리 스트링으로 변환해줍니다.
    // const response = await backendClient.get(`/address/${id}`, {
    //   params: {
    //     type: type,
    //     detail: isDetail,
    //     // 만약 들어오는 모든 쿼리 파라미터를 그대로 넘기고 싶다면:
    //     // ...Object.fromEntries(searchParams.entries())
    //   },
    // });

    const response = await backendClient.get(`/address/${id}`);

    return NextResponse.json(response.data);
  } catch (error) {
    return handleError(error, '데이터를 불러오는 중 오류가 발생했습니다.');
  }
}

export async function PUT(
  request: Request,
  { params }: { params: { id: number } },
) {
  try {
    const { id } = params;
    const body = await request.json();

    const response = await backendClient.put(`/address/${id}`, body);

    return NextResponse.json(response.data);
  } catch (error) {
    return handleError(error, '데이터 수정 중 오류가 발생했습니다.');
  }
}

export async function DELETE(
  request: Request,
  { params }: { params: { id: number } },
) {
  try {
    const { id } = params;

    // DELETE 요청 시 body가 필요한 경우 처리 (선택 사항)
    let body = {};
    try {
      body = await request.json();
    } catch {
      // body가 없는 경우 무시
    }

    // axios.delete는 config 객체 안에 data를 담아야 합니다.
    const response = await backendClient.delete(`/address/${id}`, {
      data: body,
    });

    return NextResponse.json(response.data);
  } catch (error) {
    return handleError(error, '데이터 삭제 중 오류가 발생했습니다.');
  }
}

export async function PATCH(
  request: Request,
  { params }: { params: { id: number } },
) {
  try {
    const { id } = params;
    const body = await request.json();

    // 누락되었던 body 추가
    const response = await backendClient.patch(`/address/${id}`, body);

    return NextResponse.json(response.data);
  } catch (error) {
    return handleError(error, '데이터 일부 수정 중 오류가 발생했습니다.');
  }
}
