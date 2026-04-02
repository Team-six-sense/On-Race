import { NextResponse } from 'next/server';

export async function POST(request: Request) {
  const { paymentKey, orderId, amount } = await request.json();
  const secretKey = process.env.TOSS_SECRET_KEY!; // 서버 전용 시크릿 키

  const url = 'https://api.tosspayments.com/v1/payments/confirm';
  const basicToken = Buffer.from(`${secretKey}:`).toString('base64');

  const response = await fetch(url, {
    method: 'POST',
    headers: {
      Authorization: `Basic ${basicToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      paymentKey,
      orderId,
      amount,
    }),
  });

  const result = await response.json();

  if (response.ok) {
    const orderName = result.orderName;
    const method = result.method;
    const approvedAt = result.approvedAt;
    const receiptUrl = result.receipt?.url;

    console.log(`상품명: ${orderName}`);
    console.log(`결제수단: ${method}`);
    console.log(`승인시간: ${approvedAt}`);
    console.log(`승인시간: ${receiptUrl}`);

    return NextResponse.json(result);
  } else {
    return NextResponse.json(result, { status: response.status });
  }
}
