'use client';

import { useEffect, useState } from 'react';
import { LuDroplet, LuFlag, LuMountain, LuRoute } from 'react-icons/lu';
import { MdAccessTime } from 'react-icons/md';

export function EventCourse() {
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
      {/* <div>
        <h2 className="text-xl font-bold mb-2 flex items-center">코스 안내</h2>
        <div className="relative w-full overflow-hidden bg-gray-100 border-2 rounded">
          <img
            src="/image/course.png"
            alt="코스 안내"
            className="w-full h-auto block" // h-[300px] 제거, h-auto 추가
          />
        </div>
      </div> */}
      <div>
        <h2 className="text-xl font-bold mb-4 flex items-center">코스 안내</h2>

        {/* 컨테이너: 모바일은 세로(flex-col), 데스크탑은 가로(md:flex-row) */}
        <div className="flex flex-col md:flex-row gap-4">
          {/* 좌측: 이미지 영역 (기존 높이 300px 유지) */}
          <div className="w-full md:w-3/5 h-[300px] overflow-hidden">
            <img
              src="/image/course.png"
              alt="코스 안내"
              className="w-full h-full object-fill"
            />
          </div>

          {/* 우측: 2x2 카드 그리드 영역 */}
          <div className="w-full md:w-2/5 grid grid-cols-2 grid-rows-[auto_auto_1fr] gap-2 h-[300px]">
            {/* 카드 1 */}
            <div className="bg-gray-800 p-3 flex flex-col rounded-sm">
              <span className="text-lime-600 text-sm inline-flex items-center gap-1">
                <LuFlag />총 거리
              </span>
              <span className="text-white">5km</span>
            </div>

            {/* 카드 2 */}
            <div className="bg-gray-800 p-3 flex flex-col rounded-sm">
              <span className="text-lime-600 text-sm inline-flex items-center gap-1">
                <MdAccessTime />
                제한 시간
              </span>
              <span className="text-white">1시간 30분</span>
            </div>

            {/* 카드 3 */}
            <div className="bg-gray-800 p-3 flex flex-col rounded-sm">
              <span className="text-lime-600 text-sm inline-flex items-center gap-1">
                <LuDroplet />
                급수처
              </span>
              <span className="text-white">총 3곳</span>
            </div>

            {/* 카드 4 */}
            <div className="bg-gray-800 p-3 flex flex-col rounded-sm">
              <span className="text-lime-600 text-sm inline-flex items-center gap-1">
                <LuMountain />
                고도 변화
              </span>
              <span className="text-white">±1,500m</span>
            </div>

            {/* 카드 5 */}
            <div className="bg-gray-800 p-3 flex flex-col rounded-sm col-span-2 overflow-y-auto">
              <span className="text-lime-600 text-sm inline-flex items-center gap-1">
                <LuRoute />
                상세 코스
              </span>
              <span className="text-white text-sm leading-relaxed">
                지역명 → 지역명 → 지역명(급수처) → 지역명 → 지역명 →
                지역명(급수처) → 지역명 → 지역명 → 지역명(급수처) → 지역명
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
