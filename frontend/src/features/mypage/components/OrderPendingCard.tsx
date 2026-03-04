'use client';

import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { MdImage } from 'react-icons/md';

interface OrderPendingCardProps {
  id: number;
  status: string; // 응모상태
  thumbnail: string; // 썸네일 이미지 URL
  price: number; // 가격
  title: string; // 제목
  course: string; // 코스
  pace: string; // 페이스
  applyDate: string; // 응모날짜
  resultDate: string; // 결과발표
  orderDate: string; // 결제 마감일
}

export function OrderPendingCard({
  id,
  status,
  price,
  title,
  course,
  pace,
  applyDate,
  resultDate,
  orderDate,
}: OrderPendingCardProps) {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);
  const router = useRouter();

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <div
      className="relative flex flex-col rounded-lg bg-white border border-gray-200 hover:shadow-md"
      onClick={() => router.push(`/ticketing/${id}/payment`)}
    >
      {/* 상단 영역: 상태 태그와 가격 */}
      <div className="flex justify-between items-center px-4 pt-4">
        <div className="text-xs font-semibold bg-green-100 text-green-600 px-2 py-1 rounded">
          결재 대기
        </div>
        {/* 가격을 우측 상단으로 이동 */}
        <span className="font-bold text-black">
          {price === 0 ? '무료' : `${price.toLocaleString()}원`}
        </span>
      </div>

      {/* 썸네일 영역: 중앙 정렬 및 여백 조정 */}
      <div className="w-full flex justify-center items-center text-gray-300 py-6">
        <MdImage size={80} />
      </div>

      {/* 컨텐츠 영역 */}
      <div className="px-4 pb-4">
        {/* 제목: 상단 여백 줄임 */}
        <h3 className="text-lg font-bold text-black truncate leading-tight mb-3">
          {title}
        </h3>

        {/* 코스 & 페이스 정보: 간격 및 폰트 크기 미세 조정 */}
        <div className="flex items-center gap-4 text-sm text-gray-700 mb-2">
          <div className="flex items-center">
            <span className="font-medium text-gray-500 mr-1">코스</span>
            <span>{course}</span>
          </div>
          <div className="flex items-center">
            <span className="font-medium text-gray-500 mr-1">페이스</span>
            <span>{pace}</span>
          </div>
        </div>

        {/* 날짜 정보 영역: 상단 구분선 효과 또는 여백 조정 */}
        <div className="flex flex-col gap-1 text-xs text-gray-500 pt-2 border-t border-gray-50">
          <div className="flex justify-between">
            <span>대기 날짜</span>
            <span className="text-gray-800">{applyDate}</span>
          </div>
          <div className="flex justify-between">
            <span>티켓 오픈 날짜</span>
            <span className="text-gray-800">{resultDate}</span>
          </div>
          <div className="flex justify-between">
            <span>결제 마감일</span>
            <span className="text-gray-800">{orderDate}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
