'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { UserConfirmModal } from './UserConfirmModal';
import { EventConfirmModal } from './EventConfirmModal';
import { OptionConfirmModal } from './OptionConfirmModal';

export function EntryConfirmModal({
  isUserModalOpen,
  setIsUserModalOpen,
}: {
  isUserModalOpen: boolean;
  setIsUserModalOpen: React.Dispatch<React.SetStateAction<boolean>>;
}) {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const params = useParams();
  const router = useRouter();
  const [mounted, setMounted] = useState(false);

  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [isOptionModalOpen, setIsOptionModalOpen] = useState(false);
  const [template, setTemplate] = useState(0);

  const userData = {
    name: '홍길동',
    birthDate: '1995.01.15',
    gender: '남성',
    phone: '010-1234-5678',
    email: 'hong @example.com',
  };

  const eventData = {
    name: '한강 벚꽃 러닝 페스티벌',
    eventDate: '2026.04.12 (토) 오전 9:00',
    location: '서울 여의도 한강공원 입구',
  };

  const optionData = {
    course: '10km',
    pace: '6\'30\"\/km',
  };

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  const handleApply = () => {
    router.push(`/ticketing/${params.id}/vqa`);
  };

  return (
    <section>
      <UserConfirmModal
        isOpen={isUserModalOpen}
        onClose={() => setIsUserModalOpen(false)}
        onConfirm={() => {
          setIsUserModalOpen(false);
          setIsEventModalOpen(true);
        }}
        data={userData}
        template={template}
      />
      <EventConfirmModal
        isOpen={isEventModalOpen}
        onClose={() => setIsEventModalOpen(false)}
        onConfirm={() => {
          setIsEventModalOpen(false);
          setIsOptionModalOpen(true);
        }}
        data={eventData}
        template={template}
      />
      <OptionConfirmModal
        isOpen={isOptionModalOpen}
        onClose={() => setIsOptionModalOpen(false)}
        onConfirm={() => {
          setIsOptionModalOpen(false);
          handleApply();
        }}
        data={optionData}
        template={template}
      />
    </section>
  );
}
