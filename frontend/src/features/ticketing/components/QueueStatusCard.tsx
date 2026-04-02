import { QueueProgressBar } from './QueueProgressBar';
import { Button } from '@/components/ui/button';
import { IoMdTime } from 'react-icons/io';
import LocalLottie from './LocalLottie';

export const QueueStatusCard = ({
  status,
  progress,
  onCancel,
}: {
  status: any;
  progress: number;
  onCancel: () => void;
}) => {
  if (!status) return null;

  const isPassed = status.position <= 0;

  return (
    <div className="w-full max-w-xl p-8 bg-white ">
      <div className="text-center mb-8">
        <div className="max-w-lg overflow-hidden flex justify-center items-center mx-auto">
          <LocalLottie />
        </div>
      </div>

      <div className="space-y-4">
        <div className="flex flex-col item-center justify-center text-center">
          <div>
            <p className="text-lg font-bold mb-2">나의 대기 순서.</p>
            <p className="text-3xl font-bold text-black mb-2">
              {isPassed ? '0' : status.position.toLocaleString()}번째
            </p>
          </div>
        </div>

        {/* 0.1초마다 업데이트되는 프로그래스 바 */}
        <QueueProgressBar progress={progress} />

        <div className="flex items-center text-base text-font-medium mb-2">
          <IoMdTime className="mr-2" />
          <span className="mr-3">예상 대기 시간</span>
          <span className="font-bold text-black">
            {isPassed ? '00:00' : status.expectedWaitTime}
          </span>
        </div>

        <div className="text-center">
          <p className="text-sm text-gray-500 tracking-widest">
            잠시만 기다려 주시면 선택하신 이벤트의 결제 페이지로 연결됩니다.
          </p>
          <p className="text-sm text-gray-500 tracking-widest">
            창을 닫거나 재접속하시면 대기순서가 초기화되어 대기시간이 늘어날 수
            있습니다.
          </p>
          <Button
            variant="link"
            size="fit"
            rounded="sm"
            className="text-font-medium"
            onClick={onCancel}
          >
            대기 취소
          </Button>
        </div>
      </div>
    </div>
  );
};
