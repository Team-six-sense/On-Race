'use client';

import { useRouter, usePathname } from 'next/navigation';
import { useSession, signOut } from 'next-auth/react';
import Link from 'next/link';
import { cn } from '@/lib/utils';
import { Button } from '../ui/button';
import Image from 'next/image';

export default function Header() {
  const { data: session, status } = useSession();
  const router = useRouter();
  const pathname = usePathname();

  const isHome = pathname === '/';

  const navItems = [
    { name: '홈', href: '/' },
    { name: '이벤트', href: '/event' },
    { name: '마우스이벤트(테스트용)', href: '/mouse-event' },
  ];

  return (
    <div className="relative w-full">
      <header
        className={cn(
          'w-full z-50 transition-all duration-300',
          isHome ? 'absolute top-0 ' : 'relative bg-white ',
        )}
      >
        <div className="max-w-[1200px] mx-auto flex items-center justify-between h-20 p-6">
          <div className="flex items-center gap-10">
            <Link href="/">
              <div className="flex flex-row items-center justifycenter gap-2">
                <Image
                  src={
                    isHome
                      ? '/image/logo/logo_white.png'
                      : '/image/logo/logo_black.png'
                  }
                  alt="Logo"
                  width={16}
                  height={16}
                />
                <div
                  className={cn(
                    'text-base tracking-tighter transition-colors duration-500',
                    isHome ? 'text-white' : 'text-black',
                  )}
                >
                  Race
                </div>
              </div>
            </Link>

            <ul className="hidden md:flex items-center gap-4 h-full">
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
              'flex items-center gap-4 text-xs font-bold transition-colors duration-500',
              isHome ? 'text-white/80' : 'text-gray-600',
            )}
          >
            {session ? (
              <div className="flex">
                <p
                  className={cn(
                    'flex items-center font-bold text-xs mr-4 transition-colors duration-500',
                    isHome
                      ? 'text-white/80 hover:text-white'
                      : 'text-gray-600 hover:text-black',
                  )}
                >
                  반갑습니다 {session.user?.name} 님!
                </p>
                <Button
                  className={cn(
                    'flex items-center font-bold text-xs p-0 mr-4 transition-colors duration-500 cursor-pointer',
                    isHome
                      ? 'text-white/80 hover:text-white'
                      : 'text-gray-600 hover:text-black',
                  )}
                  variant="text"
                  size="fit"
                  onClick={() => signOut({ callbackUrl: '/login' })}
                >
                  로그아웃
                </Button>
                <Link
                  href="/mypage"
                  className={cn(
                    'flex items-center transition-colors duration-500',
                    isHome ? 'hover:text-white' : 'hover:text-black',
                  )}
                >
                  <span className=""> 마이페이지 </span>
                </Link>
              </div>
            ) : (
              <>
                <Link
                  href="/login"
                  className={cn(
                    'flex items-center transition-colors duration-500',
                    isHome ? 'hover:text-white' : 'hover:text-black',
                  )}
                >
                  로그인
                </Link>
                <Link
                  href="/signup/email/agree"
                  className={cn(
                    'flex items-center transition-colors duration-500',
                    isHome ? 'hover:text-white' : 'hover:text-black',
                  )}
                >
                  <span className=""> 회원가입 </span>
                </Link>
              </>
            )}

            <Link
              href="/support"
              className={cn(
                'flex items-center transition-colors duration-500',
                isHome ? 'hover:text-white' : 'hover:text-black',
              )}
            >
              <span className="">고객센터</span>
            </Link>
          </div>
        </div>
      </header>

      {/* BANNER SECTION */}
      {isHome && (
        <section className="relative h-[700px] w-full bg-black overflow-hidden">
          <Image
            src="/image/banner2.png"
            alt="Main Banner"
            fill
            priority
            className="object-cover opacity-70"
          />
          <div className="absolute inset-0 bg-gradient-to-r from-black/60 via-black/20 to-transparent" />
          <div className="absolute bottom-12 left-1/2 -translate-x-1/2 w-full max-w-[1200px] px-6 z-10">
            <div className="max-w-xl text-left text-white">
              <h2 className="text-6xl font-bold mb-4  tracking-tight">
                달림 그 이상의 순간
              </h2>
              <p className="text-2xl mb-8 text-white/80">
                기록이 아닌 감각, 속도가 아닌 연결.
                <br />
                On이 설계한 러닝 라이프를 직접 경험하세요.
              </p>
              <Button
                variant="outline"
                rounded="full"
                size="fit"
                className="p-6 mb-6"
                onClick={() => router.push('/event')}
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
