'use client';

import { useEffect, useState } from 'react';

export function ShippingDetails() {
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
      <h2 className="text-xl font-bold text-black mb-6">배송 정보</h2>
      <div className="grid grid-cols-2 md:grid-cols-4 border-t border-l border-gray-300 text-sm md:text-base">
        {/* --- 행 1 (2컬럼) --- */}
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          배송 대상
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 font-medium">
          -
        </div>
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          배송 방법
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
          -
        </div>

        {/* --- 행 2 (2컬럼) --- */}
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          배송 일정
        </div>
        <div className="px-4 py-4 border-b border-r border-gray-200">-</div>
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          배송비
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
          -
        </div>

        {/* --- 행 3 (2컬럼) --- */}
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          배송 가능 지역
        </div>
        <div className="px-4 py-4 border-b border-r border-gray-200">-</div>
        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200">
          배송지 변경 가능 기간
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
          -
        </div>

        <div className="bg-gray-100 px-4 py-4 font-medium text-gray-600 border-b border-r border-gray-200 col-span-1">
          미배송 시 보상기준
        </div>
        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 col-span-1 md:col-span-3">
          -
        </div>
      </div>
    </section>
  );
}
