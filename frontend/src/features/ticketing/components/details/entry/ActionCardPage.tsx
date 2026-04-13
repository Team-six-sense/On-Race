import { Button } from '@/components/ui/button';
import { LuChevronLeft } from 'react-icons/lu';
import { EntryOptions } from './EntryOptions';
import { EntryParticipationInfo } from './EntryParticipationInfo';
import { EntryNotice } from './EntryNotice';
import { useEventStore } from '@/features/event/store/useEventStore';

export function ActionCardPage({
  actionCard,
  setActionCard,
  onStart,
  setIsUserModalOpen,
}: {
  actionCard: boolean;
  setActionCard: React.Dispatch<React.SetStateAction<boolean>>;
  onStart: () => void;
  setIsUserModalOpen: React.Dispatch<React.SetStateAction<boolean>>;
}) {
  const { event, course, pace } = useEventStore();

  const getHeaderText = () => {
    if (event?.status === 'READY') return '빠른 신청 준비하기';
    if (event?.appType === 'LOTTERY') return '응모하기';
    if (event?.appType === 'FIRST_COME') return '신청하기';
    return '신청하기';
  };

  const getButtonText = () => {
    if (event?.status === 'READY') return '저장하기';
    return '다음 단계로';
  };

  const handleAction = () => {
    if (!course) return alert('코스를 선택해주세요.');
    if (!pace) return alert('목표 페이스를 선택해주세요.');

    if (event?.status === 'READY') {
      alert('사전 정보가 저장되었습니다.');
    } else {
      onStart();
      setIsUserModalOpen(true);
    }
  };

  return (
    <div className="flex flex-col p-4 border border-gray-200 rounded-sm h-[500px]">
      {/* 상단 스크롤 영역 (flex-1로 남은 공간을 다 차지하게 함) */}
      <div className="flex-1 overflow-y-auto space-y-4 pr-1 ">
        <div className="flex flex-row items-center">
          <Button
            variant="ghost"
            size="fit"
            onClick={() => setActionCard(false)}
          >
            <LuChevronLeft size={20} />
          </Button>
          <h2 className="text-lg font-bold text-black">{getHeaderText()}</h2>
        </div>

        <EntryOptions />

        {event?.appType === 'LOTTERY' && event?.status !== 'READY' && (
          <div className="flex">
            <span className="w-28 text-base font-semibold text-black">
              예상 경쟁률
            </span>
            <div>
              <p className="flex-1 font-bold text-2xl">nn.n%</p>
              <p className="flex-1 text-sm text-gray-500">
                추첨 인원 N명 / 응모자 N명
              </p>
            </div>
          </div>
        )}

        {/* 참가 정보 */}
        <EntryParticipationInfo />

        {/* 안내사항 */}
        <EntryNotice />
      </div>

      {/* 하단 버튼 영역 */}
      <div className="px-4 pt-4 border-t">
        <Button
          className="w-full"
          variant="primary1"
          rounded="full"
          onClick={() => handleAction()}
        >
          {getButtonText()}
        </Button>
      </div>
    </div>
  );
}
