'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { authService } from '@/features/auth/services';

import { useRouter } from 'next/navigation';
import { useState } from 'react';

import { useSignupStore } from '@/features/auth/store/useSignupStore';

import { LuEye, LuEyeOff, LuX } from 'react-icons/lu';

export default function SignupForm() {
  const router = useRouter();
  const [name, setName] = useState<string>('');
  const [email, setEmail] = useState<string>('');
  const [phoneNumber, setPhoneNumber] = useState<string>('');

  const [password, setPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');

  const [code, setCode] = useState<string>('');
  const [showPassword, setShowPassword] = useState<boolean>(false);
  const [showConfirmPassword, setShowConfirmPassword] =
    useState<boolean>(false);

  const setSignupData = useSignupStore((state) => state.setSignupData);

  // 이메일 중복 확인 API 호출
  const handleDuplicationEmail = async () => {
    try {
      await authService.checkEmailAddress({ email });
    } catch (error: any) {
      throw new Error(error);
    }
  };

  // 휴대폰 본인 인증 API 호출
  const handleCheckCode = async () => {
    try {
      await authService.sendSmsCode({ phoneNumber });
    } catch (error: any) {
      throw new Error(error);
    }
  };

  const handleCancelSignup = () => {
    router.push('/login');
  };
  const handleSignup = async () => {
    try {
      const verifyResponse = await authService.verifySmsCode({
        phoneNumber,
        code,
      });
      if (verifyResponse.success) {
        setSignupData(name, email, password, phoneNumber);
        router.push('/signup/email-auth');
      }
    } catch (error: any) {
      throw new Error(error);
    }
  };

  return (
    <div className="flex-1 flex flex-col items-center justify-center bg-primary">
      <div className="flex flex-col w-full max-w-xl p-8 ">
        <div className="text-4xl font-bold py-4">이메일 회원가입</div>
        <div className="text-2xl font-bold py-4">기본 정보 입력</div>

        <div className="flex gap-2 space-y-2 mb-4">
          <Input
            variant="primary"
            label="이름"
            placeholder="이름을 입력해주세요"
            value={name}
            onChange={(e) => setName(e.target.value)}
          />
        </div>

        <p className="text-sm mb-1">이메일 *</p>
        <div className="flex gap-2 space-y-2 mb-4">
          <Input
            variant="primary"
            placeholder="example@gmail.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            rightElement={
              <Button
                variant="primary1"
                size="xs"
                rounded="full"
                className="text-xs"
                onClick={handleDuplicationEmail}
              >
                중복확인
              </Button>
            }
          />
        </div>

        <p className="text-sm mb-1">휴대폰번호</p>
        <div className="flex gap-2 space-y-2 mb-2">
          <Input
            variant="primary"
            placeholder="010-1234-5678"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            rightElement={
              <Button
                variant="primary1"
                size="xs"
                rounded="full"
                className="text-xs"
                onClick={handleCheckCode}
              >
                본인인증
              </Button>
            }
          />
        </div>

        <div className="space-y-2 mb-4">
          <Input
            variant="primary"
            placeholder="인증번호를 입력해주세요*"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            rightElement={
              <Button
                variant="text"
                size="iconSm"
                className="text-gray-400"
                onClick={() => setCode('')}
              >
                <LuX />
              </Button>
            }
          />
        </div>

        <div className="space-y-2 mb-6">
          <Input
            variant="primary"
            placeholder="비밀번호를 입력해주세요"
            label="비밀번호 *"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            rightElement={
              <Button
                variant="text"
                size="iconSm"
                className="text-font-low"
                onClick={() => setShowPassword((prev) => !prev)}
              >
                {showPassword ? <LuEyeOff /> : <LuEye />}
              </Button>
            }
          />
          <Input
            variant="primary"
            placeholder="비밀번호를 입력해주세요"
            label="비밀번호 확인 *"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            rightElement={
              <Button
                variant="text"
                size="iconSm"
                className="text-font-low"
                onClick={() => setShowConfirmPassword((prev) => !prev)}
              >
                {showConfirmPassword ? <LuEyeOff /> : <LuEye />}
              </Button>
            }
          />
        </div>

        <div className="flex space-y-2 gap-2">
          <Button
            variant="outline"
            rounded="sm"
            className="border-gray-200"
            onClick={handleCancelSignup}
          >
            취소
          </Button>
          <Button variant="primary1" rounded="sm" onClick={handleSignup}>
            다음
          </Button>
        </div>
      </div>
    </div>
  );
}
