'use client';

import React, { useState } from 'react';
// 아이콘 라이브러리 (이미지 대신 SVG 아이콘 사용으로 깨짐 방지)
import { Mail, User, ShieldCheck, FileText } from 'lucide-react';
import { LuCheck } from 'react-icons/lu';
// shadcn 경로 반영: @/components/shadcn/
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/shadcn/label';
import { Checkbox } from '@/components/ui/checkbox';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/shadcn/card';
import { useRouter } from 'next/navigation';

const steps = [
  { id: 1, title: '정보입력', icon: User },
  { id: 2, title: '약관동의', icon: FileText },
  { id: 3, title: '이메일인증', icon: Mail },
  { id: 4, title: '본인인증', icon: ShieldCheck },
];

export default function SignupForm() {
  const router = useRouter();
  const [step, setStep] = useState(1);

  const nextStep = () => {
    if (step < 4) {
      setStep(step + 1);
    }
  };
  const prevStep = () => {
    if (step > 1) {
      setStep(step - 1);
    } else {
      router.push('/login');
    }
  };

  return (
    <div className="flex justify-center items-center min-h-screen bg-primary p-4">
      <Card className="w-full max-w-[500px] ">
        <CardHeader className="space-y-6 pb-2">
          {/* 단계별 상단 인디케이터 (Stepper) */}
          <div className="relative flex justify-between w-full px-2">
            {/* 배경 선 */}
            <div className="absolute top-5 left-0 w-full h-[2px] bg-slate-200 -z-0" />
            {/* 활성화 선 */}
            <div
              className="absolute top-5 left-0 h-[2px] bg-black transition-all duration-500 ease-in-out -z-0"
              style={{ width: `${((step - 1) / 3) * 100}%` }}
            />

            {steps.map((s) => (
              <div
                key={s.id}
                className="relative z-10 flex flex-col items-center group"
              >
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center border-2 transition-all duration-300 ${
                    step >= s.id
                      ? 'bg-black text-white'
                      : 'bg-gray-300 text-black'
                  } ${step > s.id ? 'bg-green-500 text-white' : ''}`}
                >
                  {step > s.id ? (
                    <LuCheck className="w-4 h-4" />
                  ) : (
                    <s.icon className="w-4 h-4" />
                  )}
                </div>
                <span
                  className={`text-[11px] mt-2 font-semibold transition-colors ${
                    step >= s.id ? 'text-black-700' : 'text-gray-400'
                  }`}
                >
                  {s.title}
                </span>
              </div>
            ))}
          </div>
        </CardHeader>

        <CardContent className="min-h-[300px]">
          {/* 각 단계별 컴포넌트 분기 */}
          <div className="transition-all duration-300">
            {step === 1 && <Step1Info />}
            {step === 2 && <Step2Terms />}
            {step === 3 && <Step3Email />}
            {step === 4 && <Step4Identity />}
          </div>
        </CardContent>

        <CardFooter className="flex flex-col gap-4 pt-6">
          <div className="flex w-full gap-3">
            <Button
              variant="outline"
              rounded="none"
              onClick={prevStep}
              className="flex-1"
            >
              {step > 1 ? '이전' : '취소'}
            </Button>

            <Button
              variant="primary1"
              rounded="none"
              onClick={
                step === 4 ? () => router.push('/signup/success') : nextStep
              }
              className="flex-1"
            >
              {step === 4 ? '회원가입 완료' : '다음'}
            </Button>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
}

// --- 단계별 서브 컴포넌트 ---

function Step1Info() {
  return (
    <div>
      <div className="space-y-4 ">
        <h1>필수 정보</h1>

        <p className="text-sm mb-1">이메일 (아이디) *</p>
        <div className="flex gap-2 space-y-2">
          <Input variant="primary" placeholder="example@gmail.com" />
          <Button variant="outline" size="fit" rounded="sm">
            중복확인
          </Button>
        </div>
        <div className="space-y-2">
          <Input
            variant="primary"
            placeholder="비밀번호를 입력하세요"
            label="비밀번호 *"
          />
        </div>
        <div className="space-y-2">
          <Input
            variant="primary"
            placeholder="비밀번호를 입력하세요"
            label="비밀번호 확인 *"
          />
        </div>
      </div>

      <div className="mt-6"></div>

      <div className="space-y-4 ">
        <h1>선택 정보</h1>
        <p className="text-sm mb-1">배송지</p>
        <div className="flex gap-2 ">
          <Input variant="primary" placeholder="우편번호" />
          <Button variant="outline" size="fit" rounded="sm">
            주소 검색
          </Button>
        </div>
        <div>
          <Input variant="primary" placeholder="기본 주소" />
        </div>
        <div>
          <Input variant="primary" placeholder="나머지 주소 (선택사항)" />
        </div>
      </div>
    </div>
  );
}

function Step2Terms() {
  return (
    <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-500">
      <div className="rounded-xl border border-gray-300 bg-gray-50 p-4 space-y-4 shadow-sm">
        {/* 전체 동의 */}
        <div className="flex items-center space-x-3 pb-3 border-b border-gray-300">
          <Checkbox id="all" variant="primary" />
          <Label
            htmlFor="all"
            className="text-base font-bold cursor-pointer text-slate-800"
          >
            모든 약관에 전체 동의
          </Label>
        </div>

        <div className="space-y-2">
          {/* 필수 약관들 */}
          <div className="space-y-1 px-1 border-b border-gray-300 pb-2">
            <div className="flex items-center justify-between group">
              <div className="flex items-center space-x-3">
                <Checkbox id="t1" variant="primary" />
                <Label
                  htmlFor="t1"
                  className="text-sm cursor-pointer text-black"
                >
                  이용약관 동의
                  <span className="text-blue-600 font-semibold">(필수)</span>
                </Label>
              </div>
              <Button variant="link" size="fit" className="text-xs">
                전체보기
              </Button>
            </div>

            <div className="flex items-center justify-between group">
              <div className="flex items-center space-x-3">
                <Checkbox id="t2" variant="primary" />
                <Label
                  htmlFor="t2"
                  className="text-sm cursor-pointer text-black"
                >
                  개인정보 수집 및 이용 동의
                  <span className="text-blue-600 font-semibold">(필수)</span>
                </Label>
              </div>
              <Button variant="link" size="fit" className="text-xs">
                전체보기
              </Button>
            </div>
          </div>

          {/* 선택 약관 (마케팅 수신) */}
          <div className="space-y-3 px-1 pt-1">
            <div className="flex items-center space-x-3">
              <Checkbox id="marketing" variant="primary" />
              <Label
                htmlFor="marketing"
                className="text-sm cursor-pointer text-black"
              >
                마케팅 정보 수신 동의
                <span className="text-black font-normal">(선택)</span>
              </Label>
            </div>

            {/* 세부 마케팅 수신 옵션 */}
            <div className="flex items-center space-x-6 pl-8">
              <div className="flex items-center space-x-2">
                <Checkbox id="email_agree" variant="primary" />
                <Label
                  htmlFor="email_agree"
                  className="text-xs cursor-pointer text-black"
                >
                  이메일
                </Label>
              </div>
              <div className="flex items-center space-x-2">
                <Checkbox id="sms_agree" variant="primary" />
                <Label
                  htmlFor="sms_agree"
                  className="text-xs cursor-pointer text-black"
                >
                  SMS
                </Label>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function Step3Email() {
  return (
    <div className="space-y-6 text-center animate-in fade-in slide-in-from-bottom-2 duration-500 pt-4">
      <div className="mx-auto w-16 h-16 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center rotate-3 shadow-inner">
        <Mail className="w-8 h-8" />
      </div>
      <div className="space-y-2">
        <p className="text-sm font-medium text-slate-700">
          이메일로 전송된 인증번호를 입력하세요.
        </p>
        <div className="flex gap-2 max-w-[320px] mx-auto pt-2">
          <Input
            className="text-center text-2xl font-bold tracking-[0.5em] h-14 border-2 focus-visible:ring-blue-600"
            maxLength={6}
            placeholder="000000"
          />
        </div>
        <p className="text-xs text-slate-400 pt-2">
          이메일을 받지 못하셨나요?{' '}
          <span className="text-blue-600 font-semibold cursor-pointer">
            다시 보내기
          </span>
        </p>
      </div>
    </div>
  );
}

function Step4Identity() {
  return (
    <div className="space-y-8 text-center animate-in fade-in slide-in-from-bottom-2 duration-500 pt-4">
      <div className="mx-auto w-16 h-16 bg-green-50 text-green-600 rounded-full flex items-center justify-center shadow-inner">
        <ShieldCheck className="w-9 h-9" />
      </div>
      <div className="space-y-2">
        <h3 className="text-lg font-bold text-slate-900">
          본인 확인을 진행합니다
        </h3>
        <p className="text-sm text-slate-500 leading-relaxed px-4">
          안전한 서비스 이용과 도용 방지를 위해
          <br />
          휴대폰 본인 인증 단계가 필요합니다.
        </p>
      </div>
      <Button
        variant="outline"
        className="w-full py-8 border-2 border-slate-200 hover:border-blue-600 hover:bg-blue-50 transition-all group"
      >
        <div className="flex flex-col items-center">
          <span className="text-base font-bold text-slate-700 group-hover:text-blue-700">
            휴대폰 본인 인증
          </span>
          <span className="text-xs text-slate-400 mt-1 font-normal">
            NICE / PASS 본인확인 서비스
          </span>
        </div>
      </Button>
    </div>
  );
}
