import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

export function ScheduleFilter() {
  const options = [
    { value: 'all', label: '전체' },
    { value: 'banana', label: 'Banana' },
    { value: 'orange', label: 'Orange' },
  ];
  return (
    <div className="mb-6 border-2 border-gray-400 rounded-none">
      <div className="p-2">
        <h2>필터/검색</h2>
      </div>
      <div className="p-2 grid grid-cols-3 gap-2">
        <div className="space-y-2">
          <label className="text-sm font-bold">거리 선택</label>
          <Select>
            <SelectTrigger variant="default">
              <SelectValue placeholder="거리을 선택하세요" />
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
        <div className="space-y-2">
          <label className="text-sm font-bold">날짜 선택</label>
          <Select>
            <SelectTrigger variant="default">
              <SelectValue placeholder="날짜을 선택하세요" />
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
        <Input variant="primary" placeholder="지역명 입력" label="지역 검색" />
      </div>
      <div className="flex justify-end items-center gap-2 p-2 w-full">
        <Button variant="outline" rounded="sm" size="fit">
          필터 초기화
        </Button>
        <Button variant="primary1" rounded="sm" size="fit">
          검색
        </Button>
      </div>
    </div>
  );
}
