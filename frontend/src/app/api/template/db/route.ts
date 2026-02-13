// import pool from '@/lib/db';
import { NextResponse } from 'next/server';

// export async function GET(request: Request) {
//   try {
//     const { searchParams } = new URL(request.url);
//     const id = searchParams.get('id');

//     const [rows] = await pool.query('SELECT * FROM users');
//     return NextResponse.json(rows);
//   } catch (error) {
//     return NextResponse.json({ error: 'DB 연결 실패' }, { status: 500 });
//   }
// }

// export async function POST(request: Request) {
//   try {
//     const { searchParams } = new URL(request.url);
//     const id = searchParams.get('id');

//     const [rows] = await pool.query('CREATE ...');
//     return NextResponse.json(rows);
//   } catch (error) {
//     return NextResponse.json({ error: 'DB 연결 실패' }, { status: 500 });
//   }
// }

// export async function PATCH(request: Request) {
//   try {
//     const { searchParams } = new URL(request.url);
//     const id = searchParams.get('id');

//     const [rows] = await pool.query('UPDATE ...');
//     return NextResponse.json(rows);
//   } catch (error) {
//     return NextResponse.json({ error: 'DB 연결 실패' }, { status: 500 });
//   }
// }

// export async function DELETE(request: Request) {
//   try {
//     const { searchParams } = new URL(request.url);
//     const id = searchParams.get('id');

//     const [rows] = await pool.query('DELETE ...');
//     return NextResponse.json(rows);
//   } catch (error) {
//     return NextResponse.json({ error: 'DB 연결 실패' }, { status: 500 });
//   }
// }
