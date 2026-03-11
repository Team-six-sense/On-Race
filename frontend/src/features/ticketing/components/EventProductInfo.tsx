'use client';

import { useEffect, useState } from 'react';

import {
  EventCourse,
  EventDetails,
  EventBaseInfo,
  EventParticipationInfo,
  EventNotice,
} from './details/product';
import { Button } from '@/components/ui/button';
import { LuChevronDown, LuChevronUp } from 'react-icons/lu';

export function EventProductInfo() {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <div className="relative">
      <div
        className={`relative overflow-hidden transition-[max-height] duration-500 ease-in-out ${
          isExpanded ? 'max-h-[10000px]' : 'max-h-[800px]'
        }`}
      >
        <div className="space-y-4">
          {/* 코스 정보 섹션 */}
          <EventCourse />

          {/* 구분선 */}
          <div className="my-3 h-[1px] bg-gray-300"></div>

          {/* 상세 설명 섹션 */}
          <EventDetails />

          {/* 구분선 */}
          <div className="my-3 h-[1px] bg-gray-300"></div>

          {/* 공지사항 섹션 */}
          <EventNotice />

          {/* 구분선 */}
          <div className="my-3 h-[1px] bg-gray-300"></div>

          {/* 기본정보 섹션 */}
          <EventBaseInfo />

          {/* 구분선 */}
          <div className="my-3 h-[1px] bg-gray-300"></div>

          {/* 참가/구성 정보 섹션 */}
          <EventParticipationInfo />
        </div>

        {!isExpanded && (
          <div className="absolute bottom-0 left-0 w-full h-32 bg-gradient-to-t from-white via-white/80 to-transparent z-10" />
        )}
      </div>

      {/* 더보기/접기 버튼 */}
      <div className="relative z-20 mt-4 flex justify-center">
        <Button
          variant="outline"
          rounded="sm"
          className="border-gray-300 text-gray-500"
          onClick={() => setIsExpanded(!isExpanded)}
        >
          {isExpanded ? (
            <>
              상품설명 접기
              <span className="ml-1">
                <LuChevronUp />
              </span>
            </>
          ) : (
            <>
              상품설명 더보기
              <span className="ml-1">
                <LuChevronDown />
              </span>
            </>
          )}
        </Button>
      </div>
    </div>
  );
}
