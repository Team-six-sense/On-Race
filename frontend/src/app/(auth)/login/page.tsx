'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { SocialLoginButtons } from '@/features/auth/components';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const router = useRouter();
  return (
    <div className="flex-1 flex flex-col items-center justify-center bg-primary">
      <div className="flex flex-col w-full max-w-md p-8 ">
        <h1 className="mt-2 text-xl font-bold">ON RACE 와 함께 </h1>
        <SocialLoginButtons />
      </div>
    </div>
  );
}
