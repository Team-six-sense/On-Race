'use client';

import { SocialLoginButtons } from '@/features/auth/components';
import Image from 'next/image';

export default function LoginPage() {
  return (
    <div className="flex-1 flex flex-col items-center justify-center bg-primary">
      <div className="flex flex-col w-full max-w-md p-8 ">
        <div className="flex items-center gap-1 mb-4">
          <Image
            src="/image/logo/logo_black.png"
            alt="Logo"
            width={16}
            height={16}
          />
          <div className="text-base font-medium">Race</div>
          <div className="text-2xl font-bold">와 함께 레이스 하세요! </div>
        </div>

        <SocialLoginButtons />
      </div>
    </div>
  );
}
