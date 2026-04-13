'use client';

import { Button } from '@/components/ui/button';
import { useEffect, useState } from 'react';
import { LuCircleAlert } from 'react-icons/lu';

import AddressForm from './AddressForm';
import { myPageService } from '../services';
import { AccountInfo } from '../types/accountInfo';
import PassModal from '@/features/auth/components/PassModal';

export function AccountSettings() {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isPassModalOpen, setPassIsModalOpen] = useState(false);
  const [userInfo, setUserInfo] = useState<AccountInfo>();

  const [marketingSettings, setMarketingSettings] = useState({
    email: true,
    sms: false,
    push: true,
  });

  const formatPhoneNumber = (phoneNumber: string) => {
    return phoneNumber
      .replace(/[^0-9]/g, '')
      .replace(/^(\d{2,3})(\d{3,4})(\d{4})$/, `$1-$2-$3`);
  };
  const openModal = () => setIsModalOpen(true);
  const closeModal = () => setIsModalOpen(false);

  const toggleSetting = (key: keyof typeof marketingSettings) => {
    setMarketingSettings((prev) => ({ ...prev, [key]: !prev[key] }));
  };
  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      if (!mounted) return;

      try {
        const response = await myPageService.getAccountInfo();
        setUserInfo((prev) => ({
          ...prev,
          ...response.data,
        }));
      } catch (error) {
        console.error('데이터 로드 실패:', error);
      }
    };

    fetchData();
  }, [mounted]);

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <div className="min-h-screen bg-white">
      {userInfo && (
        <div className="space-y-20">
          <section>
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-3">
                <h2 className="text-xl font-bold text-black">회원 정보</h2>
              </div>
            </div>
            <div className="flex flex-col gap-4">
              <div className="flex">
                <span className="w-30 text-base font-medium text-font-medium">
                  이름
                </span>
                <span className="flex-1 text-base font-medium">
                  {userInfo.name}
                </span>
              </div>
              <div className="flex">
                <span className="w-30 text-base font-medium text-font-medium">
                  휴대폰 번호
                </span>
                <span className="flex-1 text-base font-medium">
                  {formatPhoneNumber(userInfo.phoneNumber)}
                </span>
              </div>
              <div className="flex items-center">
                <span className="w-30 text-base font-medium text-font-medium">
                  이메일(ID)
                </span>
                <span className="flex-1 text-base font-medium flex items-center gap-2">
                  <img
                    src="/favicon.ico"
                    alt="email icon"
                    className="w-5 h-5 object-contain shrink-0"
                  />
                  <span className="whitespace-nowrap">{userInfo.email}</span>
                </span>
              </div>
              <div className="flex items-center">
                <span className="w-30 text-base font-medium text-font-medium">
                  비밀번호
                </span>
                <span className="flex text-base font-medium mr-4">******</span>
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
                <span className="w-30 text-base font-medium text-font-medium">
                  본인인증 상태
                </span>
                <span className="flex-1 text-base font-medium ">미인증</span>
              </div>

              <div className="flex items-center justify-between p-4 bg-red-50 border border-red-100 text-font-error rounded-sm">
                <div className="flex items-center">
                  <LuCircleAlert size={24} className="mr-2" />
                  <span>
                    최초 1회의 본인인증 이후 모든 서비스 이용이 가능합니다.
                  </span>
                </div>
                <div className="flex">
                  <Button
                    variant="destructive"
                    rounded="full"
                    onClick={() => setPassIsModalOpen(true)}
                  >
                    인증하기
                  </Button>
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
              {userInfo.addressList && userInfo.addressList.length > 0 ? (
                userInfo.addressList.map((address) => (
                  <div
                    key={address.id}
                    className={`relative p-2 transition-all`}
                  >
                    <div className="flex items-center gap-2 mb-2">
                      <span className="font-bold text-gray-900 text-lg">
                        {address.label}
                      </span>
                      {address.isDefault && (
                        <span className="px-2 py-0.5 text-[10px] font-bold text-font-medium bg-gray-100 rounded-sm">
                          기본 배송지
                        </span>
                      )}
                    </div>

                    <div className="mb-1">
                      <p className="text-gray-800 font-medium">
                        {address.address1}, {address.address2}
                      </p>
                    </div>

                    <div className="text-sm text-gray-500 flex items-center gap-1">
                      <span>{address.receiverName}</span>
                      <span>•</span>
                      <span>{formatPhoneNumber(address.phoneNumber)}</span>
                    </div>

                    <div className="absolute top-3 right-3 flex flex-col gap-1">
                      <div>
                        <Button variant="outline" size="sm">
                          수정
                        </Button>
                      </div>
                      <div>
                        <Button variant="outline" size="sm">
                          삭제
                        </Button>
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <div className="flex items-center justify-center text-font-medium">
                  등록된 배송지가 없습니다
                </div>
              )}

              <div className="max-w-2xl mx-auto flex items-center justify-center">
                <Button
                  variant="outline"
                  rounded="full"
                  className="border-gray-300 text-gray-500"
                  onClick={openModal}
                >
                  신규 배송지 추가
                </Button>
                {isModalOpen && (
                  <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
                    <div className="relative w-full max-w-md bg-white rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
                      <AddressForm onClose={closeModal} />
                    </div>
                  </div>
                )}
              </div>
            </div>
          </section>
          <section>
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-3">
                <h2 className="text-xl font-bold text-gray-800">
                  마케팅 및 알림 설정
                </h2>
              </div>
            </div>

            <div className="flex flex-col gap-4">
              <div className="p-4 bg-secondary border-1 border-gray-300">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-black">
                      <span className="text-gray-500 pr-1">[선택]</span>
                      마케팅 정보 수신 동의
                    </p>
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
          <PassModal
            isOpen={isPassModalOpen}
            onClose={() => setPassIsModalOpen(false)}
            onFinish={() => {
              setPassIsModalOpen(false);
            }}
          />
        </div>
      )}
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
