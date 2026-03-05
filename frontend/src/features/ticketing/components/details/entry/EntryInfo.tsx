'use client';

import { MarathonEvent } from '@/features/schedule/types';
import { useEffect, useState } from 'react';

export function EntryInfo({ event }: { event: MarathonEvent }) {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  const recruitmentType = () => {
    if (event.status === 'UPCOMING') return '접수 후 추첨';
    if (event.type === 'LOTTERY') return '응모 후 추첨';
    if (event.type === 'FIRST_COME') return '선착순 신청';
    return '선착순 신청';
  };

  const recruitmentStatus = () => {
    if (event.status === 'CLOSED' || event.status === 'RESULT') return '마감';
    return 'N일 N시간 남음';
  };

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <section>
      <div className="space-y-3 mb-4 text-sm sm:text-base">
        <div className="flex">
          <span className="w-28 font-semibold">장소</span>
          <span className="flex-1">
            {event.venueAddress || '서울 광화문 광장'}
          </span>
        </div>
        <div className="flex">
          <span className="w-28 font-semibold">개최일</span>
          <span className="flex-1">
            {event.eventAt
              ? `${new Date(event.eventAt).toLocaleDateString('ko-KR')}`
              : '2024.04.20'}
          </span>
        </div>
        <div className="flex">
          <span className="w-28 font-semibold">참가비</span>
          <span className="flex-1 font-bold">50,000원</span>
        </div>

        <div className="flex">
          <span className="w-28 font-semibold">배송정보</span>
          <div className="flex-1">
            <p>
              본 상품은 일괄배송 상품으로 2026년 4월 1일부터 순차 배송됩니다.
            </p>
            <p>3만 원 이상 구매 시 무료배송</p>
          </div>
        </div>
        <div className="flex">
          <span className="w-28 font-semibold">모집 기간</span>
          <span className="flex-1">
            {event.appStartAt
              ? `${new Date(event.appStartAt).toLocaleDateString('ko-KR')} ~ ${new Date(event.appEndAt).toLocaleDateString('ko-KR')}`
              : '2024.03.01 - 2024.03.15'}
          </span>
        </div>
        <div className="flex">
          <span className="w-28 font-semibold">추첨 발표일</span>
          <span className="flex-1">
            {event.appStartAt
              ? `${new Date(event.resultAt).toLocaleDateString('ko-KR')}`
              : '2024.04.01'}
          </span>
        </div>
        {event.status !== 'UPCOMING' && (
          <div className="flex">
            <span className="w-28 font-semibold">예상 당첨 확률</span>
            <div>
              <p className="flex-1 font-bold text-2xl">nn.n%</p>
              <p className="flex-1 text-sm text-gray-600">
                추첨 인원 N명 / 응모자 N명
              </p>
            </div>
          </div>
        )}
        <div className="flex-1">
          <div className="flex items-center justify-center border border-gray-200 rounded-sm p-4">
            {/* 모집 현황 */}
            <div className="flex flex-col flex-1 items-center justify-center">
              <p className="text-xs text-gray-500 font-medium uppercase tracking-wider">
                모집 현황
              </p>
              <p className="text-lg font-bold">{recruitmentStatus()}</p>
            </div>

            {/* 중앙 구분선 */}
            <div className="w-px h-8 bg-gray-200" />

            {/* 모집 방식 */}
            <div className="flex flex-col flex-1 items-center justify-center">
              <p className="text-xs text-gray-500 ">모집 방식</p>
              <p className="text-lg font-bold">{recruitmentType()}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
