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

  // 현재 페이지가 메인(홈)인지 확인
  const isHome = pathname === '/';

  const navItems = [
    { name: '홈', href: '/' },
    { name: '이벤트', href: '/schedule' },
  ];

  return (
    <div className="relative w-full">
      {/* --- 1. HEADER (페이지에 따라 스타일 가변) --- */}
      <header
        className={cn(
          'w-full z-50 transition-all duration-300',
          isHome
            ? 'absolute top-0 bg-transparent'
            : 'relative bg-white border-b border-gray-100',
        )}
      >
        <div className="max-w-[1200px] mx-auto flex items-center justify-between h-20 px-6">
          {/* 왼쪽: 로고 + 메인 메뉴 */}
          <div className="flex items-center gap-12">
            <Link href="/">
              <h1
                className={cn(
                  'text-2xl font-black tracking-tighter',
                  isHome ? 'text-white' : 'text-black',
                )}
              >
                LOGO
              </h1>
            </Link>

            <ul className="hidden md:flex items-center gap-8 h-full">
              {navItems.map((item) => {
                const isActive = pathname === item.href;
                return (
                  <li
                    key={item.name}
                    className="relative flex items-center h-full"
                  >
                    <Link
                      href={item.href}
                      className={cn(
                        'text-sm font-bold transition-colors group',
                        isHome
                          ? isActive
                            ? 'text-white'
                            : 'text-white/70 hover:text-white'
                          : isActive
                            ? 'text-black'
                            : 'text-gray-500 hover:text-black',
                      )}
                    >
                      {item.name}
                      <span
                        className={cn(
                          'absolute -bottom-1 left-0 h-[2px] transition-all duration-300',
                          isHome ? 'bg-white' : 'bg-black',
                          isActive ? 'w-full' : 'w-0 group-hover:w-full',
                        )}
                      />
                    </Link>
                  </li>
                );
              })}
            </ul>
          </div>

          {/* 오른쪽: 유저 메뉴 */}
          <div
            className={cn(
              'flex items-center gap-5 text-[12px] font-bold',
              isHome ? 'text-white/80' : 'text-gray-600',
            )}
          >
            {session ? (
              <div className="flex items-center gap-4">
                <span className="hidden sm:block opacity-60">반갑습니다님</span>
                <button
                  className={isHome ? 'hover:text-white' : 'hover:text-black'}
                >
                  로그아웃
                </button>
              </div>
            ) : (
              <>
                <Link
                  href="/login"
                  className={isHome ? 'hover:text-white' : 'hover:text-black'}
                >
                  로그인
                </Link>
                <Link
                  href="/signup"
                  className={isHome ? 'hover:text-white' : 'hover:text-black'}
                >
                  회원가입
                </Link>
              </>
            )}
            <div
              className={cn(
                'w-[1px] h-3 mx-1',
                isHome ? 'bg-white/20' : 'bg-gray-200',
              )}
            />
            <Link
              href="/support"
              className={cn(
                'flex items-center gap-1',
                isHome ? 'hover:text-white' : 'hover:text-black',
              )}
            >
              <LuHeadset size={14} />{' '}
              <span className="hidden sm:inline">고객센터</span>
            </Link>
            <Link
              href="/mypage"
              className={cn(
                'flex items-center gap-1',
                isHome ? 'hover:text-white' : 'hover:text-black',
              )}
            >
              <LuUser size={14} />{' '}
              <span className="hidden sm:inline">마이페이지</span>
            </Link>
          </div>
        </div>
      </header>

      {/* --- 2. BANNER SECTION (메인 페이지에서만 렌더링) --- */}
      {isHome && (
        <section className="relative h-[700px] w-full bg-black overflow-hidden">
          <img
            src="/banner.jpg"
            alt="Main Banner"
            className="absolute inset-0 w-full h-full object-cover opacity-70"
          />
          <div className="absolute inset-0 bg-gradient-to-r from-black/60 via-black/20 to-transparent" />
          <div className="absolute bottom-12 left-1/2 -translate-x-1/2 w-full max-w-[1200px] px-6 z-10">
            <div className="max-w-xl text-left text-white">
              <h2 className="text-5xl font-black mb-4 leading-tight tracking-tight">
                달림 그 이상의 순간
              </h2>
              <p className="text-lg mb-8 text-white/80 leading-relaxed">
                기록이 아닌 감각, 속도가 아닌 연결.
                <br />
                On이 설계한 러닝 라이프를 직접 경험하세요.
              </p>
              <Button
                variant="outline"
                rounded="full"
                size="fit"
                className="p-6 mb-6"
                onClick={() => router.push('/schedule')}
              >
                진행 중인 이벤트 보러가기
              </Button>
            </div>
          </div>
        </section>
      )}
    </div>
  );
}
