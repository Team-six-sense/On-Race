'use client';

import { MarathonEvent } from '@/features/schedule/types';
import { useEffect, useState } from 'react';
import { MdImage } from 'react-icons/md';

export function EventThumbnail({ event }: { event: MarathonEvent }) {
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
    <div className="grid grid-cols-1 gap-10">
      <div className="relative w-full  overflow-hidden bg-gray-200">
        {event.thumbnailImg ? (
          <img
            src={event.thumbnailImg}
            alt={event.title}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            <MdImage size={100} />
          </div>
        )}
      </div>
    </div>
  );
}
