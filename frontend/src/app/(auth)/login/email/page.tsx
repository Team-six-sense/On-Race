'use client';

import { signIn } from 'next-auth/react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { LuEye, LuEyeOff } from 'react-icons/lu';

export default function EmailLoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [error, setError] = useState<string>('');

  const handleCredentialsLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const result = await signIn('credentials', {
      email,
      password,
      redirect: false, // 성공 시 자동 리다이렉트를 막고 직접 제어 (에러 처리를 위해)
    });

    if (result?.error) {
      // 인증 실패 시 (Spring에서 401 등을 보냈을 때)
      setError('이메일 또는 비밀번호가 일치하지 않습니다.');
    } else {
      // 로그인 성공 시 메인 페이지로 이동
      router.push('/');
      router.refresh(); // 세션 정보를 최신화하기 위해 권장
    }
  };

  return (
    <div className="flex-1 flex flex-col items-center justify-center bg-primary">
      <div className="flex flex-col w-full max-w-md p-8 ">
        <h1 className="my-2 text-xl font-bold">이메일 로그인</h1>

        <Input
          className="my-2"
          variant="primary"
          placeholder="example@gmail.com"
          label="이메일 *"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <Input
          className="my-2"
          variant="primary"
          placeholder="비밀번호를 입력해주세요"
          label="비밀번호 *"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          rightElement={
            <Button
              variant="text"
              size="iconSm"
              className="text-gray-400"
              onClick={() => setShowPassword((prev) => !prev)}
            >
              {showPassword ? <LuEyeOff /> : <LuEye />}
            </Button>
          }
        />
        <Button
          className="my-2"
          rounded="full"
          onClick={handleCredentialsLogin}
        >
          로그인 하기
        </Button>

        <div className="flex justify-end items-center w-full">
          <Button
            className="px-1 text-gray-400"
            variant="text"
            size="fit"
            onClick={() => router.push('/find/account')}
          >
            아이디 찾기
          </Button>

          <span className="text-sm text-gray-400">|</span>

          <Button
            className="px-1 text-gray-400"
            variant="text"
            size="fit"
            onClick={() => router.push('/find/password')}
          >
            비밀번호 재설정
          </Button>

          <span className="text-sm text-gray-400">|</span>

          <Button
            className="px-1 text-gray-400"
            variant="text"
            size="fit"
            onClick={() => router.push('/signup/email/agree')}
          >
            회원가입
          </Button>
        </div>
      </div>
    </div>
  );
}
