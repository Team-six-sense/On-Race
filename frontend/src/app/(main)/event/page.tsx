'use client';

import { Suspense, useEffect, useState, useRef } from 'react';
import { EventFilter } from '@/features/event/components';
import { useMarathonFilter } from '@/features/event/hooks';
import { Course, Event } from '@/features/event/types';
import { eventService } from '@/features/event/services';
import { Button } from '@/components/ui/button';
import { useRouter, useSearchParams } from 'next/navigation';
import {
  TYPE,
  getAppTypeLabel,
  getStatusConfig,
  getStatusLabel,
  getTypeLabel,
} from '@/types/constants';
import { useEventStore } from '@/features/event/store/useEventStore';
import { cn } from '@/lib/utils';
import { LuCircleAlert } from 'react-icons/lu';
import { formatKoreanDate } from '@/features/ticketing/utils/date';

function EventContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [events, setEvents] = useState<Event[]>([]);
  const { setEvent } = useEventStore();

  // 무한 스크롤 관련 상태
  const [displayCount, setDisplayCount] = useState(12); // 초기 노출 개수
  const observerRef = useRef<HTMLDivElement>(null); // 하단 감지용 Ref

  const {
    searchType,
    setSearchLocation,
    setSearchTerm,
    setSearchDistance,
    setSearchDate,
    setSearchType,
    filteredEvents,
  } = useMarathonFilter(events);

  // 데이터 로드
  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await eventService.getEvents();

        if (response.success) {
          setEvents(response.data.content);
        }
      } catch (error: any) {
        throw new Error(error);
      }
    };
    fetchData();
    setCategoryType();
  }, []);

  // 필터가 변경될 때마다 노출 개수 초기화
  useEffect(() => {
    setDisplayCount(12);
  }, [filteredEvents.length, searchType]);

  // 무한 스크롤 감지 로직
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && displayCount < filteredEvents.length) {
          setDisplayCount((prev) => prev + 12); // 12개씩 추가
        }
      },
      { threshold: 1.0 },
    );

    if (observerRef.current) observer.observe(observerRef.current);
    return () => observer.disconnect();
  }, [displayCount, filteredEvents.length]);

  const setCategoryType = () => {
    const category = searchParams.get('category');
    if (category) setSearchType(category);
  };

  const discountPrice = (price: number, discountRate: number) => {
    const discountPrice = price * (1 - discountRate / 100);
    return discountPrice.toLocaleString('ko-KR');
  };

  const displayStatusLabel = (status: string) => {
    let displayLabel = '';
    switch (status) {
      case 'READY':
        const dateStr = '2026-03-15T09:00:00';
        const date = new Date(dateStr);
        const formattedDate = new Intl.DateTimeFormat('ko-KR', {
          month: 'long',
          day: 'numeric',
          hour: 'numeric',
          minute: 'numeric',
          hour12: false,
        }).format(date);
        displayLabel = `${formattedDate} ${getStatusLabel(status)}`;
        break;
      case 'CLOSING_SOON':
        displayLabel = `내일 ${getStatusLabel(status)}`;
        break;
      case 'DRAW_COMPLETED':
        displayLabel = getStatusLabel('END');
        break;
      default:
        displayLabel = getStatusLabel(status);
    }
    return displayLabel;
  };

  const handleEventDetail = (event: Event) => {
    setEvent(event);
    router.push(`/ticketing/${event.id}`);
  };

  // 실제로 렌더링할 데이터만 슬라이싱
  const visibleEvents = filteredEvents.slice(0, displayCount);

  return (
    <div className="max-w-7xl mx-auto min-h-screen bg-primary1 px-30">
      <header className="text-black py-6">
        <div>
          <h2 className="text-3xl font-bold">이벤트</h2>
          <div className="my-2 h-[2px] bg-black"></div>
        </div>
      </header>

      <main className="max-w-7xl space-y-6">
        <EventFilter
          setSearchTerm={setSearchTerm}
          setSearchDate={setSearchDate}
          setSearchLocation={setSearchLocation}
          setSearchDistance={setSearchDistance}
        />

        <div className="flex flex-wrap gap-1 ">
          {TYPE.map((type) => (
            <Button
              key={type.id}
              variant="outline"
              size="fit"
              rounded="full"
              onClick={() => setSearchType(type.id)}
              className={cn(
                'font-medium',
                searchType === type.id
                  ? 'border-2 text-black border-black'
                  : 'border text-font-low border-cta-outline',
              )}
            >
              {type.label}
            </Button>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {visibleEvents.map((event) => (
            <div
              key={event.id}
              className="flex flex-col bg-white rounded-md overflow-hidden hover:shadow-lg cursor-pointer"
              onClick={() => handleEventDetail(event)}
            >
              <div className="relative aspect-[16/16] overflow-hidden">
                <img
                  src={'/image/default.png'}
                  alt={'이벤트'}
                  className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                />
                <div className="absolute top-2 left-2">
                  <span className="px-2 py-1 rounded-xs bg-black text-font-accent text-xs">
                    {getTypeLabel(event.type)}
                  </span>
                </div>
              </div>

              <div className="p-2 flex flex-col flex-grow">
                <div className="flex-grow">
                  <div className="space-y-1.5">
                    <div className="inline-block px-2 py-0.5 mr-1 rounded-xs text-xs font-bold bg-gray-100 text-gray-500">
                      {getAppTypeLabel(event.appType)}
                    </div>
                    <div
                      className={cn(
                        'inline-block px-2 py-0.5 rounded-xs text-xs font-bold',
                        getStatusConfig(event.status),
                      )}
                    >
                      {displayStatusLabel(event.status)}
                    </div>
                    <h2 className="font-bold text-black text-xl leading-tight truncate">
                      {event.title}
                    </h2>
                    <div className="flex items-center text-font-low text-sm min-w-0">
                      <span className="truncate shrink-0 max-w-[120px] sm:max-w-none">
                        {event.venue}
                      </span>
                      <span className="mx-1 shrink-0">·</span>
                      <span className="truncate">
                        {event.courses.map((c: Course) => c.name).join(', ')}
                      </span>
                    </div>
                    <div className="flex items-center text-font-low text-sm">
                      <span>{formatKoreanDate(event.eventAt)}</span>
                    </div>
                  </div>
                </div>
                <div className="my-2 flex items-center text-black font-bold">
                  {event.discountRate > 0 && (
                    <p className="text-md text-font-error mr-2">
                      {event.discountRate}%
                    </p>
                  )}
                  <p className="text-xl">
                    {discountPrice(event.minPrice, event.discountRate)}원 ~
                  </p>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 무한 스크롤 트리거 요소 */}
        <div ref={observerRef} className="h-10" />

        {filteredEvents.length === 0 && (
          <div className="text-center p-30 bg-white">
            <LuCircleAlert className="mx-auto text-gray-300 mb-4" size={48} />
            <p className="text-font-medium font-medium text-lg">
              검색 결과가 없습니다 <br /> 다른 키워드로 다시 검색해 주세요
            </p>
          </div>
        )}
      </main>
    </div>
  );
}

export default function EventPage() {
  return (
    <Suspense fallback={null}>
      <EventContent />
    </Suspense>
  );
}
