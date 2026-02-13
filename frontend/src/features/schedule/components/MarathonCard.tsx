import { Card } from '@/components/shadcn/card';
import { Badge } from '@/components/shadcn/badge';
import { cn } from '@/lib/utils';
import { MarathonEvent, EventStatus } from '@/features/schedule/types/schedule';
import { LuArrowRight } from 'react-icons/lu';
import Link from 'next/link';

interface Props {
  event: MarathonEvent;
}

export function MarathonCard({ event }: Props) {
  return (
    <Card className="p-4 group overflow-hidden border-2 border-gray-400 rounded-none bg-primary flex flex-col sm:flex-row h-full sm:h-44">
      {/* 1. 썸네일 섹션 (고정 너비) */}
      <div className="flex shrink-0 w-full sm:w-48 h-40 sm:h-full overflow-hidden bg-gray-100 border-1 border-gray-400">
        {event.thumbnail ? (
          <img
            src={event.thumbnail}
            alt={event.title}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            썸네일
          </div>
        )}
      </div>

      {/* 2. 정보 섹션 (flex-1을 추가하여 남은 공간을 다 차지하게 함) */}
      <div className="py-4 px-6 flex-1 flex flex-col justify-between min-w-0">
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <h3 className="text-xl font-bold text-black truncate">
              {event.title}
            </h3>
          </div>

          <Badge
            variant="outline"
            className={cn('rounded-sm text-black border-gray-400 bg-white')}
          >
            {event.status}
          </Badge>

          <div className="py-1 flex flex-wrap items-center gap-x-3 gap-y-1">
            <div className="flex gap-1.5 items-center">
              <span className="text-xs font-bold text-gray-400">지역:</span>
              <span className="text-xs font-bold text-black">
                {event.location}
              </span>
            </div>
            <div className="flex gap-1.5 items-center">
              <span className="text-xs font-bold text-gray-400">코스:</span>
              <div className="flex gap-1">
                {event.courses.map((course) => (
                  <span
                    key={course}
                    className="text-xs font-bold text-black uppercase"
                  >
                    {course}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-gray-400">이벤트 날짜:</span>
          <span className="text-xs font-bold text-black">{event.date}</span>
        </div>
      </div>

      {/* 3. 우측 화살표 섹션 (정렬 유지) */}
      <Link
        href={`/ticketing/${event.id}`}
        className="hidden sm:flex w-14 shrink-0 items-center justify-center border-gray-400 group-hover:bg-gray-100 transition-colors cursor-pointer"
      >
        <LuArrowRight
          size={20}
          className="text-gray-400 group-hover:text-black group-hover:translate-x-1 transition-all"
        />
      </Link>
    </Card>
  );
}
