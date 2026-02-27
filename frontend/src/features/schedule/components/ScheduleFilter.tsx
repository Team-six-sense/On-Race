import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import DistanceSlider from './filter/DistanceSlider';
import { useState } from 'react';
import DualDateRangePicker from './filter/DualDateRangePicker';
import { IoLocationOutline } from 'react-icons/io5';

export function ScheduleFilter() {
  const [range, setRange] = useState({ min: 0, max: 99 });
  const [dateRange, setDateRange] = useState<{
    start: Date | null;
    end: Date | null;
  }>({
    start: null,
    end: null,
  });

  const options = [
    { value: 'all', label: '전체' },
    { value: 'location1', label: '서울' },
    { value: 'location2', label: '경기' },
    { value: 'location3', label: '강원' },
    { value: 'location4', label: '충청' },
    { value: 'location5', label: '전라' },
    { value: 'location6', label: '경상' },
  ];

  return (
    <div className="mb-6 ">
      <div className="p-2 grid grid-cols-3 gap-4">
        <div className="space-y-2">
          <label className="text-sm font-bold">거리</label>
          <DistanceSlider
            min={0}
            max={100}
            step={5}
            onChange={(val: any) => setRange(val)}
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-bold">날짜</label>
          <DualDateRangePicker onChange={(val) => setDateRange(val)} />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-bold">지역</label>

          <Select>
            <SelectTrigger variant="default" className="justify-between">
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
      </div>
      <div className="flex justify-end items-center gap-2 p-2 w-full">
        <Button variant="outline" rounded="full" size="fit">
          초기화
        </Button>
        <Button variant="primary1" rounded="full" size="fit">
          검색하기
        </Button>
      </div>
    </div>
  );
}
