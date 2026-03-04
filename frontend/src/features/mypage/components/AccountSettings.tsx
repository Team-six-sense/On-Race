'use client';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useRouter } from 'next/navigation';

import { useEffect, useState } from 'react';
import { LuChevronRight } from 'react-icons/lu';

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
    <div className="min-h-screen bg-white">
      <div className="max-w-4xl mx-auto space-y-6">
        {/* 회원정보 카드 */}
        <div className="bg-white rounded-sm border border-gray-400 overflow-hidden">
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-3">
                <h2 className="text-xl font-semibold text-gray-800">
                  회원 정보
                </h2>
              </div>
              <button className="text-sm bg-gray-100 p-3 border border-gray-300">
                계정 유형: 이메일 계정
              </button>
            </div>
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-3">
                <h2 className="text-sm font-semibold text-gray-800">
                  본인인증 여부
                </h2>
              </div>
              <div className="flex items-center">
                <div className="text-sm bg-yellow-100 px-2 py-0.5 border border-yellow-300">
                  미인증
                </div>
                <div className="text-sm bg-yellow-100 px-2 py-0.5 border border-yellow-300">
                  인증대기
                </div>
                <div className="text-sm bg-yellow-100 px-2 py-0.5 border border-yellow-300">
                  미인증
                </div>
                <LuChevronRight />
              </div>
            </div>

            <div className="flex flex-col items-center gap-8">
              <div className="flex-1 grid grid-cols-2 gap-4 w-full">
                <div>
                  <label className="text-sm text-gray-500">이름</label>
                  <p className="font-medium text-gray-900">홍길동</p>
                </div>
                <div>
                  <label className="text-sm text-gray-500">이메일</label>
                  <div className="flex items-center gap-2">
                    <p className="font-medium text-gray-900">
                      example@email.com
                    </p>
                  </div>
                </div>
                <div>
                  <label className="text-sm text-gray-500">휴대폰 번호</label>
                  <div className="flex items-center gap-2">
                    <p className="font-medium text-gray-900">010-1234-5678</p>
                  </div>
                </div>
              </div>
            </div>
            <div className="text-right">
              <Button variant="primary1" size="fit" rounded="sm">
                회원 정보 변경
              </Button>
            </div>
          </div>
        </div>

        {/* 비밀번호 변경 카드 */}
        <div className="bg-white rounded-sm border border-gray-400 overflow-hidden">
          <div className="p-6">
            <div className="flex items-center justify-between gap-3">
              <h2 className="ftext-xl font-semibold text-gray-800">
                비밀번호 변경
              </h2>
              <Button variant="primary1" size="fit" rounded="sm">
                비밀번호 변경
              </Button>
            </div>
          </div>
        </div>

        {/* 비밀번호 변경 카드(이메일) */}
        <div className="bg-white rounded-sm border border-gray-400 overflow-hidden">
          <div className="p-6">
            <div className="flex flex-col gap-3">
              <h2 className="ftext-xl font-semibold text-gray-800">
                비밀번호 변경
              </h2>

              <div className="p-4 bg-blue-100 border-2 border-blue-400">
                <p> example@email.com 으로 인증 메일이 발송되었습니다. </p>
                <p> 메일함을 확인하시고 인증 링크를 클릭해주세요. </p>
              </div>

              <div className="p-4 bg-gray-100 border-1 border-gray-400">
                <p> 인증 메일 유효 시간: 15분 </p>
                <p>
                  * 인증 메일은 1분 간격, 하루 최대5회까지 재발송할 수 있습니다.
                </p>
              </div>

              <div className="text-right">
                <Button variant="primary1" size="fit" rounded="sm">
                  비밀번호 변경
                </Button>
              </div>
            </div>
          </div>
        </div>

        {/* 배송지 정보 카드 */}
        <div className="bg-white rounded-sm border border-gray-400 overflow-hidden">
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-3">
                <h2 className="text-xl font-semibold text-gray-800">
                  배송지 정보
                </h2>
              </div>
            </div>

            <div className="flex flex-col gap-2 mb-4">
              <div>
                <span className="text-xs bg-gray-100 px-2 py-0.5 ">
                  기본배송지
                </span>
              </div>
              <div className="flex flex-col items-center gap-2 mb-1">
                <Input label="수령인" />
                <Input label="주소 별칭" />
                <Input label="배송지" />
                <Input label="전화번호" />
              </div>
            </div>
            <div className="text-right">
              <Button variant="outline" size="fit" rounded="sm">
                변경
              </Button>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-sm border border-gray-400 overflow-hidden">
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-3">
                <h2 className="text-xl font-semibold text-gray-800">
                  배송지 정보
                </h2>
              </div>
            </div>

            <div className="flex flex-col gap-2 mb-4">
              <div>
                <span>등록된 배송지 정보가 없습니다.</span>
              </div>
            </div>
            <div className="text-right">
              <Button variant="primary1" size="fit" rounded="sm">
                등록하기
              </Button>
            </div>
          </div>
        </div>

        {/* 마케팅 및 광고 알림 설정 카드 */}
        <div className="bg-white rounded-sm border border-gray-400 overflow-hidden">
          <div className="p-6">
            <div className="flex items-center gap-3 mb-6">
              <h2 className="text-xl font-semibold text-gray-800">
                마케팅 및 알림 설정
              </h2>
            </div>

            <div className="p-4 bg-gray-100 border-1 border-gray-300 mb-6">
              <p>
                마케팅 정보 수신 동의는 선택 항목이며, 미동의 상태에서도
                서비스를 이용하실 수 있습니다.
              </p>
            </div>

            <div className="p-4 bg-gray-100 border-1 border-gray-300">
              {/* 이메일 알림 */}
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">
                    마케팅 정보 수신 동의 (선택)
                  </p>
                  <p className="text-sm text-gray-500">
                    이벤트 알림, 프로모션 메시지 등의 마케팅 정보를 받습니다.
                  </p>
                </div>
                <Toggle
                  enabled={marketingSettings.email}
                  onToggle={() => toggleSetting('email')}
                />
              </div>
            </div>

            <div>
              <p className="text-xs text-gray-400">
                • SMS, 이메일을 통해 이벤트 및 프로모션 정보를 제공합니다
              </p>
              <p className="text-xs text-gray-400">
                • 설정 변경은 즉시 반영됩니다
              </p>
            </div>
          </div>
        </div>
        <div className="text-right">
          <Button
            variant="outline"
            size="fit"
            rounded="sm"
            onClick={() => router.push('/mypage/withdraw/')}
          >
            회원탈퇴
          </Button>
        </div>
      </div>
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
        enabled ? 'bg-blue-600' : 'bg-gray-200'
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
