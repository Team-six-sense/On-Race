'use client';

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import PostcodeModal from './PostcodeModal';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Checkbox } from '@/components/ui/checkbox';
import { LuChevronLeft } from 'react-icons/lu';
import { cn } from '@/lib/utils';

// 데이터 타입 정의
type AddressFormData = {
  nickname: string;
  receiver: string;
  contact: string;
  zonecode: string;
  address: string;
  detailAddress: string;
};

export default function AddressForm({ onClose }: { onClose?: () => void }) {
  const [isPostcodeOpen, setIsPostcodeOpen] = useState(false);
  const [nicknameType, setNicknameType] = useState<
    'HOME' | 'OFFICE' | 'MANUAL'
  >('HOME');

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { isValid }, // isValid를 통해 폼 완성 여부 확인
  } = useForm<AddressFormData>({
    mode: 'onChange', // 실시간으로 버튼 활성화 상태를 반영하기 위해 설정
    defaultValues: {
      nickname: '우리집',
      receiver: '',
      contact: '',
      zonecode: '',
      address: '',
      detailAddress: '',
    },
  });

  const handleChipClick = (
    type: 'HOME' | 'OFFICE' | 'MANUAL',
    value: string,
  ) => {
    setNicknameType(type);
    setValue('nickname', value, { shouldValidate: true });
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
                {...register('address', { required: true })}
                readOnly
                placeholder="기본 주소"
              />
            </div>
            <div>
              <Button
                type="button"
                variant="outline"
                onClick={() => setIsPostcodeOpen(true)}
              >
                주소 검색
              </Button>
            </div>
          </div>
          <Input
            {...register('detailAddress', { required: true })}
            placeholder="상세 주소를 입력하세요"
            className="mt-2"
          />
          <div className="flex items-center mt-2">
            <Checkbox className="mr-1" />
            <span className="text-sm text-gray-500">기본 배송지로 저장</span>
          </div>
        </div>

        {/* 배송지 별명 */}
        <div className="flex gap-2 mb-3">
          <Button
            type="button"
            variant={nicknameType === 'HOME' ? 'primary1' : 'outline'}
            size="fit"
            rounded="full"
            onClick={() => handleChipClick('HOME', '우리집')}
            className={cn(nicknameType !== 'HOME' && 'border-gray-400')}
          >
            우리집
          </Button>
          <Button
            type="button"
            variant={nicknameType === 'OFFICE' ? 'primary1' : 'outline'}
            size="fit"
            rounded="full"
            onClick={() => handleChipClick('OFFICE', '회사')}
            className={cn(nicknameType !== 'OFFICE' && 'border-gray-400')}
          >
            회사
          </Button>
          <Button
            type="button"
            variant={nicknameType === 'MANUAL' ? 'primary1' : 'outline'}
            size="fit"
            rounded="full"
            onClick={() => handleChipClick('MANUAL', '')}
            className={cn(nicknameType !== 'MANUAL' && 'border-gray-400')}
          >
            직접입력
          </Button>
        </div>

        {nicknameType === 'MANUAL' && (
          <div className="animate-in fade-in slide-in-from-top-1 duration-200">
            <Input
              {...register('nickname', { required: true })}
              placeholder="배송지 별명을 입력해 주세요"
              autoFocus
            />
          </div>
        )}

        {/* 받는 사람 */}
        <div>
          <label className="block text-sm font-medium">받으실 분*</label>
          <Input {...register('receiver', { required: true })} />
        </div>

        {/* 연락처 */}
        <div>
          <label className="block text-sm font-medium">연락처*</label>
          <Input
            {...register('contact', {
              required: true,
              pattern: /^\d{2,3}-\d{3,4}-\d{4}$/,
            })}
            placeholder="010-0000-0000"
          />
        </div>

        <div className="flex gap-2 pt-4">
          <Button
            rounded="full"
            type="submit"
            disabled={!isValid} // 폼이 유효하지 않으면 버튼 비활성화
          >
            저장하기
          </Button>
        </div>
      </form>

      {isPostcodeOpen && (
        <PostcodeModal
          onComplete={(data) => {
            setValue('zonecode', data.zonecode, { shouldValidate: true });
            setValue('address', data.fullAddress, { shouldValidate: true });
            setIsPostcodeOpen(false);
          }}
          onClose={() => setIsPostcodeOpen(false)}
        />
      )}
    </div>
  );
}
