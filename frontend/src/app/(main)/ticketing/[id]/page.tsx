'use client';

import { useEffect, useState } from 'react';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';

import {
  EventProductInfo,
  EventSalesInfo,
  EventEntryInfo,
} from '@/features/ticketing/components';
import { EventThumbnail } from '@/features/ticketing/components';
import { EntryConfirmModal } from '@/features/ticketing/components/details/entry';
import { useEventStore } from '@/features/event/store/useEventStore';
import { EventDetails } from '@/features/event/types';
import { useParams } from 'next/navigation';
import { eventService } from '@/features/event/services';
import { useDetailedTracker } from '@/features/ticketing/hooks/useDetailedTracker';
import { collectFingerprint } from '@/lib/fingerprint';

export default function MarathonDetailPage() {
  const params = useParams();
  const { event } = useEventStore();

  const [mounted, setMounted] = useState<boolean>(false);
  const [isUserModalOpen, setIsUserModalOpen] = useState(false);

  const [activeTab, setActiveTab] = useState('product');
  const [eventDetails, setEventDetails] = useState<EventDetails>();

  const [minDistance, setMinDistance] = useState(10);

  const { isRecording, logs, startTracking, stopTracking, getFinalData } =
    useDetailedTracker();

  const handleStart = () => {
    startTracking(minDistance);
  };
  const handleStop = () => {
    stopTracking();
    const fp = collectFingerprint();
    // handleDownloadBinary();
  };

  const handleDownloadBinary = () => {
    const currentLogs = isRecording ? [...logs] : logs;
    if (currentLogs.length === 0) return;

    /**
     * [바이너리 구조 설계]
     * 1개 로그당 바이트 계산:
     * - x: 4 bytes (Int32)
     * - y: 4 bytes (Int32)
     * - timestamp: 8 bytes (Float64 - 밀리초 단위 정밀도 유지)
     * - eventType: 1 byte (Uint8 - 0: move, 1: down, 2: up)
     * 총 합계: 17 bytes per log
     */
    const RECORD_SIZE = 17;
    const buffer = new ArrayBuffer(currentLogs.length * RECORD_SIZE);
    const view = new DataView(buffer);

    // 이벤트 타입 매핑
    const typeMap: Record<string, number> = {
      pointermove: 0,
      pointerdown: 1,
      pointerup: 2,
    };

    currentLogs.forEach((log, index) => {
      const offset = index * RECORD_SIZE;

      view.setInt32(offset, Math.floor(log.x), true); // x 저장
      view.setInt32(offset + 4, Math.floor(log.y), true); // y 저장
      view.setFloat64(offset + 8, log.timestamp, true); // timestamp 저장
      view.setUint8(offset + 16, typeMap[log.eventType] ?? 255); // eventType 저장
    });

    // Blob 생성 (application/octet-stream은 일반적인 이진 파일 형식)
    const blob = new Blob([buffer], { type: 'application/octet-stream' });
    const url = URL.createObjectURL(blob);

    const link = document.createElement('a');
    link.href = url;
    link.download = `mouse_data_${new Date().getTime()}.bin`; // 확장자를 .bin으로 변경
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  // 데이터 로드
  useEffect(() => {
    const fetchData = async () => {
      if (!mounted) return;

      try {
        if (event) {
          const response = await eventService.getEventDetails(
            Number(params.id),
          );
          setEventDetails((prev) => ({
            ...prev,
            ...response.data,
          }));
        }
      } catch (error) {
        console.error('데이터 로드 실패:', error);
      }
    };

    fetchData();
  }, [mounted, params.id]);

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />;
  }

  if (!event) {
    return <div className="p-10 text-center">대회를 찾을 수 없습니다.</div>;
  }

  // 공통 탭 버튼 스타일
  const getTabClass = (tabName: string) => {
    const isActive = activeTab === tabName;
    return `
      w-32 py-3 text-center text-sm  transition-all duration-200 relative
      ${isActive ? 'font-bold' : 'font-medium'}
    `;
  };

  return (
    <div className="max-w-7xl mx-auto px-10 pt-8 pb-0">
      {eventDetails && (
        <div className="flex flex-col lg:flex-row gap-8 ">
          {/* 좌측: 대회 상세 정보 영역 */}
          <div className="flex-1">
            <EventThumbnail thumbnailImg={eventDetails.thumbnailImg} />

            {/* 구분선 */}
            <div className="py-2"></div>

            <div className="p-2 ">
              <div className="w-full">
                {/* 탭 메뉴: 왼쪽 정렬 및 탭 간 간격 설정 */}
                <div className="flex border-b border-gray-200 mb-6">
                  <Button
                    variant="text"
                    onClick={() => setActiveTab('product')}
                    className={cn(
                      getTabClass('product'),
                      'flex-1 relative rounded-none py-4',
                    )}
                  >
                    상세정보
                    {/* 활성화 시 나타나는 언더바 */}
                    {activeTab === 'product' && (
                      <div className="absolute bottom-0 w-full left-0 h-0.5 bg-black transition-all" />
                    )}
                  </Button>
                  <Button
                    variant="text"
                    onClick={() => setActiveTab('sales')}
                    className={cn(
                      getTabClass('sales'),
                      'flex-1 relative rounded-none py-4',
                    )}
                  >
                    판매정보
                    {/* 활성화 시 나타나는 언더바 */}
                    {activeTab === 'sales' && (
                      <div className="absolute bottom-0 w-full left-0 h-0.5 bg-black transition-all" />
                    )}
                  </Button>
                </div>

                {/* 메인 컨텐츠 영역 */}
                <div className="p-2">
                  {/* 상품정보 탭 */}
                  <div className={activeTab === 'product' ? 'block' : 'hidden'}>
                    <EventProductInfo
                      event={event}
                      eventDetails={eventDetails}
                    />
                  </div>
                  {/* 판매정보 탭 */}
                  <div className={activeTab === 'sales' ? 'block' : 'hidden'}>
                    <EventSalesInfo />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* 우측: 참여 정보 카드 (Sidebar) */}
          <div className="w-full lg:w-[380px]">
            <div className="sticky top-5">
              <EventEntryInfo
                event={event}
                setIsUserModalOpen={setIsUserModalOpen}
                onStart={handleStart}
              />
            </div>
          </div>

          {/* 사용자 정보 확인 Modal */}
          <EntryConfirmModal
            isUserModalOpen={isUserModalOpen}
            setIsUserModalOpen={setIsUserModalOpen}
            onStop={handleStop}
          />
        </div>
      )}
    </div>
  );
}
