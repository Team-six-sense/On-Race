'use client';

import { useEffect, useState } from 'react';

export function EntryParticipationInfo() {
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
      <div className="flex items-center">
        <label className="text-base font-semibold text-black">참가 정보</label>
      </div>
      <div className="p-4 space-y-2">
        <div className="flex text-base">
          <span className="w-24 text-font-medium">이름</span>
          <span className="text-gray-900 font-medium">김러닝</span>
        </div>
        <div className="flex text-base">
          <span className="w-24 text-font-medium">생년월일</span>
          <span className="text-gray-900 font-medium">1999.01.01</span>
        </div>
        <div className="flex text-base">
          <span className="w-24 text-font-medium">휴대폰번호</span>
          <span className="text-gray-900 font-medium">010-1234-5678</span>
        </div>
      </div>
    </section>
  );
}
