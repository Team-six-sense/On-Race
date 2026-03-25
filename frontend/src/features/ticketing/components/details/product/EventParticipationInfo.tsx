'use client';

import { CourseDetails } from '@/features/event/types';
import { useEffect, useState } from 'react';

export function EventParticipationInfo({
  courses,
}: {
  courses: CourseDetails[];
}) {
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
      <h2 className="text-base font-bold mb-2 flex items-center">
        참가/구성 정보
      </h2>
      <div className="border-1 border-gray-300">
        <table className="w-full text-sm border-collapse">
          <tbody>
            <tr className="border-b border-gray-300">
              <th className="py-4 px-4 bg-gray-100 text-left text-gray-600 font-medium">
                참가비 안내
              </th>
              <td className="py-4 px-4 text-gray-600">
                <ul className="list-disc list-inside space-y-1">
                  {courses.map((course) => (
                    <p key={course.id} className="text-gray-600">
                      {course.name}
                      {course.distanceMeter > 10000 && (
                        <span>
                          ({(course.distanceMeter / 1000).toFixed(3)}km)
                        </span>
                      )}
                      <span className="ml-1 ">
                        {course.price.toLocaleString()}원
                      </span>
                    </p>
                  ))}
                </ul>
              </td>
            </tr>
            <tr className="border-b border-gray-300">
              <th className="py-4 px-4 bg-gray-100 text-left text-gray-600 font-medium">
                참가 인원
              </th>
              <td className="py-4 px-4 text-gray-600">
                <ul className="list-disc list-inside space-y-1">
                  {courses.map((course) => (
                    <p key={course.id} className="text-gray-500">
                      {course.name}
                      {course.distanceMeter > 10000 && (
                        <span>
                          ({(course.distanceMeter / 1000).toFixed(3)}km)
                        </span>
                      )}
                      <span className="ml-1 ">
                        {course.courseCapacity.toLocaleString()}명
                      </span>
                    </p>
                  ))}
                </ul>
              </td>
            </tr>
            <tr className="border-b border-gray-300">
              <th className="py-4 px-4 bg-gray-100 text-left text-gray-600 font-medium">
                패키지 옵션
              </th>
              <td className="py-4 px-4 text-gray-600">
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
