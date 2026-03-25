'use client';

import { Suspense, useEffect, useState } from 'react';
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
import { LuSearch } from 'react-icons/lu';

function EventContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [events, setEvents] = useState<Event[]>([]);
  const { setEvent } = useEventStore();

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
        setEvents(response.data.content);
      } catch (error: any) {
        throw new Error(error);
      }
    };

    fetchData();
    setCategoryType();
  }, []);

  const setCategoryType = () => {
    const category = searchParams.get('category');
    if (category) {
      setSearchType(category);
    }
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
          month: 'long', // "3월"
          day: 'numeric', // "15일"
          hour: 'numeric', // "9시"
          minute: 'numeric',
          hour12: false, // 24시간 형식
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

  return (
    <div className="min-h-screen bg-primary1">
      {/* Header */}
      <header className="bg-primary text-black py-6  px-4 ">
        <div className="max-w-5xl mx-auto">
          <h2 className="text-3xl font-bold mb-2">이벤트 </h2>
          <p className="opacity-80">
            참여 가능한 러닝 이벤트를 확인하고 신청해 보세요!
          </p>
        </div>
      </header>

      <main className="max-w-[1100px] mx-auto px-6 pt-6 pb-20 space-y-8">
        <EventFilter
          setSearchTerm={setSearchTerm}
          setSearchDate={setSearchDate}
          setSearchLocation={setSearchLocation}
          setSearchDistance={setSearchDistance}
        />

        <div className="flex flex-wrap gap-2">
          {TYPE.map((type) => (
            <Button
              key={type.id}
              variant={searchType === type.id ? 'primary1' : 'outline'}
              size="fit"
              rounded="full"
              onClick={() => {
                setSearchType(type.id);
              }}
              className={`
            ${
              searchType === type.id
                ? '' // 선택되었을 때 스타일
                : 'border-gray-400' // 비선택 스타일
            }
            border
          `}
            >
              {type.label}
            </Button>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {filteredEvents.map((event) => (
            <div
              key={event.id}
              className="flex flex-col bg-white rounded-md overflow-hidden hover:shadow-lg cursor-pointer"
              onClick={() => handleEventDetail(event)}
            >
              {/* 이미지 및 상태 칩 영역 */}
              <div className="relative aspect-[16/16] overflow-hidden">
                <img
                  src={'/image/default.png'}
                  alt={'이벤트'}
                  className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                />

                {/* 상태 칩 (예: 접수중, 마감, 예정) */}
                <div className="absolute top-2 left-2">
                  <span className="px-2 py-1 rounded-sm  bg-black text-font-accent text-xs">
                    {getTypeLabel(event.type)}
                  </span>
                </div>
              </div>

              {/* 카드 바디 (내용) 영역 */}
              <div className="p-2 flex flex-col flex-grow">
                <div className="flex-grow">
                  <div className="space-y-1.5">
                    <div className="inline-block px-2 py-0.5 mr-1 rounded-sm text-[10px] font-bold bg-gray-100 text-gray-500">
                      {getAppTypeLabel(event.appType)}
                    </div>
                    <div
                      className={cn(
                        'inline-block px-2 py-0.5 rounded-sm text-[10px] font-bold',
                        getStatusConfig(event.status),
                      )}
                    >
                      {displayStatusLabel(event.status)}
                    </div>

                    <h2 className="font-bold text-black text-lg leading-tight truncate">
                      {event.title}
                    </h2>

                    {/* 주소 및 코스 */}
                    <div className="flex items-center text-gray-500 text-sm min-w-0">
                      <span className="truncate shrink-0 max-w-[120px] sm:max-w-none">
                        {event.venue}
                      </span>
                      <span className="mx-1 shrink-0">·</span>
                      <span className="truncate">
                        {event.courses.map((c: Course) => c.name).join(', ')}
                      </span>
                    </div>
                    {/* 날짜 정보 */}
                    <div className="flex items-center text-gray-400 text-sm">
                      <span>
                        {new Date(event.eventAt).toLocaleDateString('ko-KR', {
                          year: 'numeric',
                          month: '2-digit',
                          day: '2-digit',
                        })}
                      </span>
                    </div>
                  </div>
                </div>

                {/* 가격 정보 */}
                <div className="my-2 flex items-center text-black font-bold">
                  {event.discountRate > 0 && (
                    <p className="text-md text-red-500 mr-2">
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

        {/* Empty State (기존 유지) */}
        {filteredEvents.length === 0 && (
          <div className="text-center py-32 border-2 border-dashed border-slate-200 rounded-3xl bg-white">
            <LuSearch className="mx-auto text-slate-200 mb-4" size={48} />
            <p className="text-slate-400 font-bold text-lg">
              검색 결과가 없습니다.
            </p>
            <p className="text-slate-400 text-sm">
              다른 키워드로 검색해 보시겠어요?
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
