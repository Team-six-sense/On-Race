'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { useQueue } from '@/features/ticketing/hooks';
import { QueueStatusCard } from '@/features/ticketing/components';
import {
  Modal,
  ModalClose,
  ModalContent,
  ModalDescription,
  ModalFooter,
  ModalHeader,
  ModalTitle,
} from '@/components/ui/modal';
import { Button } from '@/components/ui/button';
import { LuCircleAlert } from 'react-icons/lu';

export default function WaitingPage() {
  const params = useParams();
  const router = useRouter();
  const eventId = params.id as string;

  const [openModal, setOpenModal] = useState(false);

  const { status, progress, stopPolling, isStoppedByUser, isError } =
    useQueue(eventId);

  // 자동 이동 로직
  useEffect(() => {
    console.log(status);
    if (status?.status === 'passed' && status?.passToken) {
      // 토큰 저장
      localStorage.setItem(`token_${eventId}`, status.passToken);

      // 즉시 이동 (또는 메시지를 위해 1초 지연 가능)
      router.push(`/ticketing/${eventId}/payment`);
    }
  }, [status, eventId, router]);

  const handleCancelRequest = () => {
    stopPolling();
    router.push('/event');
  };

  if (!status && !isStoppedByUser) {
    return <div className="text-center p-10">대기열 확인 중...</div>;
  }

  return (
    <main className="flex flex-col items-center justify-center p-6 space-y-4">
      {/* 대기열 카드 UI */}
      {!isStoppedByUser && (
        <QueueStatusCard
          status={status}
          progress={progress}
          onCancel={() => setOpenModal(true)}
        />
      )}

      <Modal open={openModal} onOpenChange={setOpenModal}>
        {/* cva를 통해 정의한 size="lg" 적용 */}
        <ModalContent size="lg">
          <ModalHeader className="py-4 text-left">
            <LuCircleAlert
              size={50}
              className="text-font-error block mx-auto mb-6"
            />
            <ModalTitle className="text-3xl font-bold text-left">
              대기를 취소하시겠습니까?
            </ModalTitle>
            <ModalDescription className="text-font-medium text-left">
              현재 대기 순번
              <span className="font-bold">{' ' + status?.position}</span>
              번이 삭제됩니다.
              <br />
              취소 후 다시 대기하시면 마지막 순번으로 배정되어 대기 시간이
              길어질 수 있습니다.
            </ModalDescription>
          </ModalHeader>

          <ModalFooter>
            <ModalClose asChild>
              <Button variant="secondary" rounded="full">
                돌아가기
              </Button>
            </ModalClose>
            <Button
              variant="primary1"
              rounded="full"
              onClick={handleCancelRequest}
            >
              대기 취소
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </main>
  );
}
