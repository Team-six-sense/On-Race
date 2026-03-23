'use client';

import { useState } from 'react';
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

export default function MarathonDetailPage() {
  const { event } = useEventStore();

  const [activeTab, setActiveTab] = useState('product');
  const [isUserModalOpen, setIsUserModalOpen] = useState(false);

  if (!event)
    return <div className="p-10 text-center">대회를 찾을 수 없습니다.</div>;

  // 공통 탭 버튼 스타일
  const getTabClass = (tabName: string) => {
    const isActive = activeTab === tabName;
    return `
      w-32 py-3 text-center text-sm  transition-all duration-200 relative
      ${isActive ? 'font-bold' : 'font-medium'}
    `;
  };

  return (
    <div className="max-w-6xl mx-auto px-4 pt-8 pb-0">
      <div className="flex flex-col lg:flex-row gap-8">
        {/* 좌측: 대회 상세 정보 영역 */}
        <div className="flex-1">
          <EventThumbnail event={event} />

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
                  <EventProductInfo event={event} />
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
          <EventEntryInfo
            event={event}
            setIsUserModalOpen={setIsUserModalOpen}
          />
        </div>

        {/* 사용자 정보 확인 Modal */}
        <EntryConfirmModal
          isUserModalOpen={isUserModalOpen}
          setIsUserModalOpen={setIsUserModalOpen}
        />
      </div>
    </div>
  );
}
