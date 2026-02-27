'use client';

import { useEffect, useRef, useState } from 'react';
import { Button } from '../ui/button';
import { MarathonEvent } from '@/features/schedule/types';
import { scheduleService } from '@/features/schedule/services';
import { useRouter } from 'next/navigation';
import { LuArrowLeft, LuArrowRight, LuCircleAlert } from 'react-icons/lu';

export default function Body() {
  const [events, setEvents] = useState<MarathonEvent[]>([]);
  const router = useRouter();
  const scrollRef = useRef<HTMLDivElement>(null);

  const platformContents = [
    {
      id: 1,
      title: '마라톤',
      subtitle:
        '익숙했던 거리를 새로운 시각으로 바라볼 수 있는 지역 기반 마라톤',
      img: '/contents1.jpg',
    },
    {
      id: 2,
      title: '플레이 런',
      subtitle:
        '보물찾기, 경찰과 도둑처럼 테마를 더해 놀이처럼 즐기는 미니 러닝 이벤트',
      img: '/contents2.jpg',
    },
    {
      id: 3,
      title: '체험단',
      subtitle: 'On의 신제품을 가장 먼저 체험해볼 수 있는 소규모 러닝 이벤트',
      img: '/contents3.png',
    },
    {
      id: 4,
      title: '러닝 클래스',
      subtitle:
        '전문 트레이너·스포츠 인플루언서의 코칭을 받을 수 있는 그룹 클래스',
      img: '/contents4.png',
    },
  ];

  const handleScroll = (direction: 'left' | 'right') => {
    if (scrollRef.current) {
      const scrollAmount = 400;
      const scrollToValue =
        direction === 'left'
          ? scrollRef.current.scrollLeft - scrollAmount
          : scrollRef.current.scrollLeft + scrollAmount;

      scrollRef.current.scrollTo({
        left: scrollToValue,
        behavior: 'smooth',
      });
    }
  };

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

  return (
    <main className="py-4 flex flex-col ">
      {/* --- 플랫폼 소개 (Platform Features) --- */}
      <section className="py-20 bg-white overflow-hidden">
        <div className="max-w-[1200px] mx-auto px-6">
          {/* 상단 텍스트 영역 */}
          <div className="mb-12">
            <h2 className="text-4xl font-bold mb-4">
              러닝을 경험하는 새로운 방식
            </h2>
          </div>

          {/* 가로 스크롤 카드 영역 */}
          <div
            ref={scrollRef}
            className="flex gap-6 overflow-x-auto scrollbar-hide snap-x snap-mandatory pb-4"
            style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}
          >
            {platformContents.map((content) => (
              <div
                key={content.id}
                className="relative min-w-[320px] h-[550px] rounded-lg overflow-hidden snap-start group shadow-xl shadow-gray-200/50"
              >
                {/* 배경 이미지 */}
                <img
                  src={content.img}
                  alt={content.title}
                  className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                />

                {/* 이미지 하단 텍스트를 위한 그라데이션 오버레이 */}
                <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />

                {/* 이미지 왼쪽 하단 텍스트 배치 */}
                <div className="absolute bottom-10 left-8 text-white">
                  <h3 className="text-3xl font-bold tracking-tight">
                    {content.title}
                  </h3>
                  <p className="text-white/70 text-sm font-semibold mb-2">
                    {content.subtitle}
                  </p>
                </div>
              </div>
            ))}
          </div>

          {/* 하단 스크롤 버튼 */}
          <div className="flex gap-4 mt-4">
            <Button
              variant="ghost"
              rounded="sm"
              size="fit"
              onClick={() => handleScroll('left')}
              className="flex items-center justify-centergroup"
            >
              <LuArrowLeft size={30} />
            </Button>
            <Button
              variant="ghost"
              rounded="sm"
              size="fit"
              onClick={() => handleScroll('right')}
              className="flex items-center justify-centergroup"
            >
              <LuArrowRight size={30} />
            </Button>
          </div>
        </div>
      </section>

      {/* --- 이벤트 목록 (Event List) --- */}
      <section className="py-20 bg-white">
        <div className="max-w-[1200px] mx-auto px-6">
          {/* 섹션 타이틀 */}
          <div className="mb-12 flex justify-between items-end">
            <div>
              <h2 className="text-4xl font-black text-black tracking-tighter">
                다가오는 러닝 이벤트
              </h2>
            </div>
            <Button
              variant="text"
              size="fit"
              onClick={() => router.push('/schedule')}
            >
              전체 보기
            </Button>
          </div>

          {/* 이벤트 리스트 */}
          <div>
            {events && events.length > 0 ? (
              events.map((event) => (
                <div
                  key={event.id}
                  className="flex flex-row items-center justify-between px-4 py-8 my-4 bg-gray-50 transition-colors group"
                >
                  <div className="flex items-center w-full md:w-auto mb-4 md:mb-0">
                    {/* 날짜 영역 (왼쪽) */}
                    <div className="flex flex-col items-center justify-center min-w-[80px] border-r border-gray-200 pr-6 mr-8">
                      <span className="text-sm font-bold text-black">
                        {event.month}월
                      </span>
                      <span className="text-3xl font-black text-black">
                        {event.day}
                      </span>
                    </div>

                    {/* 상태/타이틀/지역 영역 (중앙) */}
                    <div className="flex flex-col gap-2">
                      <div className="flex items-center gap-3">
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded-sm bg-gray-200">
                          Category
                        </span>
                        <span className="text-[10px] font-bold px-2 py-0.5 rounded-sm bg-gray-200">
                          {event.status}
                        </span>
                      </div>
                      <h3 className="text-xl font-bold text-black transition-colors">
                        {event.title}
                      </h3>
                      <span className="text-sm font-bold text-gray-500">
                        {event.location}
                      </span>
                    </div>
                  </div>

                  {/* 신청 버튼 영역 (우측 끝) */}
                  <div className="w-full md:w-auto">
                    <Button
                      variant="primary1"
                      rounded="full"
                      size="lg"
                      // disabled={event.status !== '신청중'}
                      onClick={() => router.push(`/ticketing/${event.id}`)}
                    >
                      신청하기
                    </Button>
                  </div>
                </div>
              ))
            ) : (
              <div className="flex flex-col items-center justify-center py-10 my-4 bg-gray-50 transition-colors">
                <div className="py-2 text-gray-400">
                  <LuCircleAlert size={40} />
                </div>

                {/* 이벤트가 없을 경우 */}
                <div className="py-2 text-gray-600">
                  <p>다가오는 이벤트가 없습니다.</p>
                </div>
              </div>
            )}
          </div>
        </div>
      </section>
    </main>
  );
}
