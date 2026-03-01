'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';

import {
  EventConfirmModal,
  OptionConfirmModal,
  UserConfirmModal,
} from '@/features/ticketing/components';
import { useDetailedTracker } from '@/features/ticketing/hooks/useDetailedTracker';
import { Input } from '@/components/ui/input';

export default function MarathonDetailPage() {
  const [template, setTemplate] = useState(0);
  const [isUserModalOpen, setIsUserModalOpen] = useState(false);
  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [isOptionModalOpen, setIsOptionModalOpen] = useState(false);

  const [intervalInput, setIntervalInput] = useState(100);
  const { isRecording, logs, startTracking, stopTracking } =
    useDetailedTracker();

  const handleStart = () => {
    setTemplate(Math.floor(Math.random() * 2));
    setIsUserModalOpen(true);

    startTracking(intervalInput);
  };
  const handleStop = () => {
    setIsUserModalOpen(false);
    setIsEventModalOpen(false);
    setIsOptionModalOpen(false);
    stopTracking();
  };

  const handleDownload = () => {
    // 수집 중일 때는 중지 후 다운로드하거나, 지금까지의 데이터를 다운로드
    const currentLogs = isRecording ? [...logs] : logs;

    const jsonString = JSON.stringify(currentLogs, null, 2);
    const blob = new Blob([jsonString], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `mouse_data_${new Date().getTime()}.json`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  const userData = {
    name: '홍길동',
    birthDate: '1995.01.15',
    gender: '남성',
    phone: '010-1234-5678',
    email: 'hong @example.com',
  };

  const eventData = {
    name: '한강 벚꽃 러닝 페스티벌',
    eventDate: '2026.04.12 (토) 오전 9:00',
    location: '서울 여의도 한강공원 입구',
  };

  const optionData = {
    course: '10km',
    pace: '6\'30\"\/km',
  };

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex items-center justify-bewteen">
        <div className="flex-1 text-2xl mb-4 text-bold">
          마우스 데이터 모니터링
        </div>
        <div className="flex text-2xl mb-4 text-bold gap-2">
          <Input
            value={intervalInput}
            onChange={(e) => setIntervalInput(Number(e.target.value))}
          />
          <Button
            variant="outline"
            size="fit"
            rounded="lg"
            onClick={handleStart}
          >
            수집
          </Button>
          <Button
            variant="outline"
            size="fit"
            rounded="lg"
            onClick={handleDownload}
            disabled={logs.length === 0}
          >
            JSON 다운로드
          </Button>
        </div>

        <div>
          <UserConfirmModal
            isOpen={isUserModalOpen}
            onClose={handleStop}
            onConfirm={() => {
              setIsUserModalOpen(false);
              setIsEventModalOpen(true);
            }}
            data={userData}
            template={template}
          />
          <EventConfirmModal
            isOpen={isEventModalOpen}
            onClose={handleStop}
            onConfirm={() => {
              setIsEventModalOpen(false);
              setIsOptionModalOpen(true);
            }}
            data={eventData}
            template={template}
          />
          <OptionConfirmModal
            isOpen={isOptionModalOpen}
            onClose={handleStop}
            onConfirm={handleStop}
            data={optionData}
            template={template}
          />
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* 요약 카드 */}
        <div className="lg:col-span-1 space-y-4">
          <SummaryCard title="총 데이터 포인트" value={logs.length} unit="개" />
          <SummaryCard
            title="총 클릭(Down)"
            value={logs.reduce((s, l) => s + l.downCount, 0)}
            unit="회"
            color="text-rose-600"
          />
          <SummaryCard
            title="총 이동 거리"
            value={logs.reduce((s, l) => s + l.distance, 0).toFixed(0)}
            unit="px"
            color="text-blue-600"
          />
        </div>

        {/* 상세 로그 테이블 */}
        <div className="lg:col-span-3 bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
          <div className="p-4 border-b bg-slate-50 flex justify-between items-center">
            <span className="font-bold text-slate-700">
              이벤트 타입별 상세 로그
            </span>
            <span className="text-xs text-slate-400 font-mono italic">
              Newest first
            </span>
          </div>
          <div className="overflow-y-auto max-h-[600px]">
            <table className="w-full text-left">
              <thead className="bg-slate-50 text-slate-500 text-[11px] uppercase sticky top-0 border-b">
                <tr>
                  <th className="p-4">Time</th>
                  <th className="p-4">Position(x, y)</th>
                  <th className="p-4 text-center">Move</th>
                  <th className="p-4 text-center">Down</th>
                  <th className="p-4 text-center">Up</th>
                  <th className="p-4">Distance</th>
                  <th className="p-4">Last Event</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {logs
                  .slice()
                  .reverse()
                  .slice(0, 50)
                  .map((log, i) => (
                    <tr
                      key={i}
                      className="hover:bg-indigo-50/30 transition-colors"
                    >
                      <td className="p-4 font-mono text-[11px] text-slate-400">
                        {(log.timestamp % 100000).toFixed(0)}
                      </td>
                      <td className="p-4 font-bold text-slate-700 text-sm">
                        {log.x}, {log.y}
                      </td>

                      {/* 타입별 카운팅 표시 */}
                      <td className="p-4 text-center">
                        <span
                          className={`text-xs font-bold ${log.moveCount > 0 ? 'text-blue-500' : 'text-slate-200'}`}
                        >
                          {log.moveCount}
                        </span>
                      </td>
                      <td className="p-4 text-center">
                        <span
                          className={`px-2 py-1 rounded text-xs font-bold ${log.downCount > 0 ? 'bg-rose-100 text-rose-600' : 'text-slate-200'}`}
                        >
                          {log.downCount}
                        </span>
                      </td>
                      <td className="p-4 text-center">
                        <span
                          className={`px-2 py-1 rounded text-xs font-bold ${log.upCount > 0 ? 'bg-amber-100 text-amber-600' : 'text-slate-200'}`}
                        >
                          {log.upCount}
                        </span>
                      </td>

                      <td className="p-4">
                        <div className="w-full bg-slate-100 h-1.5 rounded-full overflow-hidden">
                          <div
                            className="bg-blue-400 h-full"
                            style={{ width: `${Math.min(log.distance, 100)}%` }}
                          ></div>
                        </div>
                        <span className="text-[10px] text-slate-400">
                          {log.distance}px
                        </span>
                      </td>

                      <td className="p-4 text-xs">
                        <span
                          className={`px-2 py-0.5 rounded-full font-medium ${getEventBadgeColor(log.lastEventType)}`}
                        >
                          {log.lastEventType || 'none'}
                        </span>
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}

function SummaryCard({ title, value, unit, color = 'text-slate-800' }: any) {
  return (
    <div className="bg-white p-5 rounded-2xl shadow-sm border border-slate-200">
      <div className="text-[11px] font-bold text-slate-400 uppercase mb-1">
        {title}
      </div>
      <div className={`text-2xl font-black ${color}`}>
        {value}{' '}
        <span className="text-sm font-normal text-slate-400">{unit}</span>
      </div>
    </div>
  );
}

function getEventBadgeColor(type: string) {
  switch (type) {
    case 'pointermove':
      return 'bg-blue-50 text-blue-600';
    case 'pointerdown':
      return 'bg-rose-50 text-rose-600 font-bold underline';
    case 'pointerup':
      return 'bg-amber-50 text-amber-600 font-bold underline';
    default:
      return 'bg-slate-50 text-slate-400';
  }
}
