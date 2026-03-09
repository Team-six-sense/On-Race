'use client';

import { Button } from '@/components/ui/button';

import { Label } from '@/components/shadcn/label';
import { Checkbox } from '@/components/ui/checkbox';

import { useRouter } from 'next/navigation';

export default function SignupForm() {
  const router = useRouter();

  return (
    <div className="flex-1 flex flex-col items-center justify-center bg-primary">
      <div className="flex flex-col w-full max-w-xl p-8 ">
        <div className="text-2xl font-bold py-4">이메일 회원가입</div>
        <div className="text-lg font-bold py-4">약관 동의</div>

        <div className="rounded-xl space-y-4 ">
          {/* 전체 동의 */}
          <div className="flex items-center space-x-3 pb-3 border-b">
            <Checkbox id="all" variant="primary" />
            <Label
              htmlFor="all"
              className="text-base font-bold cursor-pointer text-slate-800"
            >
              전체 동의
            </Label>
          </div>
          <div className="space-y-2">
            {/* 필수 약관들 */}
            <div className="text-sm font-bold text-gray-700">필수 약관</div>
            <div className="space-y-3 px-1 pb-2">
              <div className="flex items-center justify-between group">
                <div className="flex items-center space-x-3">
                  <Checkbox id="t1" variant="primary" />
                  <Label
                    htmlFor="t1"
                    className="text-sm cursor-pointer text-black"
                  >
                    이용약관 동의
                  </Label>
                </div>
                <Button variant="link" size="fit" className="text-xs h-5">
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
                  </Label>
                </div>
                <Button variant="link" size="fit" className="text-xs h-5">
                  전체보기
                </Button>
              </div>
            </div>

            {/* 선택 약관 (마케팅 수신) */}
            <div className="space-y-3 px-1 pt-1">
              <div className="text-sm font-bold text-gray-700">선택 약관</div>
              <div className="flex items-center space-x-3">
                <Checkbox id="marketing" variant="primary" />
                <Label
                  htmlFor="marketing"
                  className="text-sm cursor-pointer text-black"
                >
                  마케팅 정보 수신 동의 (이메일)
                </Label>
              </div>
              <div className="flex items-center space-x-3">
                <Checkbox id="marketing" variant="primary" />
                <Label
                  htmlFor="marketing"
                  className="text-sm cursor-pointer text-black"
                >
                  마케팅 정보 수신 동의 (SMS)
                </Label>
              </div>
            </div>

            {/* 구분선 */}
            <div className="my-3 h-[1px] bg-gray-300"></div>

            <div className="space-y-3 px-1 pt-1">
              <Button
                variant="primary1"
                rounded="full"
                onClick={() => router.push('/signup/user-info')}
              >
                PASS로 본인인증 하기
              </Button>
              <Button
                variant="outline"
                className="border-gray-300 text-gray-400"
                rounded="full"
                onClick={() => router.push('/signup/user-info')}
              >
                다음
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
