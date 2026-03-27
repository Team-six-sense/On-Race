'use client';

import { useEffect, useState } from 'react';

export function EntryNotice() {
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
    <section className="p-4">
      <div className="flex items-center gap-2 mb-2">
        <label className="text-base font-bold">안내사항</label>
      </div>
      <ul className="space-y-1.5 text-sm text-gray-500">
        <li className="flex gap-1.5">
          <span>•</span>
          <span>
            ‘빠른 신청 준비하기’는 실제 접수 오픈 전, 참가에 필요한 정보를 미리
            저장해두는 기능입니다.
          </span>
        </li>
        <li className="flex gap-1.5">
          <span>•</span>
          <span>
            원활한 참가 신청을 위해 사전 정보를 정확하게 입력해 주시기 바랍니다.
          </span>
        </li>
        <li className="flex gap-1.5">
          <span>•</span>
          <span>
            ‘빠른 신청 준비하기’를 입력했더라도 참가 신청이 완료된 것이 아니며,
            접수 기간 내 결제 단계까지 모두 완료되어야 최종 참가가 확정됩니다.
          </span>
        </li>
      </ul>
    </section>
  );
}
