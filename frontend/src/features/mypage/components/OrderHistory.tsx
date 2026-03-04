'use client';

import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';
import { useEffect, useState } from 'react';
import { OrderPendingCard } from './OrderPendingCard';
import { OrderCompleteCard } from './OrderCompleteCard';

export function OrderHistory() {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);
  const [activeTab, setActiveTab] = useState<string>('pending');

  const events = [
    {
      id: 1,
      status: '신청중',
      thumbnail:
        'https://images.unsplash.com/photo-1530541930197-ff16ac917b0e?auto=format&fit=crop&w=800&q=80',
      price: 35000,
      title: '서울 마라톤 대회 2026',
      course: '풀코스 (42.195km)',
      pace: '중급 (6:00 ~ 7:00/km)',
      applyDate: '2026.02.10',
      resultDate: '2026.03.15',
      orderDate: '2026.03.15 ~ 2026.03.15',
    },
    {
      id: 4,
      status: '신청 마감',
      thumbnail:
        'https://images.unsplash.com/photo-1530541930197-ff16ac917b0e?auto=format&fit=crop&w=800&q=80',
      price: 35000,
      title: '제주 올레길 마라톤',
      course: '하프 (21.1km)',
      pace: '초보 (7:00 ~ 8:00/km)',
      applyDate: '2026.02.05',
      resultDate: '2026.03.20',
      orderDate: '2026.03.15 ~ 2026.03.15',
    },
  ];

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  const getTabClass = (tabName: string) => {
    const isActive = activeTab === tabName;
    return `
      w-32 py-3 text-center text-sm  transition-all duration-200 relative
      ${isActive ? 'font-bold' : 'font-medium'}
    `;
  };

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <div className="p-2 ">
      <div>
        <h2 className="text-2xl font-bold">주문내역</h2>
      </div>
      <div className="w-full">
        {/* 탭 메뉴: 왼쪽 정렬 및 탭 간 간격 설정 */}
        <div className="flex border-b border-gray-200 mb-6">
          <Button
            variant="text"
            onClick={() => setActiveTab('pending')}
            className={cn(
              getTabClass('pending'),
              'flex-1 relative rounded-none py-4',
            )}
          >
            결제 대기
            {/* 활성화 시 나타나는 언더바 */}
            {activeTab === 'pending' && (
              <div className="absolute bottom-0 w-full left-0 h-0.5 bg-black transition-all" />
            )}
          </Button>
          <Button
            variant="text"
            onClick={() => setActiveTab('completed')}
            className={cn(
              getTabClass('completed'),
              'flex-1 relative rounded-none py-4',
            )}
          >
            결제 완료
            {/* 활성화 시 나타나는 언더바 */}
            {activeTab === 'completed' && (
              <div className="absolute bottom-0 w-full left-0 h-0.5 bg-black transition-all" />
            )}
          </Button>
        </div>

        {/* 메인 컨텐츠 영역 */}
        <div className="p-2">
          {/* 결제 대기 탭 */}
          {activeTab === 'pending' && (
            <div className="space-y-4">
              <section>
                <div>
                  {events.map((event, index) => (
                    <div key={index} className="flex flex-col py-2">
                      <OrderPendingCard key={index} {...event} />
                    </div>
                  ))}
                </div>
              </section>
            </div>
          )}

          {/* 결제 완료 탭 */}
          {activeTab === 'completed' && (
            <div className="space-y-6">
              <section>
                <div>
                  {events.map((event, index) => (
                    <div key={index} className="flex flex-col py-2">
                      <OrderCompleteCard key={index} {...event} />
                    </div>
                  ))}
                </div>
              </section>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
