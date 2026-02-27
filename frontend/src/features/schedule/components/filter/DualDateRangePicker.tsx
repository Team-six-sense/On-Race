'use client';

import { useState } from 'react';
import { format, isBefore, startOfDay } from 'date-fns';
import { ko } from 'date-fns/locale';

import { DateRange } from 'react-day-picker';

import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/shadcn/calendar';
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from '@/components/shadcn/popover';
import { LuCalendar, LuChevronDown } from 'react-icons/lu';

interface Props {
  onChange?: (range: { start: Date | null; end: Date | null }) => void;
  className?: string;
}

export default function IndependentDualPicker({ onChange, className }: Props) {
  const [isOpen, setIsOpen] = useState(false);

  // 초기 상태를 명확히 정의하여 타입 오류 방지
  const [date, setDate] = useState<DateRange | undefined>({
    from: undefined,
    to: undefined,
  });

  const [leftMonth, setLeftMonth] = useState<Date>(new Date());
  const [rightMonth, setRightMonth] = useState<Date>(
    new Date(new Date().setMonth(new Date().getMonth() + 1)),
  );

  const notifyChange = (newRange: DateRange) => {
    if (onChange) {
      onChange({
        start: newRange.from ?? null,
        end: newRange.to ?? null,
      });
    }
  };

  // 왼쪽 달력: 날짜 클릭 시 '시작일'로 고정
  const handleLeftSelect = (
    _range: DateRange | undefined,
    selectedDay: Date,
  ) => {
    const newRange: DateRange = {
      from: selectedDay,
      // 만약 기존 종료일이 새로운 시작일보다 빠르면 종료일 삭제
      to:
        date?.to && isBefore(date.to, startOfDay(selectedDay))
          ? undefined
          : date?.to,
    };
    setDate(newRange);
    notifyChange(newRange);
  };

  // 오른쪽 달력: 날짜 클릭 시 '종료일'로 고정
  const handleRightSelect = (
    _range: DateRange | undefined,
    selectedDay: Date,
  ) => {
    // 시작일이 없으면 선택 불가하게 하거나, 시작일을 해당 날짜로 설정
    if (!date?.from) {
      const newRange: DateRange = { from: selectedDay, to: undefined };
      setDate(newRange);
      notifyChange(newRange);
      return;
    }

    // 시작일보다 이전 날짜를 종료일로 선택하려고 하면 무시 (혹은 시작일로 변경)
    if (isBefore(startOfDay(selectedDay), startOfDay(date.from))) {
      return;
    }

    const newRange: DateRange = {
      from: date.from,
      to: selectedDay,
    };
    setDate(newRange);
    notifyChange(newRange);
  };

  const handleReset = () => {
    const cleared = { from: undefined, to: undefined };
    setDate(cleared);
    if (onChange) onChange({ start: null, end: null });
  };

  return (
    <div className={cn('grid gap-2', className)}>
      <Popover open={isOpen} onOpenChange={setIsOpen}>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            className="w-full max-w-md justify-between text-left h-10 px-3 border-gray-200 
            hover:bg-white active:scale-100 active:bg-white"
          >
            <div
              className={cn(
                'flex items-center justify-center',
                date?.from ? 'text-black' : 'text-gray-400',
              )}
            >
              <LuCalendar className="mr-2" />
              <span>
                {date?.from
                  ? date.to
                    ? `${format(date.from, 'yyyy.MM.dd')} ~ ${format(date.to, 'yyyy.MM.dd')}`
                    : `${format(date.from, 'yyyy.MM.dd')} — 종료일 선택`
                  : '전체'}
              </span>
            </div>
            <LuChevronDown className="h-4 w-4 text-gray-400" />
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-6" align="start">
          <div className="flex flex-col md:flex-row gap-8">
            {/* 왼쪽 달력 */}
            <div className="space-y-2">
              <p className="text-xs font-semibold text-gray-500 px-3">시작일</p>
              <Calendar
                mode="range" // range 모드를 유지해야 CSS(강조색)가 정상 작동함
                month={leftMonth}
                onMonthChange={setLeftMonth}
                selected={date}
                onSelect={handleLeftSelect} // 커스텀 핸들러
                locale={ko}
                numberOfMonths={1}
                className="rounded-md border-none"
              />
            </div>

            {/* 오른쪽 달력 */}
            <div className="space-y-2 border-l pt-4 md:pt-0 md:pl-8">
              <p className="text-xs font-semibold text-gray-500 px-3">종료일</p>
              <Calendar
                mode="range"
                month={rightMonth}
                onMonthChange={setRightMonth}
                selected={date}
                onSelect={handleRightSelect} // 커스텀 핸들러
                locale={ko}
                numberOfMonths={1}
                className="rounded-md border-none"
                // 시작일 이전 날짜는 비활성화하여 사용자 실수 방지
                disabled={(day) =>
                  date?.from ? isBefore(day, startOfDay(date.from)) : false
                }
              />
            </div>
          </div>

          <div className="pt-4 flex items-center justify-between border-t mt-4">
            <Button
              variant="ghost"
              size="fit"
              rounded="full"
              onClick={handleReset}
            >
              초기화
            </Button>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="fit"
                rounded="full"
                onClick={() => setIsOpen(false)}
              >
                취소
              </Button>
              <Button
                variant="primary1"
                size="fit"
                rounded="full"
                onClick={() => setIsOpen(false)}
              >
                확인
              </Button>
            </div>
          </div>
        </PopoverContent>
      </Popover>
    </div>
  );
}
