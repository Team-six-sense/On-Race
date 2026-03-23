'use client';

import { useEffect, useState } from 'react';
import { getStatusLabel } from '@/types/constants';
import { Event } from '@/features/event/types';
import { Button } from '@/components/ui/button';
import { LuChevronLeft, LuShare } from 'react-icons/lu';
import {
  EntryInfo,
  EntryOptions,
  EntryNotice,
  EntryParticipationInfo,
} from './details/entry';

export function EventEntryInfo({
  event,
  setIsUserModalOpen,
}: {
  event: Event;
  setIsUserModalOpen: React.Dispatch<React.SetStateAction<boolean>>;
}) {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);
  const [actionCard, setActionCard] = useState<Boolean>(false);
  const [resultCard, setResultCard] = useState<Boolean>(false);

  // 상태 관리: 코스 및 페이스 선택
  const [selectedCourse, setSelectedCourse] = useState('');
  const [selectedPace, setSelectedPace] = useState('');

  const isClosed = event.status === 'END';
  const isEntry = event.appType === 'LOTTERY' && isClosed;

  const getHeaderText = () => {
    if (event.status === 'READY') return '빠른 신청 준비하기';
    if (event.appType === 'LOTTERY') return '응모하기';
    if (event.appType === 'FIRST_COME') return '신청하기';
    return '신청하기';
  };

  const getButtonText = () => {
    if (event.status === 'READY') return '저장하기';
    return '다음 단계로';
  };

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  const handleAction = () => {
    if (!selectedCourse) return alert('코스를 선택해주세요.');
    if (!selectedPace) return alert('목표 페이스를 선택해주세요.');

    if (event.status === 'READY') {
      alert('사전 정보가 저장되었습니다.');
    } else {
      setIsUserModalOpen(true);
    }
  };

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <div className="flex flex-col">
      <div className="flex gap-2">
        <div className="text-sm font-semibold bg-gray-100 text-gray-600 px-2 py-0.5 rounded-sm">
          {getStatusLabel(event.status)}
        </div>
      </div>
      <div className="flex flex-row items-center justify-between mb-8">
        <h1 className="text-3xl font-bold ">{event.title}</h1>
        <Button variant="ghost" size="icon">
          <LuShare />
        </Button>
      </div>

      {/* 정보 리스트 */}
      <EntryInfo event={event} />

      {/* 선택 옵션 및 버튼 */}
      <div className="mt-auto space-y-3">
        {actionCard && (
          <div className="p-4 space-y-4 border border-gray-200 rounded-sm">
            <div className="flex flex-row items-center">
              <Button
                variant="ghost"
                size="fit"
                onClick={() => setActionCard(false)}
              >
                <LuChevronLeft size={20} />
              </Button>
              <h2 className="text-lg font-bold text-gray-900">
                {getHeaderText()}
              </h2>
            </div>

            <EntryOptions
              selectedCourse={selectedCourse}
              setSelectedCourse={setSelectedCourse}
              selectedPace={selectedPace}
              setSelectedPace={setSelectedPace}
            />

            {event.status !== 'READY' && (
              <div className="flex">
                <span className="w-28 text-sm font-semibold text-gray-700">
                  예상 경쟁률
                </span>
                <div>
                  <p className="flex-1 font-bold text-2xl">nn.n%</p>
                  <p className="flex-1 text-sm text-gray-600">
                    추첨 인원 N명 / 응모자 N명
                  </p>
                </div>
              </div>
            )}

            {/* 참가 정보 */}
            <EntryParticipationInfo />

            {/* 안내사항 */}
            <EntryNotice />

            <Button
              variant="primary1"
              rounded="full"
              onClick={() => handleAction()}
            >
              {getButtonText()}
            </Button>
          </div>
        )}

        {resultCard && (
          <div className="space-y-2">
            <div className="p-4 space-y-4 border border-gray-200 rounded-sm">
              <div className="flex flex-row items-center">
                <Button
                  variant="ghost"
                  size="fit"
                  onClick={() => setResultCard(false)}
                >
                  <LuChevronLeft size={20} />
                </Button>
                <h2 className="text-lg font-bold text-gray-900">결과 보기</h2>
              </div>

              <section>
                <div className="flex flex-col items-center justify-center py-6 space-y-2">
                  <p className="font-bold">당첨을 축하합니다 🎉</p>
                  <p className="text-xl font-bold text-red-600">
                    2026.04.01 (일) 까지
                  </p>
                  <p>결제를 완료해주세요</p>
                </div>
              </section>

              <section>
                <div className="flex justify-between items-center">
                  <label className="text-sm font-semibold text-gray-700">
                    예상 결제 금액
                  </label>
                </div>
                <div className="rounded-xl p-4 space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">10km, 5'30"/km</span>
                    <span className="text-gray-900 font-medium">50,000원</span>
                  </div>
                </div>
              </section>

              <section>
                <div className="flex justify-between items-center">
                  <label className="text-sm font-semibold text-gray-700">
                    추가 가능한 옵션
                  </label>
                </div>
                <div className="rounded-xl p-4 space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">키링</span>
                    <span className="text-gray-900 font-medium">+7,900원</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">키링 + 텀블러</span>
                    <span className="text-gray-900 font-medium">+14,000원</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">
                      키링 + 텀블러 + 후드집업
                    </span>
                    <span className="text-gray-900 font-medium">+32,000원</span>
                  </div>
                </div>
              </section>

              <EntryNotice />

              <Button
                variant="primary1"
                rounded="full"
                onClick={() => handleAction()}
              >
                결제하기
              </Button>
            </div>

            {/* 응모 실패 */}
            <div className="p-4 space-y-4 border border-gray-200 rounded-sm">
              <div className="flex flex-row items-center">
                <Button
                  variant="ghost"
                  size="fit"
                  onClick={() => setResultCard(false)}
                >
                  <LuChevronLeft size={20} />
                </Button>
                <h2 className="text-lg font-bold text-gray-900">결과 보기</h2>
              </div>

              <section>
                <div className="flex flex-col items-center justify-center py-6 space-y-2">
                  <p className="font-bold">당첨되지 않았습니다 😢</p>
                  <p className="text-xl font-bold">
                    아쉽지만 다음 기회에 또 만나요!
                  </p>
                  <Button variant="primary1" rounded="sm" size="fit">
                    다른 이벤트 보러가기
                  </Button>
                </div>
              </section>

              <EntryNotice />
            </div>

            <div className="p-4 space-y-4 border border-gray-200 rounded-sm">
              <div className="flex flex-row items-center">
                <Button
                  variant="ghost"
                  size="fit"
                  onClick={() => setResultCard(false)}
                >
                  <LuChevronLeft size={20} />
                </Button>
                <h2 className="text-lg font-bold text-gray-900">결과 보기</h2>
              </div>

              <section>
                <div className="flex flex-col items-center justify-center py-6 space-y-2">
                  <p className="text-xl text-gray-500 font-bold">
                    응모한 내역이 없습니다.
                  </p>
                  <p className="text-gray-400">
                    마이페이지에서 내가 응모한 이벤트를 확인해보세요.
                  </p>
                  <Button variant="primary1" rounded="sm" size="fit">
                    마이페이지 가기
                  </Button>
                </div>
              </section>

              <EntryNotice />
            </div>

            <div className="p-4 space-y-4 border border-gray-200 rounded-sm">
              <div className="flex flex-row items-center">
                <Button
                  variant="ghost"
                  size="fit"
                  onClick={() => setResultCard(false)}
                >
                  <LuChevronLeft size={20} />
                </Button>
                <h2 className="text-lg font-bold text-gray-900">결과 보기</h2>
              </div>

              <section>
                <div className="flex flex-col items-center justify-center py-6 space-y-2">
                  <p className="text-xl text-gray-500 font-bold">
                    당첨이 취소되었습니다.
                  </p>
                  <p className="text-gray-400">
                    기한 내 결제하지 않아 당첨이 취소되었습니다
                  </p>
                </div>
              </section>

              <EntryNotice />
            </div>
          </div>
        )}

        {!actionCard && !isEntry && (
          <div>
            <Button
              disabled={isClosed}
              variant="primary1"
              rounded="full"
              onClick={() => setActionCard((prev) => !prev)}
            >
              {getHeaderText()}
            </Button>
          </div>
        )}

        {!resultCard && isEntry && (
          <div>
            <Button
              variant="primary1"
              rounded="full"
              onClick={() => setResultCard((prev) => !prev)}
            >
              결과보기
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}
