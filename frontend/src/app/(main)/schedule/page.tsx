'use client';

import { useEffect, useState } from 'react';
import { Search as SearchIcon } from 'lucide-react';
import { MarathonCard, ScheduleFilter } from '@/features/schedule/components';
import { useMarathonFilter } from '@/features/schedule/hooks';
import { MarathonEvent } from '@/features/schedule/types';
import { scheduleService } from '@/features/schedule/services';
import { Button } from '@/components/ui/button';
import { useRouter } from 'next/navigation';
import { LuImage } from 'react-icons/lu';

export default function MarathonSchedulePage() {
  const router = useRouter();
  const [events, setEvents] = useState<MarathonEvent[]>([]);
  const [selected, setSelected] = useState('all');

  const CATEGORIES = [
    { id: 'all', label: '전체보기' },
    { id: 'marathon', label: '마라톤' },
    { id: 'playrun', label: '플레이 런' },
    { id: 'class', label: '러닝클래스' },
    { id: 'etc', label: '기타' },
  ];

  // 데이터 로드
  useEffect(() => {
    const fetchData = async () => {
      try {
        const result = await scheduleService.getSchedule();
        setEvents(result.data);
      } catch (error) {
        console.error('데이터 로드 실패:', error);
      }
    };

    fetchData();
  }, []);

  const { searchTerm, setSearchTerm, filteredEvents } =
    useMarathonFilter(events);

  return (
    <div className="min-h-screen bg-primary1">
      {/* 1. Header */}
      <header className="bg-primary text-black py-6  px-4 ">
        <div className="max-w-5xl mx-auto">
          <h1 className="text-3xl font-bold mb-2">이벤트 </h1>
          <p className="opacity-80">
            참여 가능한 러닝 이벤트를 확인하고 신청해 보세요!
          </p>
        </div>
      </header>

      <main className="max-w-[1100px] mx-auto px-6 pt-6 pb-20 space-y-8">
        <ScheduleFilter />

        <div className="flex flex-wrap gap-2">
          {CATEGORIES.map((category) => (
            <Button
              key={category.id}
              variant="outline"
              size="fit"
              rounded="full"
              onClick={() => setSelected(category.id)}
              className={`
            ${
              selected === category.id
                ? 'border-2! border-black' // 선택되었을 때 스타일
                : 'border-gray-400' // 비선택 스타일
            }
            border
          `}
            >
              {category.label}
            </Button>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {filteredEvents.map((event) => (
            <div
              key={event.id}
              className="flex flex-col bg-white rounded-md overflow-hidden border hover:shadow-lg cursor-pointer"
              onClick={() => router.push(`/ticketing/${event.id}`)}
            >
              {/* 1. 이미지 및 상태 칩 영역 */}
              <div className="relative aspect-[16/16] overflow-hidden">
                {/* <img
                  src={
                    event.id <= 2
                      ? `/contents${event.id}.jpg`
                      : `/contents${event.id}.png`
                  }
                  alt={event.title}
                  className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500 bg-gray-100"
                /> */}
                <div className="flex flex-col items-center justify-center w-full h-full bg-gray-100 text-gray-400 rounded-lg">
                  <LuImage size={48} />
                  {/* 텍스트가 있을 경우를 대비해 mt-2(간격) 유지 */}
                </div>
                {/* 상태 칩 (예: 접수중, 마감, 예정) */}
                <div className="absolute top-4 left-4">
                  <span className="px-3 py-1.5 mr-1 rounded-full text-xs font-bold bg-gray-100">
                    {event.status}
                  </span>
                  <span className="px-3 py-1.5 rounded-full text-xs font-bold bg-gray-100">
                    catagory
                  </span>
                </div>
              </div>

              {/* 2. 카드 바디 (내용) 영역 */}
              <div className="p-6 flex flex-col flex-grow">
                <div className="flex-grow">
                  <h2 className="font-bold text-black">{event.title}</h2>

                  <div className="space-y-2">
                    <div className="flex items-center text-slate-500 text-sm">
                      {/* <CalendarIcon size={16} className="mr-2 shrink-0" /> */}
                      <span>{event.date}</span>
                    </div>
                    <div className="flex items-center text-slate-500 text-sm">
                      {/* <MapPinIcon size={16} className="mr-2 shrink-0" /> */}
                      <span className="mr-2">{event.location}</span>
                      <span>{event.courses.join(', ')}</span>
                    </div>
                    <div className="flex items-center text-black text-md font-bold">
                      {/* <MapPinIcon size={16} className="mr-2 shrink-0" /> */}
                      무료
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* 4. Empty State (기존 유지) */}
        {filteredEvents.length === 0 && (
          <div className="text-center py-32 border-2 border-dashed border-slate-200 rounded-3xl bg-white">
            <SearchIcon className="mx-auto text-slate-200 mb-4" size={48} />
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
