'use client';

import { useRouter, usePathname } from 'next/navigation';
import { useSession, signOut } from 'next-auth/react';
import Link from 'next/link';
import { cn } from '@/lib/utils';
import { useEffect, useState } from 'react';
import { Button } from '../ui/button';
import { LuHeadset, LuSearch, LuUser } from 'react-icons/lu';

export default function Header() {
  const { data: session, status } = useSession();
  const [mounted, setMounted] = useState(false);
  const router = useRouter();
  const pathname = usePathname(); // 현재 경로에 따른 활성화 표시를 위함

  // 하이드레이션 오류 방지: 클라이언트 마운트 이후에만 UI를 그리도록 설정
  useEffect(() => {
    setMounted(true);
  }, []);

  const navItems = [
    // { name: '공지사항', href: '/notice' },
    { name: '이벤트', href: '/schedule' },
    { name: '기록조회', href: '/records' },
    { name: '대회신청내역', href: '/history' },
  ];

  return (
    <header className="w-full bg-white">
      {/* 1. 최상단 유저 메뉴 - 더 얇고 정돈된 느낌 */}
      <div className="bg-secondary border-b border-gray-100">
        <div className="max-w-[1100px] mx-auto flex justify-end items-center gap-6 py-2 px-6 text-[11px] text-slate-500 font-semibold tracking-tight">
          {session ? (
            // 로그인 상태일 때: 로그아웃 버튼 (클릭 시 signOut 실행)
            <div className="flex gap-4">
              <p className="text-black">반갑습니다 {session.user?.name} 님!</p>
              <Button
                className="contents font-semibold text-xs cursor-pointer"
                variant="text"
                onClick={() => signOut()}
              >
                로그아웃
              </Button>
            </div>
          ) : (
            // 로그아웃 상태일 때: 로그인 링크
            <Link
              href="/login"
              className="flex items-center gap-1 text-black hover:text-status-hover1 transition-colors"
            >
              로그인
            </Link>
          )}
          <Link
            href="/signup"
            className="flex items-center gap-1 text-black hover:text-status-hover1  transition-colors"
          >
            회원가입
          </Link>
          <Link
            href="/support"
            className="flex items-center gap-1 text-black hover:text-status-hover1  transition-colors"
          >
            <LuHeadset size={12} /> 고객센터
          </Link>
          <Link
            href="/mypage"
            className="flex items-center gap-1 text-black hover:text-status-hover1 transition-colors"
          >
            <LuUser size={12} /> 마이페이지
          </Link>
        </div>
      </div>

      {/* 2. 메인 로고 및 메뉴 섹션 */}
      <nav className="border-b border-slate-100">
        {' '}
        {/* 하단에 아주 연한 경계선 추가 (선택 사항) */}
        <div className="max-w-[1100px] mx-auto flex items-center h-20 px-6">
          {/* 로고 */}
          <div
            className="flex items-center cursor-pointer"
            onClick={() => router.push('/')}
          >
            <h1 className="text-xl font-bold tracking-tight text-black">
              LOGO
            </h1>
          </div>

          {/* 세로 구분선 */}
          <div className="w-[1px] h-4 bg-slate-300 mx-8" />

          {/* 메뉴 리스트 */}
          <ul className="flex items-center gap-8 h-full">
            {navItems.map((item) => {
              const isActive = pathname === item.href;
              return (
                <li
                  key={item.name}
                  className={cn(
                    'relative flex items-center h-full text-sm font-medium transition-colors cursor-pointer group',
                    isActive ? 'text-black' : 'text-gray-500 hover:text-black',
                  )}
                  onClick={() => router.push(item.href)}
                >
                  {item.name}

                  {/* 활성화 표시 언더라인 (더 심플하게 수정) */}
                  {isActive && (
                    <span className="absolute bottom-0 left-0 w-full h-[2px] bg-black" />
                  )}

                  {/* 호버 시 나타나는 언더라인 */}
                  {!isActive && (
                    <span className="absolute bottom-0 left-0 w-0 h-[2px] bg-gray-400 transition-all duration-300 group-hover:w-full" />
                  )}
                </li>
              );
            })}
          </ul>
        </div>
      </nav>
    </header>
  );
}
