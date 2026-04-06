'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { UserConfirmModal } from './UserConfirmModal';
import { AgreeConfirmModal } from './AgreeConfirmModal';
import { VqaModal } from './VqaModal';

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

  const [isAgreeModalOpen, setIsAgreeModalOpen] = useState(false);
  const [isVqaModalOpen, setIsVqaModalOpen] = useState(false);

  const userData = {
    name: '홍길동',
    birthDate: '1995.01.15',
    gender: '남성',
    phone: '010-1234-5678',
    email: 'hong @example.com',
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
          setIsAgreeModalOpen(true);
        }}
        data={userData}
      />
      <AgreeConfirmModal
        isOpen={isAgreeModalOpen}
        onClose={() => setIsAgreeModalOpen(false)}
        onConfirm={() => {
          setIsAgreeModalOpen(false);
          handleApply();
        }}
      />
      {/* <VqaModal
        isOpen={isVqaModalOpen}
        onClose={() => setIsVqaModalOpen(false)}
        onConfirm={() => {
          setIsVqaModalOpen(false);
          handleApply();
        }}
      /> */}
    </section>
  );
}
