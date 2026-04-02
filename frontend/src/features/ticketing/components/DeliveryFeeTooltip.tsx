import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '@/components/shadcn/tooltip';
import { LuCircleAlert } from 'react-icons/lu';

export function DeliveryFeeTooltip() {
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
            기본 배송비 3,000원이며, 총 상품 금액이 50,000원 이상인 경우 무료
            배송됩니다.
          </p>

          <p>
            <span className="px-1">•</span>
            제주 및 도서산간 지역은 추가 배송비가 발생할 수 있습니다.
          </p>
          <p>
            <span className="px-1">•</span>
            (제주: 3,000원 / 도서산간: 5,000원 추가) 배송비 정책
          </p>
        </TooltipContent>
      </Tooltip>
    </TooltipProvider>
  );
}
