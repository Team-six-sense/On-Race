'use client';

import { Event } from '@/features/event/types';
import { useEffect, useState } from 'react';

export function EventParticipationInfo() {
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
      <h2 className="text-xl font-bold mb-6 flex items-center">
        참가/구성 정보
      </h2>
      <div className="border-1 border-gray-300">
        <table className="w-full text-sm border-collapse">
          <tbody>
            <tr className="border-b border-gray-300">
              <th className="py-4 px-4 bg-gray-100 text-left text-gray-600 font-medium">
                참가비 안내
              </th>
              <td className="py-4 px-4 text-gray-900">
                <ul className="list-disc list-inside space-y-1">
                  <p>풀 (42.195km) 50,000원</p>
                  <p>하프 (21.1km) 40,000원</p>
                  <p>10km 30,000원</p>
                </ul>
              </td>
            </tr>
            <tr className="border-b border-gray-300">
              <th className="py-4 px-4 bg-gray-100 text-left text-gray-600 font-medium">
                참가 인원
              </th>
              <td className="py-4 px-4 text-gray-900">
                <ul className="list-disc list-inside space-y-1">
                  <p>풀 (42.195km) 1,000명</p>
                  <p>하프 (21.1km) 1,000명</p>
                  <p>10km 1,000명</p>
                </ul>
              </td>
            </tr>
            <tr className="border-b border-gray-300">
              <th className="py-4 px-4 bg-gray-100 text-left text-gray-600 font-medium">
                패키지 옵션
              </th>
              <td className="py-4 px-4 text-gray-900">
                <ul className="list-disc list-inside space-y-1">
                  <p>옵션별 상이 / 상세페이지 참고</p>
                </ul>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  );
}
