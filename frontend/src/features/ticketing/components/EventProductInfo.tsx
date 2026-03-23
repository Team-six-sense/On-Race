'use client';

import { useEffect, useState } from 'react';

import {
  EventCourse,
  EventDetailsInfo,
  EventBaseInfo,
  EventParticipationInfo,
  EventNotice,
} from './details/product';
import { Button } from '@/components/ui/button';
import { LuChevronDown, LuChevronUp } from 'react-icons/lu';
import { Event, EventDetails } from '@/features/event/types';
import { useParams } from 'next/navigation';
import { eventService } from '@/features/event/services';

export function EventProductInfo({ event }: { event: Event }) {
  const params = useParams();

  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState<boolean>(false);
  const [eventDetails, setEventDetails] = useState<EventDetails>();
  const [isExpanded, setIsExpanded] = useState<boolean>(false);

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

  return (
    <div className="relative">
      {eventDetails && (
        <div className="space-y-4">
          {/* 코스 정보 섹션 */}
          <EventCourse />

          {/* 구분선 */}
          <div className="my-3 h-[1px] bg-gray-300"></div>

          {/* 상세 설명 섹션 */}
          <div className="relative">
            <div
              className={`overflow-hidden transition-[max-height] duration-500 ease-in-out ${
                isExpanded ? 'max-h-[10000px]' : 'max-h-[1290px]'
              }`}
            >
              <EventDetailsInfo detailImgList={eventDetails.detailImg} />
            </div>

            {/* 그라데이션 및 버튼 영역 */}
            <div
              className={`${
                isExpanded
                  ? 'relative mt-4'
                  : 'absolute bottom-0 h-60 bg-gradient-to-t from-white white/50 to-transparent'
              } left-0 w-full flex items-end justify-center z-20 pb-4`}
            >
              <Button
                variant="outline"
                rounded="sm"
                className="border-gray-300 text-gray-500 bg-white"
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

          {/* 구분선 */}
          <div className="my-3 h-[1px] bg-gray-300"></div>

          {/* 공지사항 섹션 */}
          <EventNotice notice={eventDetails.notice} />

          <div className="my-3 h-[1px] bg-gray-300"></div>

          {/* 기본 정보 */}
          <EventBaseInfo event={event} />

          <div className="my-3 h-[1px] bg-gray-300"></div>

          {/* 참가/구성 정보 */}
          <EventParticipationInfo />
        </div>
      )}
    </div>
  );
}
