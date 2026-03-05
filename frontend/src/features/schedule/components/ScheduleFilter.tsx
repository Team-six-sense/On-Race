'use client';

import { useState, useEffect } from 'react'; // useEffect 추가
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import DistanceSlider from './filter/DistanceSlider';
import DualDateRangePicker from './filter/DualDateRangePicker';
import { IoLocationOutline } from 'react-icons/io5';
import { Input } from '@/components/ui/input';

export function ScheduleFilter({
  setSearchDistance,
  setSearchDate,
  setSearchLocation,
  setSearchTerm,
}: {
  setSearchDistance: React.Dispatch<
    React.SetStateAction<{ min: number; max: number }>
  >;
  setSearchDate: (range: {
    start: Date | string | null;
    end: Date | string | null;
  }) => void;
  setSearchLocation: React.Dispatch<React.SetStateAction<string>>;
  setSearchTerm: React.Dispatch<React.SetStateAction<string>>;
}) {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);

  const [keyword, setKeyword] = useState<string>('');
  const [location, setLocation] = useState<string>('all');
  const [range, setRange] = useState({ min: 0, max: 100 });
  const [dateRange, setDateRange] = useState<{
    start: Date | null;
    end: Date | null;
  }>({
    start: null,
    end: null,
  });

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  const options = [
    { value: 'all', label: '전체' },
    { value: '서울', label: '서울' },
    { value: '인천', label: '인천' },
    { value: '대전', label: '대전' },
    { value: '광주', label: '광주' },
    { value: '부산', label: '부산' },
    { value: '대구', label: '대구' },
    { value: '울산', label: '울산' },
    { value: '경기', label: '경기' },
    { value: '강원', label: '강원' },
    { value: '충북', label: '충북' },
    { value: '충남', label: '충남' },
    { value: '전북', label: '전북' },
    { value: '전남', label: '전남' },
    { value: '경북', label: '경북' },
    { value: '경남', label: '경남' },
    { value: '제주', label: '제주' },
  ];

  const applyFilter = () => {
    setSearchDistance(range);
    setSearchDate(dateRange);
    setSearchLocation(location);
    setSearchTerm(keyword);
  };
  const initFilter = () => {
    setKeyword('');
    setLocation('all');
    setRange({ min: 0, max: 100 });
    setDateRange({ start: null, end: null });
  };

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <div className="p-6 mb-6 bg-gray-50 rounded-sm">
      <div className="grid grid-cols-3 space-y-4 gap-4 mb-4">
        <div>
          <DistanceSlider
            min={0}
            max={100}
            step={1}
            onChange={(val: any) => setRange(val)}
          />
        </div>
        <div>
          <label className="text-sm font-bold">날짜</label>
          <DualDateRangePicker onChange={(val) => setDateRange(val)} />
        </div>
        <div>
          <label className="text-sm font-bold">지역</label>
          <Select value={location} onValueChange={(val) => setLocation(val)}>
            <SelectTrigger
              variant="default"
              className="justify-between bg-white"
            >
              <div className="flex items-center text-left">
                <IoLocationOutline className="mr-2" />
                <SelectValue placeholder="지역을 선택하세요" />
              </div>
            </SelectTrigger>
            <SelectContent>
              {options.map((opt) => (
                <SelectItem key={opt.value} value={opt.value}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div className="col-span-full space-y-2">
          <label className="text-sm font-bold">키워드</label>
          <Input value={keyword} onChange={(e) => setKeyword(e.target.value)} />
        </div>
      </div>
      <div className="flex justify-end items-center gap-2  w-full">
        <Button
          variant="primary2"
          rounded="full"
          size="fit"
          onClick={initFilter}
        >
          필터 초기화
        </Button>
        <Button
          variant="primary1"
          rounded="full"
          size="fit"
          onClick={applyFilter}
        >
          검색하기
        </Button>
      </div>
    </div>
  );
}
