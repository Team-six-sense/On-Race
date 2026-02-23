import { LuSquareDashed } from 'react-icons/lu';
import { QueueProgressBar } from './QueueProgressBar';
import { Button } from '@/components/ui/button';

export const QueueStatusCard = ({
  status,
  progress,
}: {
  status: any;
  progress: number;
}) => {
  if (!status) return null;

  const isPassed = status.position <= 0;

  return (
    <div className="w-full max-w-lg p-8 bg-white rounded-3xl shadow-xl border border-gray-100">
      <div className="text-center mb-8">
        <h1 className="text-2xl font-black text-gray-800 tracking-tight mb-3">
          대기열
        </h1>
        <div className="flex justify-center items-center mb-3">
          <div className="p-4 border-1 rounded-full bg-gray-200">
            <LuSquareDashed className="" />
          </div>
        </div>
        <div className="p-4 bg-gray-50 rounded-2xl border border-gray-100 text-center">
          <p className="text-gray-500 font-bold">매크로 검증(VQA) </p>
          <p className="text-gray-500 font-bold">
            [reCAPTCHA 또는 봇 검증 영역]
          </p>

          <p className="text-gray-500 font-bold">
            정상적인 사용자 확인을 위한 검증을 필요합니다.
          </p>
        </div>
      </div>

      <div className="space-y-4">
        <div className="flex flex-col item-center justify-center text-center">
          <div>
            <p className="text-xs text-gray-400 mb-2">내 대기 순서.</p>
            <p className="text-4xl font-black text-black mb-2">
              {isPassed ? '0' : status.position.toLocaleString()}
            </p>
          </div>
          <div>
            <p className="text-xs text-gray-400 font-bold uppercase tracking-wider mb-2">
              예상 대기 시간
            </p>
            <p className="text-xl font-bold text-black mb-2">
              {isPassed ? '00:00' : status.expectedWaitTime}
            </p>
          </div>
        </div>

        {/* 0.1초마다 업데이트되는 프로그래스 바 */}
        <QueueProgressBar progress={progress} />

        <div className="p-4 bg-gray-50 rounded-2xl border border-gray-100 text-center">
          <p className="text-sm text-gray-600 leading-relaxed uppercase font-bold tracking-widest">
            대기열 토큰 유효 시간: 30분
          </p>
          <p className="text-[11px] text-gray-400 leading-relaxed uppercase font-bold tracking-widest">
            * 10초마다 순번이 갱신됩니다.
          </p>
        </div>

        <div className="p-4 flex item-center justify-center">
          <Button variant="outline" size="fit" rounded="sm">
            대기 취소
          </Button>
        </div>

        <div className="p-0 flex item-center justify-center">
          <ul className="text-xs text-gray-500 space-y-2">
            <li>• 창이 을 닫으면 대기열에서 제외됩니다.</li>
            <li>• 토큰 만료 시 재진입이 필요합니다.</li>
          </ul>
        </div>
      </div>
    </div>
  );
};
