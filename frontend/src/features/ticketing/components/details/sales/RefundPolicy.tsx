'use client';

import { useEffect, useState } from 'react';

export function RefundPolicy() {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <section>
      <h2 className="text-xl font-bold text-black mb-6">취소 및 환불 정책</h2>
      <div className="grid grid-cols-2 md:grid-cols-4 border-t border-l border-gray-300 text-sm md:text-base">
        {/* --- 행 1 (2컬럼) --- */}
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          환불 가능 기간
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
          -
        </div>
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          환불 불가 시점
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
          -
        </div>

        {/* --- 행 2 (2컬럼) --- */}
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          취소 수수료 기준
        </div>
        <div className="px-4 py-4 border-b border-r border-gray-200">-</div>
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          양도 가능여부
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
          -
        </div>

        {/* --- 행 3 (1x2로 표시: 제목 1칸, 내용 3칸 차지) --- */}
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200 col-span-1">
          환불 취소 정책
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 col-span-1 md:col-span-3">
          -
        </div>

        {/* --- 행 4 (1x2) --- */}
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200 col-span-1">
          우천/천재지변 시 환불정책
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 col-span-1 md:col-span-3">
          -
        </div>
      </div>
    </section>
  );
}
