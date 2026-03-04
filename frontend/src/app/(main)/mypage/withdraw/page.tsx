'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { FaExclamationTriangle } from 'react-icons/fa';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radioGroup';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { Button } from '@/components/ui/button';
import {
  Modal,
  ModalClose,
  ModalContent,
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
  ModalTrigger,
} from '@/components/ui/modal';

const WithdrawPage = () => {
  const router = useRouter();
  const [selectedValue, setSelectedValue] = useState('');
  const [open, setOpen] = useState(false);

  const handleEvent = () => {
    setOpen(false);
  };
  const reason = [
    { id: '1', label: '서비스 이용 빈도가 낮아서' },
    { id: '2', label: '다른 서비스가 더 좋아서' },
    { id: '3', label: '개인정보 보호를 위해' },
    { id: '4', label: '기타' },
  ];

  return (
    <div className="contents items-center justify-center min-h-screen p-4">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-2xl font-bold text-red-900 mb-6">회원 탈퇴</h1>

        <div className="bg-white rounded-sm border-2 border-dashed border-gray-400 overflow-hidden">
          <div className="p-8">
            <div className="flex items-center gap-3 mb-6">
              <div>
                <h1 className="text-xl font-bold text-red-900">
                  정말 탈퇴하시겠습니까?
                </h1>
              </div>
            </div>

            {/* 주의사항 영역 */}
            <div className="bg-red-50 border border-red-100 rounded-sm p-5 mb-4">
              <h3 className="text-red-800 font-semibold mb-3 flex items-center gap-2">
                <FaExclamationTriangle size={18} />
                회원탈퇴 시 주의사항
              </h3>
              <ul className="space-y-2 text-sm text-red-700 opacity-90">
                <li>• 계정 상태가 '비활성화'로 변경됩니다</li>
                <li>• 로그인 및 모든 서비스 이용이 불가능합니다</li>
                <li>• 탈퇴 후 계정 복구가 불가능합니다</li>
                <li>• 회원님의 모든 데이터가 삭제될 수 있습니다</li>
              </ul>
            </div>

            <div className="border-2 border-dashed border-gray-400 rounded-sm p-5 mb-4">
              <h3 className="text-gray-500 mb-3 flex items-center gap-2">
                탈퇴하실 계정 정보:
              </h3>

              <div className="flex flex-col">
                <div className="flex items-center justify-between">
                  <p className="text-gray-500">이메일:</p>
                  <p>example@email.com</p>
                </div>
                <div className="flex items-center justify-between">
                  <p className="text-gray-500">가입일:</p>
                  <p>2026-01-15</p>
                </div>
                <div className="flex items-center justify-between">
                  <p className="text-gray-500">계정 유형:</p>
                  <p>이메일 회원가입</p>
                </div>
              </div>
            </div>

            <div className="mb-4">
              <h3 className="text-gray-900 mb-3 flex items-center gap-2">
                탈퇴 사유 (선택)
              </h3>

              <RadioGroup
                value={selectedValue}
                onValueChange={setSelectedValue}
                className="gap-1"
              >
                {reason.map((item) => (
                  <label
                    key={item.id}
                    htmlFor={item.id}
                    className="flex items-center space-x-3 px-3 py-1 cursor-pointer"
                  >
                    <RadioGroupItem value={item.id} id={item.id} />
                    <span className="text-sm font-medium">{item.label}</span>
                  </label>
                ))}
              </RadioGroup>
            </div>

            <div className="mb-4">
              <Input
                placeholder="[기타 의견 입력한]"
                className="border-1 border-gray-400 rounded-none"
              />
            </div>

            <div className="flex items-center space-x-3 p-4 border-1 border-yellow-200 bg-yellow-50 rounded-sm mb-4">
              <Checkbox id="check" size="default" />
              <label htmlFor="check" className="text-xm">
                위 내용을 모두 확인했으며, 회원탈퇴에 동의합니다 *
              </label>
            </div>

            {/* 구분선 */}
            <div className="my-3 h-[1px] bg-gray-300"></div>

            <div className="flex space-x-3">
              <Button
                variant="outline"
                rounded="sm"
                size="lg"
                onClick={() => router.push('/mypage')}
              >
                취소
              </Button>
              <Modal open={open} onOpenChange={setOpen}>
                <ModalTrigger asChild>
                  <Button variant="destructive" rounded="sm" size="lg">
                    회원탈퇴
                  </Button>
                </ModalTrigger>
                {/* cva를 통해 정의한 size="lg" 적용 */}
                <ModalContent size="sm">
                  <ModalHeader>
                    <ModalTitle className="text-center py-2">
                      탈퇴안내
                    </ModalTitle>
                    <ModalDescription className="text-center py-2">
                      탈퇴가 완료되었습니다.
                      <br />
                      해당 계정으로 서비스 이용이 제한됩니다.
                    </ModalDescription>
                  </ModalHeader>
                  {/* <div className="py-4">
          <p>여기에 모달 본문 내용을 넣습니다.</p>
        </div> */}
                  <ModalFooter>
                    <Button
                      variant="primary1"
                      rounded="none"
                      onClick={handleEvent}
                    >
                      확인
                    </Button>
                  </ModalFooter>
                </ModalContent>
              </Modal>
            </div>

            <div></div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default WithdrawPage;
