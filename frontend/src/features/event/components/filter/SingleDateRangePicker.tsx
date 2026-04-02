'use client';

import { useState } from 'react';
import { format } from 'date-fns';
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
  placeholder?: string;
}

export default function SingleDateRangePicker({
  onChange,
  className,
  placeholder = '전체',
}: Props) {
  const [isOpen, setIsOpen] = useState(false);
  const [date, setDate] = useState<DateRange | undefined>({
    from: undefined,
    to: undefined,
  });

  const handleSelect = (range: DateRange | undefined) => {
    setDate(range);
    if (onChange) {
      onChange({
        start: range?.from ?? null,
        end: range?.to ?? null,
      });
    }
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
            hover:bg-white active:scale-100 active:bg-white font-normal"
          >
            <div
              className={cn(
                'flex items-center justify-center',
                date?.from ? 'text-black' : 'text-gray-400',
              )}
            >
              <LuCalendar className="mr-2 h-4 w-4" />
              <span className="truncate">
                {date?.from
                  ? date.to
                    ? `${format(date.from, 'yyyy.MM.dd')} ~ ${format(date.to, 'yyyy.MM.dd')}`
                    : `${format(date.from, 'yyyy.MM.dd')} — 종료일 선택`
                  : placeholder}
              </span>
            </div>
            <LuChevronDown className="h-4 w-4 text-gray-400 ml-2" />
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-0" align="start">
          <div className="p-4">
            {/* numberOfMonths={2}를 주면 두 달이 동시에 보입니다. 
                 모바일 환경만 고려한다면 1로 수정하거나 반응형 처리를 할 수 있습니다. */}
            <Calendar
              initialFocus
              mode="range"
              defaultMonth={date?.from}
              selected={date}
              onSelect={handleSelect}
              numberOfMonths={1}
              locale={ko}
              className="rounded-md"
            />
          </div>

          {/* 하단 액션 버튼 영역 */}
          <div className="px-6 py-4 flex items-center justify-between border-t">
            <Button
              variant="ghost"
              size="fit"
              onClick={handleReset}
              className="text-gray-500 hover:text-black"
            >
              초기화
            </Button>
            <div className="flex gap-2">
              <Button
                variant="secondary"
                rounded="full"
                size="fit"
                onClick={() => setIsOpen(false)}
              >
                취소
              </Button>
              <Button
                variant="primary1"
                rounded="full"
                size="fit"
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
