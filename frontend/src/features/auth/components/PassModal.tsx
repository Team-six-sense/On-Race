'use client';

import React, { useEffect, useState } from 'react';
import {
  Modal,
  ModalContent,
  ModalHeader,
  ModalTitle,
} from '@/components/ui/modal'; // 실제 경로에 맞춰 수정하세요
import { LuChevronLeft, LuCircleCheck, LuX } from 'react-icons/lu';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

type AuthStep = 'SELECT_AGENCY' | 'INPUT_INFO' | 'WAITING' | 'SUCCESS';

interface PassModalProps {
  isOpen: boolean;
  onClose: () => void;
  onFinish: () => void;
}

const providers = [
  { id: 'SKT', name: 'SKT' },
  { id: 'KT', name: 'KT' },
  { id: 'LG', name: 'LG U+' },
  { id: 'SKTM', name: 'SKT 알뜰폰' },
  { id: 'KTM', name: 'KT 알뜰폰' },
  { id: 'LGM', name: 'LG U+ 알뜰폰' },
];

export default function PassModal({
  isOpen,
  onClose,
  onFinish,
}: PassModalProps) {
  const [step, setStep] = useState<AuthStep>('SELECT_AGENCY');
  const [selectedAgency, setSelectedAgency] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const [name, setName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');

  // 모달 닫힐 때 상태 리셋
  useEffect(() => {
    if (!isOpen) {
      const timer = setTimeout(() => setStep('SELECT_AGENCY'), 300);
      return () => clearTimeout(timer);
    }
  }, [isOpen]);

  const handleCheckAuth = () => {
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      setStep('SUCCESS');
    }, 1500);
  };

  return (
    <Modal open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <ModalContent size="md" className="p-8">
        {/* 상단 헤더 커스텀 제어 */}
        {step !== 'SUCCESS' && (
          <div className="flex items-center justify-between">
            {step !== 'SELECT_AGENCY' && (
              <Button
                variant="ghost"
                size="fit"
                onClick={() =>
                  setStep(step === 'WAITING' ? 'INPUT_INFO' : 'SELECT_AGENCY')
                }
              >
                <LuChevronLeft size={24} />
              </Button>
            )}
            <ModalTitle className="text-lg font-bold">PASS 인증</ModalTitle>
            <Button variant="ghost" size="fit" onClick={onClose}>
              <LuX size={24} />
            </Button>
          </div>
        )}

        {/* 단계별 컨텐츠 */}
        <div className="max-h-[300px] flex flex-col justify-center">
          {/* STEP 1: 통신사 선택 */}
          {step === 'SELECT_AGENCY' && (
            <div className="animate-in fade-in duration-500">
              <h4 className="text-xl font-bold text-center mb-2">
                통신사 선택
              </h4>
              <p className="text-center text-sm text-gray-500 mb-8">
                이용 중인 통신사를 선택해 주세요.
              </p>
              <div className="grid grid-cols-2 gap-3">
                {providers.map((p) => (
                  <Button
                    key={p.id}
                    variant="outline"
                    onClick={() => {
                      setSelectedAgency(p.name);
                      setStep('INPUT_INFO');
                    }}
                  >
                    {p.name}
                  </Button>
                ))}
              </div>
            </div>
          )}

          {/* STEP 2: 정보 입력 */}
          {step === 'INPUT_INFO' && (
            <div className="animate-in fade-in duration-500">
              <div className="mb-8">
                <span className="inline-block px-2 py-1 bg-blue-50 text-blue-500 font-bold text-xs rounded-sm mb-1">
                  {selectedAgency}
                </span>
                <h4 className="text-2xl font-bold">정보를 입력해주세요</h4>
              </div>
              <div className="space-y-4">
                <div className="flex gap-2 space-y-2 mb-4">
                  <Input
                    variant="primary"
                    label="이름"
                    placeholder="이름을 입력해주세요"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                  />
                </div>
                <div className="flex gap-2 space-y-2 mb-8">
                  <Input
                    variant="primary"
                    label="휴대폰번호"
                    placeholder="휴대폰번호를 입력해주세요"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                  />
                </div>
              </div>
              <div>
                <Button
                  variant="primary1"
                  rounded="full"
                  onClick={() => setStep('WAITING')}
                >
                  인증 요청
                </Button>
              </div>
            </div>
          )}

          {/* STEP 3: 푸시 대기 */}
          {step === 'WAITING' && (
            <div className="text-center animate-in fade-in duration-500">
              <div className="w-20 h-20 bg-blue-600 rounded-3xl mx-auto flex items-center justify-center mb-8 shadow-xl">
                <span className="text-white font-black text-xl italic">
                  PASS
                </span>
              </div>
              <h4 className="text-xl font-bold mb-4">
                {selectedAgency} PASS 앱으로
                <br />
                인증 요청을 보냈습니다.
              </h4>
              <Button
                variant="primary1"
                rounded="full"
                onClick={handleCheckAuth}
                disabled={isLoading}
              >
                {isLoading ? (
                  <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                ) : (
                  '인증 완료 확인'
                )}
              </Button>
            </div>
          )}

          {/* STEP 4: 인증 완료 */}
          {step === 'SUCCESS' && (
            <div className="flex flex-col items-center justify-center animate-in fade-in zoom-in duration-500">
              <LuCircleCheck size={60} className="my-4 text-font-medium" />

              <ModalTitle className="text-2xl font-bold">
                본인인증 완료
              </ModalTitle>
              <p className="text-sm text-gray-500 pb-4">
                안전하게 본인 확인이 완료되었습니다.
              </p>
              <Button variant="primary1" rounded="sm" onClick={onFinish}>
                확인
              </Button>
            </div>
          )}
        </div>
      </ModalContent>
    </Modal>
  );
}
