'use client';

import { useParams } from 'next/navigation';
import { QueueStatusCard } from '@/features/ticketing/components';
import { useQueue } from '@/features/ticketing/hooks';

/**
 * 대기열 페이지 사용 예시
 * URL 경로 예시: /waiting/concert-2024
 */
export default function WaitingPage() {
  const params = useParams();
  const eventId = params.eventId as string;

  // 1. 커스텀 훅을 사용하여 상태와 부드러운 프로그래스 바 값 추출
  const { status, progress } = useQueue(eventId);

  // 2. 초기 로딩 상태 처리
  if (!status) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-500 font-medium">
            대기 순번을 확인하고 있습니다...
          </p>
        </div>
      </div>
    );
  }

  // 3. 메인 UI 렌더링
  return (
    <main className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-6">
      <QueueStatusCard status={status} progress={progress} />

      <footer className="mt-8 text-gray-400 text-[10px] uppercase tracking-widest">
        Powered by Queue-Gate System v2.0
      </footer>
    </main>
  );
}
