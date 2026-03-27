'use client';

import { useRef } from 'react';
import { LuArrowLeft, LuArrowRight } from 'react-icons/lu';
import { Button } from '@/components/ui/button';

export default function Platform() {
  const scrollRef = useRef<HTMLDivElement>(null);

  const platformContents = [
    {
      id: 1,
      title: '마라톤',
      subtitle:
        '익숙했던 거리를 새로운 시각으로 바라볼 수 있는 지역 기반 마라톤',
      img: '/image/contents1.jpg',
    },
    {
      id: 2,
      title: '플레이 런',
      subtitle:
        '보물찾기, 경찰과 도둑처럼 테마를 더해 놀이처럼 즐기는 미니 러닝 이벤트',
      img: '/image/contents2.jpg',
    },
    {
      id: 3,
      title: '체험단',
      subtitle: 'On의 신제품을 가장 먼저 체험해볼 수 있는 소규모 러닝 이벤트',
      img: '/image/contents3.png',
    },
    {
      id: 4,
      title: '러닝 클래스',
      subtitle:
        '전문 트레이너·스포츠 인플루언서의 코칭을 받을 수 있는 그룹 클래스',
      img: '/image/contents4.png',
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

  return (
    <section className="py-20 bg-white overflow-hidden">
      <div className="max-w-[1440px] mx-auto w-full px-6">
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
  );
}
