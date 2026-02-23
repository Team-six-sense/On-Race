'use client';

import { useEffect, useState } from 'react';
import { Search as SearchIcon } from 'lucide-react';
import { MarathonCard, ScheduleFilter } from '@/features/schedule/components';
import { useMarathonFilter } from '@/features/schedule/hooks';
import { MarathonEvent } from '@/features/schedule/types';
import { scheduleService } from '@/features/schedule/services';

export default function MarathonSchedulePage() {
  const [events, setEvents] = useState<MarathonEvent[]>([]);

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
      <header className="bg-primary text-black pt-4 pb-0 px-4 ">
        <div className="max-w-5xl mx-auto">
          <h1 className="text-3xl font-bold mb-2">이벤트 목록</h1>
          <p className="opacity-80">참여 가능한 이벤트를 확인하고 신청하세요</p>
        </div>
      </header>

      <main className="max-w-[1100px] mx-auto px-6 pt-6 pb-20 space-y-8">
        {/* 2. Controls */}
        {/* <ScheduleControls
          searchTerm={searchTerm}
          onSearchChange={setSearchTerm}
        /> */}
        <ScheduleFilter />

        <div className="my-2">참여 가능 이벤트 ({filteredEvents.length}개)</div>

        {/* 3. Grid */}
        <div className="grid gap-6">
          {filteredEvents.map((event) => (
            <MarathonCard key={event.id} event={event} />
          ))}
        </div>
        {/* 4. Empty State */}
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
