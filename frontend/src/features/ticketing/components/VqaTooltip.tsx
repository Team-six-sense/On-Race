import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/shadcn/tooltip';
import { LuCircleAlert } from 'react-icons/lu';

export function VqaTooltip() {
  return (
    <TooltipProvider>
      <Tooltip>
        <TooltipTrigger asChild>
          <LuCircleAlert className="h-4 w-4 text-gray-400" />
        </TooltipTrigger>

        <TooltipContent
          side="right"
          sideOffset={10}
          className="p-2 text-sm font-medium bg-cta-secondary text-font-medium"
        >
          <p>
            <span className="px-1">•</span>
            매크로 및 비정상적인 신청을 방지하기 위해 간단한 인증 문제가
            출제됩니다.
          </p>

          <p>
            <span className="px-1">•</span>각 문제는 1분 이내에 풀어야 합니다.
          </p>
          <p>
            <span className="px-1">•</span>
            3문제를 모두 실패할 경우 이벤트 신청이 취소되며, 다시 신청해야
            합니다.
          </p>
          <p>
            <span className="px-1">•</span>
            인증 진행 중 뒤로가기를 누르거나 페이지를 이탈하면 신청이
            취소됩니다.
          </p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}
