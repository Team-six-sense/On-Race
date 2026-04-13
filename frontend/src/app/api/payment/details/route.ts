export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const paymentKey = searchParams.get('paymentKey');

  const secretKey = process.env.TOSS_SECRET_KEY;
  const basicToken = Buffer.from(secretKey + ':').toString('base64');

  const response = await fetch(
    `https://api.tosspayments.com/v1/payments/${paymentKey}`,
    {
      headers: {
        Authorization: `Basic ${basicToken}`,
        'Content-Type': 'application/json',
      },
    },
  );

  const data = await response.json();
  return Response.json(data);
}
