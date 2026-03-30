'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import PostcodeModal from './PostcodeModal';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { LuChevronLeft } from 'react-icons/lu';
import { cn } from '@/lib/utils';

// 유효성 검사 스키마
const addressSchema = z.object({
  nickname: z.string().min(1, '배송지 별명을 입력해주세요.'),
  receiver: z.string().min(2, '받는 사람 이름을 입력해주세요.'),
  contact: z
    .string()
    .regex(
      /^\d{2,3}-\d{3,4}-\d{4}$/,
      '올바른 연락처 형식을 입력해주세요. (예: 010-1234-5678)',
    ),
  zonecode: z.string().min(1, '우편번호를 검색해주세요.'),
  address: z.string().min(1, '주소를 검색해주세요.'),
  detailAddress: z.string().min(1, '상세 주소를 입력해주세요.'),
});

type AddressFormData = z.infer<typeof addressSchema>;

export default function AddressForm({ onClose }: { onClose?: () => void }) {
  const [isPostcodeOpen, setIsPostcodeOpen] = useState(false);
  const [nicknameType, setNicknameType] = useState<
    'HOME' | 'OFFICE' | 'MANUAL'
  >('HOME');

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<AddressFormData>({
    resolver: zodResolver(addressSchema),
    defaultValues: {
      nickname: '우리집',
    },
  });

  // 칩 클릭 시 처리 함수
  const handleChipClick = (
    type: 'HOME' | 'OFFICE' | 'MANUAL',
    value: string,
  ) => {
    setNicknameType(type);
    setValue('nickname', value);
  };

  const onSubmit = (data: AddressFormData) => {
    console.log('서버로 전송할 데이터:', data);
    alert('배송지가 추가되었습니다.');
    if (onClose) onClose();
  };

  return (
    <div className="p-6 bg-white rounded-xl shadow-md max-w-md mx-auto">
      <div className="flex items-center border-b-2 border-black mb-6">
        <Button variant="ghost" size="icon" onClick={onClose}>
          <LuChevronLeft />
        </Button>
        <h1 className="text-xl font-bold ">배송지 추가</h1>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        {/* 주소 검색 */}
        <div>
          <label className="block text-sm font-medium">주소*</label>
          <div className="flex gap-2">
            <div className="flex-1">
              <Input
                {...register('address')}
                readOnly
                placeholder="기본 주소"
              />
            </div>
            <div>
              <Button variant="outline" onClick={() => setIsPostcodeOpen(true)}>
                주소 검색
              </Button>
            </div>
          </div>
          <div>
            <Input
              {...register('detailAddress')}
              placeholder="상세 주소를 입력하세요"
              className="mt-2"
            />
          </div>

          {(errors.address || errors.detailAddress) && (
            <p className="text-red-500 text-xs mt-1">
              주소를 모두 입력해주세요.
            </p>
          )}

          <div className="flex items-center mt-1">
            <Checkbox size="default" className="border-cta-outline mr-1" />
            <span className="text-sm text-gray-500">기본 배송지로 저장</span>
          </div>
        </div>

        {/* 배송지 별명 */}
        <div className="flex gap-2 mb-3">
          <div>
            <Button
              type="button"
              variant={nicknameType === 'HOME' ? 'primary1' : 'outline'}
              rounded="full"
              onClick={() => handleChipClick('HOME', '우리집')}
              className={cn(
                nicknameType === 'OFFICE'
                  ? '' // 선택되었을 때 스타일
                  : 'border-gray-400', // 비선택 스타일
              )}
            >
              우리집
            </Button>
          </div>
          <div>
            <Button
              type="button"
              variant={nicknameType === 'OFFICE' ? 'primary1' : 'outline'}
              rounded="full"
              onClick={() => handleChipClick('OFFICE', '회사')}
              className={cn(
                nicknameType === 'OFFICE'
                  ? '' // 선택되었을 때 스타일
                  : 'border-gray-400', // 비선택 스타일
              )}
            >
              회사
            </Button>
          </div>
          <div>
            <Button
              type="button"
              variant={nicknameType === 'MANUAL' ? 'primary1' : 'outline'}
              rounded="full"
              onClick={() => handleChipClick('MANUAL', '')}
              className={cn(
                nicknameType === 'OFFICE'
                  ? '' // 선택되었을 때 스타일
                  : 'border-gray-400', // 비선택 스타일
              )}
            >
              직접입력
            </Button>
          </div>
        </div>

        {/* 직접입력 선택 시에만 나타나는 Input */}
        {nicknameType === 'MANUAL' && (
          <div className="animate-in fade-in slide-in-from-top-1 duration-200">
            <Input
              {...register('nickname')}
              placeholder="배송지 별명을 입력해 주세요 (예: 친구집)"
              autoFocus // 직접입력 클릭 시 바로 입력할 수 있도록 포커스
            />
          </div>
        )}

        {errors.nickname && (
          <p className="text-red-500 text-xs mt-1">{errors.nickname.message}</p>
        )}

        {/* 받는 사람 */}
        <div>
          <label className="block text-sm font-medium">받으실 분*</label>
          <Input {...register('receiver')} />
          {errors.receiver && (
            <p className="text-red-500 text-xs mt-1">
              {errors.receiver.message}
            </p>
          )}
        </div>

        {/* 연락처 */}
        <div>
          <label className="block text-sm font-medium">연락처*</label>
          <Input {...register('contact')} placeholder="010-0000-0000" />
          {errors.contact && (
            <p className="text-red-500 text-xs mt-1">
              {errors.contact.message}
            </p>
          )}
        </div>

        <div className="flex gap-2 pt-4">
          <Button rounded="full" type="submit">
            저장하기
          </Button>
        </div>
      </form>

      {/* 주소 검색 모달 */}
      {isPostcodeOpen && (
        <PostcodeModal
          onComplete={(data) => {
            setValue('zonecode', data.zonecode);
            setValue('address', data.fullAddress);
          }}
          onClose={() => setIsPostcodeOpen(false)}
        />
      )}
    </div>
  );
}
