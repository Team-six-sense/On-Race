'use client';

import { useEffect, useState } from 'react';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useEventStore } from '@/features/event/store/useEventStore';

export function EntryOptions() {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const { course, pace, setCourse, setPace } = useEventStore();
  const [mounted, setMounted] = useState(false);

  const courses = [
    { value: '5km', label: '5km' },
    { value: '10km', label: '10km' },
    { value: 'Half', label: 'Half' },
    { value: 'Full', label: 'Full' },
  ];
  const paces = [
    { label: 'Sub-3 (2:59:59)', value: '179' },
    { label: '3시간 30분', value: '210' },
    { label: 'Sub-4 (3:59:59)', value: '239' },
    { label: '4시간 30분', value: '270' },
    { label: '5시간 완주', value: '300' },
  ];
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
      {/* 코스 선택 */}
      <div className="p-2">
        <label className="text-base font-semibold">코스*</label>
        <Select value={course} onValueChange={setCourse}>
          <SelectTrigger variant="default">
            <SelectValue placeholder="코스을 선택하세요" />
          </SelectTrigger>
          <SelectContent>
            {courses.map((opt) => (
              <SelectItem key={opt.value} value={opt.label}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* 페이스 선택 */}
      <div className="p-2">
        <label className="text-base font-semibold">페이스</label>
        <Select value={pace} onValueChange={setPace}>
          <SelectTrigger variant="default">
            <SelectValue placeholder="페이스를 선택하세요" />
          </SelectTrigger>
          <SelectContent>
            {paces.map((opt) => (
              <SelectItem key={opt.value} value={opt.label}>
                {opt.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
    </section>
  );
}
