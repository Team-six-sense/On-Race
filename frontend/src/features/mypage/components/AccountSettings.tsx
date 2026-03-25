'use client';

import { Button } from '@/components/ui/button';
import { useRouter } from 'next/navigation';

import { useEffect, useState } from 'react';
import { HiOutlineDotsCircleHorizontal } from 'react-icons/hi';

export function AccountSettings() {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);
  const router = useRouter();
  const [marketingSettings, setMarketingSettings] = useState({
    email: true,
    sms: false,
    push: true,
  });

  const toggleSetting = (key: keyof typeof marketingSettings) => {
    setMarketingSettings((prev) => ({ ...prev, [key]: !prev[key] }));
  };
  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <div className="min-h-screen bg-white space-y-8">
      {/* 회원정보 카드 */}
      <section>
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-bold text-gray-800">회원 정보</h2>
          </div>
        </div>

        <div className="flex flex-col gap-4">
          <div className="flex">
            <span className="w-30 font-semibold text-gray-500">이름</span>
            <span className="flex-1">-</span>
          </div>
          <div className="flex">
            <span className="w-30 font-semibold text-gray-500">
              휴대폰 번호
            </span>
            <span className="flex-1">010-1234-5678</span>
          </div>
          <div className="flex">
            <span className="w-30 font-semibold text-gray-500">이메일(ID)</span>
            <span className="flex-1">u***@email.com</span>
          </div>
          <div className="flex items-center">
            <span className="w-30 font-semibold text-gray-500">비밀번호</span>
            <span className="flex mr-4">******</span>
            <div>
              <Button
                variant="outline"
                size="xs"
                rounded="sm"
                className="text-gray-500"
              >
                변경하기
              </Button>
            </div>
          </div>
          <div className="flex">
            <span className="w-30 font-semibold text-gray-500">
              본인인증 상태
            </span>
            <span className="flex-1 font-bold">인증대기</span>
          </div>

          <div className="p-4 bg-gray-100 border-1 border-gray-100 text-gray-500 rounded-sm">
            <div className="flex items-center">
              <HiOutlineDotsCircleHorizontal size={24} className="mr-2" />
              <span>
                본인인증 정보를 확인하고 있습니다. 잠시만 기다려주시면
                본인인증이 완료됩니다.
              </span>
            </div>
          </div>
        </div>
      </section>

      <section>
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-bold text-gray-800">배송지 정보</h2>
          </div>
        </div>

        <div className="flex flex-col gap-4">
          <div className="flex items-center justify-center text-gray-400">
            등록된 배송지가 없습니다
          </div>
          <div className="max-w-2xl mx-auto flex items-center justify-center">
            <Button
              variant="outline"
              rounded="full"
              className="border-gray-300 text-gray-500"
            >
              신규 배송지 추가
            </Button>
          </div>
        </div>
      </section>

      {/* 마케팅 및 광고 알림 설정 카드 */}
      <section>
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-bold text-gray-800">
              마케팅 및 알림 설정
            </h2>
          </div>
        </div>

        <div className="flex flex-col gap-4">
          <div className="p-4 bg-gray-100 border-1 border-gray-300">
            {/* 이메일 알림 */}
            <div className="flex items-center justify-between">
              <div>
                <p>[선택] 마케팅 정보 수신 동의</p>
                <p className="text-sm text-gray-500">
                  동의하시면 이벤트 알람, 프로모션 메시지 등의 마케팅 정보를
                  받아보실 수 있습니다.
                </p>
              </div>
              <Toggle
                enabled={marketingSettings.email}
                onToggle={() => toggleSetting('email')}
              />
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}

// 토글 스위치 컴포넌트
const Toggle = ({
  enabled,
  onToggle,
}: {
  enabled: boolean;
  onToggle: () => void;
}) => {
  return (
    <button
      onClick={onToggle}
      className={`${
        enabled ? 'bg-black' : 'bg-gray-200'
      } relative inline-flex h-6 w-11 items-center rounded-full transition-colors focus:outline-none`}
    >
      <span
        className={`${
          enabled ? 'translate-x-6' : 'translate-x-1'
        } inline-block h-4 w-4 transform rounded-full bg-white transition-transform`}
      />
    </button>
  );
};
