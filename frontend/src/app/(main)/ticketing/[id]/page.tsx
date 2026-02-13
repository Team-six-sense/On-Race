'use client';

import { scheduleService } from '@/features/schedule/services';
import { useParams, useRouter } from 'next/navigation';
import { MarathonEvent } from '@/features/schedule/types';
import { useEffect, useState } from 'react';
import { LuArrowLeft } from 'react-icons/lu';
import { Badge } from '@/components/shadcn/badge';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

export default function MarathonDetailPage() {
  const params = useParams();
  const router = useRouter();

  const [events, setEvents] = useState<MarathonEvent[]>([]);

  // 상태 관리: 코스 및 페이스 선택
  const [selectedCourse, setSelectedCourse] = useState('');
  const [selectedPace, setSelectedPace] = useState('');

  // 데이터 로드
  useEffect(() => {
    const fetchData = async () => {
      try {
        const result = await scheduleService.getSchedule();
        setEvents(result.data);
      } catch (error) {
        console.error('데이터 로드 실패:', error);
      }
    };

    fetchData();
  }, []);
  // 데이터 찾기
  const event = events.find((item) => item.id === Number(params.id));

  if (!event)
    return <div className="p-10 text-center">대회를 찾을 수 없습니다.</div>;

  const options = [
    { value: 'all', label: '전체' },
    { value: 'banana', label: 'Banana' },
    { value: 'orange', label: 'Orange' },
  ];

  const handleApply = () => {
    // if (!selectedCourse) return alert('코스를 선택해주세요.');
    // if (!selectedPace) return alert('목표 페이스를 선택해주세요.');

    router.push(`/ticketing/${params.id}/apply`); // 응모 완료 후 목록으로
  };

  return (
    <div className="max-w-6xl mx-auto px-4 pt-8 pb-0">
      {/* 1. 상단 네비게이션: 목록으로 가기 */}
      <div className="flex justify-self-start">
        <Button variant="text" onClick={() => router.back()}>
          <LuArrowLeft size={20} />
          목록으로
        </Button>
      </div>

      <div className="flex flex-col lg:flex-row gap-8">
        {/* 2. 좌측: 대회 상세 정보 영역 */}
        <div className="flex-1">
          <div className="p-4 border-3 border-gray-300">
            <div className="flex gap-2 pt-0 pb-3">
              <Badge
                variant="outline"
                className={cn('rounded-sm text-black border-gray-400 bg-white')}
              >
                {event.status}
              </Badge>
              <Badge
                variant="outline"
                className={cn('rounded-sm text-black border-gray-400 bg-white')}
              >
                마라톤
              </Badge>
            </div>
            <div className="relative w-full h-[400px] rounded-2xl overflow-hidden bg-gray-50 mb-8 border-2 rounded-none">
              {event.thumbnail ? (
                <img
                  src={event.thumbnail}
                  alt={event.title}
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-400 ">
                  이벤트 썸네일 이미지
                </div>
              )}
            </div>
            <h1 className="text-3xl font-bold mb-6">{event.title}</h1>
            <Button variant="outline" size="fit" rounded="sm">
              URL 공유
            </Button>
          </div>

          {/* 구분선 */}
          <div className="py-2"></div>

          <div className="p-2 border-3 border-gray-300">
            <h3 className="text-xl font-bold text-black mb-4">상세 정보</h3>

            <p className="font-bold">대회 일시</p>
            <div className="p-2 my-2 border-2 bg-gray-50">{event.date}</div>

            <p className="font-bold">접수 기간</p>
            <div className="p-2 my-2 border-2 bg-gray-50">{event.date}</div>

            <p className="font-bold">대회 장소</p>
            <div className="p-2 my-2 border-2 bg-gray-50">{event.location}</div>

            <p className="font-bold">대회 종목</p>
            <div className="p-2 my-2 border-2 bg-gray-50">{event.courses}</div>

            <p className="font-bold">참가비 안내</p>
            <div className="p-2 my-2 border-2 bg-gray-50">
              <ul className="list-disc pl-5 ">
                <li>조기 접수 할인 이벤트 진행 중</li>
                <li>단체 할인(10명 이상) 가능</li>
                <li>대학생 할인 30% 적용 가능</li>
              </ul>
            </div>

            <p className="font-bold">코스 안내</p>
            <div className="p-2 my-2 relative w-full h-[100px] rounded-2xl overflow-hidden bg-gray-100 border-2 rounded-none">
              {event.thumbnail ? (
                <img
                  src={event.thumbnail}
                  alt={event.title}
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-400 ">
                  [코스 맵 이미지 영역]
                </div>
              )}
            </div>

            <p className="font-bold">시상 및 기념품</p>
            <div className="p-2 my-2 border-2 bg-gray-50">
              <ul className="list-disc pl-5 ">
                <li>완주 메달 전원 제공</li>
                <li>기능성 티셔츠 제공</li>
                <li>각 부문 1~3위 트로피 및 상금</li>
              </ul>
            </div>

            <p className="font-bold">참가 및 유의사항</p>
            <div className="p-2 my-2 border-2 bg-gray-50">
              <ul className="list-disc pl-5 ">
                <li>만 19세 이상 참가 가능</li>
                <li>건강 상태 확인서 제출 필수</li>
                <li>기상 악화 시 일정 변경 가능</li>
                <li>환불 규정은 공지사항 참조</li>
              </ul>
            </div>

            <p className="font-bold">공지사항</p>
            <div className="p-2 my-2 border-2 bg-gray-50">
              [공지사항 목록 영역]
            </div>
          </div>
        </div>

        {/* 3. 우측: 참여 정보 카드 (Sidebar) */}
        <aside className="w-full lg:w-[380px]">
          <div className="sticky top-8 bg-white border-3 border-gray-400 rounded-none p-6">
            <h2 className="text-xl font-bold flex items-center gap-2">
              참여 정보
            </h2>

            <div className="my-4 h-[1px] bg-gray-300"></div>

            <div className="p-2 grid grid-row gap-2">
              <div className="space-y-2">
                <label className="text-sm font-bold">코스 선택</label>
                <Select
                  value={selectedCourse}
                  onValueChange={setSelectedCourse}
                >
                  <SelectTrigger variant="default">
                    <SelectValue placeholder="코스을 선택하세요" />
                  </SelectTrigger>
                  <SelectContent>
                    {options.map((opt) => (
                      <SelectItem key={opt.value} value={opt.value}>
                        {opt.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <label className="text-sm font-bold">페이스 선택</label>
                <Select value={selectedPace} onValueChange={setSelectedPace}>
                  <SelectTrigger variant="default">
                    <SelectValue placeholder="페이스를 선택하세요" />
                  </SelectTrigger>
                  <SelectContent>
                    {options.map((opt) => (
                      <SelectItem key={opt.value} value={opt.value}>
                        {opt.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="p-2 my-2 border-2 bg-gray-50">
              <p className="text-gray-800">현재 경쟁률</p>
              <p className="text-black">84% (4,200 / 5,000명)</p>
              <p className="text-gray-500">* 분 단위 자동 갱신</p>
            </div>

            {/* 구분선 */}
            <div className="my-3 h-[1px] bg-gray-300"></div>

            {/* 금액 및 응모 버튼 */}
            <div className="">
              <div className="flex justify-between items-center mb-6">
                <span className="text-gray-500">참가비</span>
                <span className="text-xl ">50,000원</span>
              </div>

              <Button variant="primary1" rounded="none" onClick={handleApply}>
                신청하기
              </Button>

              <p className="text-center text-xs text-gray-400 mt-4">
                * 로그인이 필요합니다.
              </p>
            </div>
          </div>
        </aside>
      </div>
    </div>
  );
}
