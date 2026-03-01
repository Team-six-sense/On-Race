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
import {
  EventConfirmModal,
  OptionConfirmModal,
  UserConfirmModal,
} from '@/features/ticketing/components';

export default function MarathonDetailPage() {
  const params = useParams();
  const router = useRouter();

  const [events, setEvents] = useState<MarathonEvent[]>([]);

  const [template, setTemplate] = useState(0);

  // 상태 관리: 코스 및 페이스 선택
  const [selectedCourse, setSelectedCourse] = useState('');
  const [selectedPace, setSelectedPace] = useState('');
  const [activeTab, setActiveTab] = useState('product');
  const [isUserModalOpen, setIsUserModalOpen] = useState(false);
  const [isEventModalOpen, setIsEventModalOpen] = useState(false);
  const [isOptionModalOpen, setIsOptionModalOpen] = useState(false);

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

  const courses = [
    { value: '5km', label: '5km' },
    { value: '10km', label: '10km' },
    { value: 'Half', label: 'Half' },
    { value: 'Full', label: 'Full' },
  ];
  const paces = [
    { label: 'Sub-3 (2:59:59)', value: '179' },
    { label: '3시간 30분', value: '210' },
    { label: 'Sub-4 (3:59:59)', value: '239' },
    { label: '4시간 30분', value: '270' },
    { label: '5시간 완주', value: '300' },
  ];

  const handleApply = () => {
    // if (!selectedCourse) return alert('코스를 선택해주세요.');
    // if (!selectedPace) return alert('목표 페이스를 선택해주세요.');

    router.push(`/ticketing/${params.id}/apply`); // 응모 완료 후 목록으로
  };

  // 공통 탭 버튼 스타일 (활성화 여부에 따라 변경)
  const getTabClass = (tabName: string) => {
    const isActive = activeTab === tabName;
    return `
      w-32 py-3 text-center text-sm  transition-all duration-200 relative
      ${isActive ? 'font-bold' : 'font-medium'}
    `;
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
            <div className="w-full max-w-4xl mx-auto p-2">
              {/* 탭 메뉴: 왼쪽 정렬 및 탭 간 간격 설정 */}
              <div className="flex border-b border-gray-200 mb-6">
                <button
                  onClick={() => setActiveTab('product')}
                  className={getTabClass('product')}
                >
                  상품정보
                  {/* 활성화 시 나타나는 언더바 */}
                  {activeTab === 'product' && (
                    <div className="absolute bottom-0 left-0 w-full h-0.5 bg-black transition-all" />
                  )}
                </button>
                <button
                  onClick={() => setActiveTab('sales')}
                  className={getTabClass('sales')}
                >
                  판매정보
                  {/* 활성화 시 나타나는 언더바 */}
                  {activeTab === 'sales' && (
                    <div className="absolute bottom-0 left-0 w-full h-0.5 bg-black transition-all" />
                  )}
                </button>
              </div>

              {/* 메인 컨텐츠 영역 */}
              <div className="p-2">
                {/* 1. 상품정보 탭 */}
                {activeTab === 'product' && (
                  <div className="space-y-4">
                    {/* 1. 기본정보 섹션 */}
                    <section>
                      <div className="flex items-center mb-4">
                        <h2 className="text-xl font-bold text-gray-800">
                          기본정보
                        </h2>
                      </div>

                      <div className="grid grid-cols-2 md:grid-cols-4 border-1 border-gray-300 border-b border-gray-200 text-sm md:text-base">
                        {/* 행 1-1 */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-gray-200">
                          이벤트명
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-gray-200 md:border-r border-gray-200 font-medium">
                          서울 마라톤 대회 2026
                        </div>
                        {/* 행 1-2 */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-gray-200">
                          이벤트장소
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-gray-200">
                          서울 올림픽공원
                        </div>

                        {/* 행 2-1 */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 md:border-b-0 border-b border-gray-200">
                          접수기간
                        </div>
                        <div className="px-4 py-4 md:border-r md:border-b-0 border-b border-gray-200">
                          2026.01.26 ~ 2026.01.26
                        </div>
                        {/* 행 2-2 */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600">
                          개최날짜
                        </div>
                        <div className="px-4 py-4 text-gray-900">
                          2026.04.26
                        </div>
                      </div>
                    </section>

                    {/* 2. 참가/구성 정보 섹션 */}
                    <section>
                      <h2 className="text-xl font-bold mb-6 flex items-center">
                        참가/구성 정보
                      </h2>
                      <div className="border-1 border-gray-300">
                        <table className="w-full text-sm md:text-base border-collapse">
                          <tbody>
                            <tr className="border-b border-gray-300">
                              <th className="w-1/4 py-4 px-4 bg-gray-50 text-left font-semibold text-gray-600">
                                참가비 안내
                              </th>
                              <td className="py-4 px-4">
                                <ul className="list-disc list-inside space-y-1">
                                  <p>풀코스 (42.195KM): 50,000원</p>
                                  <p>하프 (21.1KM): 40,000원</p>
                                  <p>10KM: 30,000원</p>
                                </ul>
                              </td>
                            </tr>
                            <tr className="border-b border-gray-300">
                              <th className="py-4 px-4 bg-gray-50 text-left font-semibold text-gray-600">
                                패키지 정보
                              </th>
                              <td className="py-4 px-4 leading-relaxed">
                                <ul className="list-disc list-inside space-y-1">
                                  <p>
                                    기본 패키지(상제정보 → 상세이미지): +0원
                                  </p>
                                  <p>
                                    패키지1(상제정보 → 상세이미지): +40,000원
                                  </p>
                                  <p>
                                    패키지2(상제정보 → 상세이미지): +30,000원
                                  </p>
                                </ul>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </section>

                    <section>
                      <div>
                        <h2 className="text-xl font-bold mb-6 flex items-center">
                          공지사항
                        </h2>
                        <div className="w-full p-2 overflow-hidden bg-gray-100 border-2 rounded">
                          <p>안전 수칙</p>
                          <p>건강 책임 고지</p>
                          <p>사고 발생 시 책임 범위</p>
                          <p>보험 적용 여부</p>
                          <p>건강 상태 자가 판단 책임</p>
                          <p>안전 중심 행사 안내</p>
                        </div>
                      </div>
                    </section>

                    <section>
                      <div>
                        <h2 className="text-xl font-bold mb-6 flex items-center">
                          코스 안내
                        </h2>
                        <div className="relative w-full h-[200px] overflow-hidden bg-gray-100 border-2 rounded">
                          {event.thumbnail ? (
                            <img
                              src={event.thumbnail}
                              alt="코스 지도"
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center text-gray-400 text-sm">
                              [코스 맵 이미지 영역]
                            </div>
                          )}
                        </div>
                      </div>
                    </section>
                    <section>
                      <div>
                        <h2 className="text-xl font-bold mb-6 flex items-center">
                          상세이미지
                        </h2>
                        <div className="relative w-full h-[200px] overflow-hidden bg-gray-100 border-2 rounded">
                          {event.thumbnail ? (
                            <img
                              src={event.thumbnail}
                              alt="코스 지도"
                              className="w-full h-full object-cover"
                            />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center text-gray-400 text-sm">
                              [상품 상세 이미지 영역]
                            </div>
                          )}
                        </div>
                      </div>
                    </section>
                  </div>
                )}

                {/* 2. 판매정보 탭 */}
                {activeTab === 'sales' && (
                  <div className="space-y-6">
                    <h2 className="text-xl font-bold text-black mb-6">
                      판매 및 배송 정보
                    </h2>

                    <section>
                      <div className="border-1 border-gray-300">
                        <table className="w-full text-sm md:text-base border-collapse">
                          <tbody>
                            <tr className="border-b border-gray-300">
                              <th className="w-1/4 py-4 px-4 bg-gray-50 text-left font-semibold text-gray-600">
                                판매 방식
                              </th>
                              <td className="py-4 px-4">선착</td>
                            </tr>
                            <tr className="border-gray-300">
                              <th className="py-4 px-4 bg-gray-50 text-left font-semibold text-gray-600">
                                참가 정원
                              </th>

                              <td className="py-4 px-4">
                                <ul className="list-disc list-inside space-y-1">
                                  <p>풀코스 (42.195KM): 1,000원</p>
                                  <p>하프 (21.1KM): 1,000원</p>
                                  <p>10KM: 1,000원</p>
                                </ul>
                              </td>
                            </tr>
                          </tbody>
                        </table>
                      </div>
                    </section>

                    {/* 구분선 */}
                    <div className="my-3 h-[1px] bg-gray-300"></div>

                    <h2 className="text-xl font-bold text-black mb-6">
                      취소 및 환불 정책
                    </h2>
                    <section>
                      <div className="grid grid-cols-2 md:grid-cols-4 border-t border-l border-gray-300 text-sm md:text-base">
                        {/* --- 행 1 (2컬럼) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          환불 가능 기간
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 font-medium">
                          -
                        </div>
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          환불 불가 시점
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
                          -
                        </div>

                        {/* --- 행 2 (2컬럼) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          취소 수수료 기준
                        </div>
                        <div className="px-4 py-4 border-b border-r border-gray-200">
                          -
                        </div>
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          양도 가능여부
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
                          -
                        </div>

                        {/* --- 행 3 (2컬럼) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          통신판매업 신고 번호
                        </div>
                        <div className="px-4 py-4 border-b border-r border-gray-200">
                          -
                        </div>
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          통신판매자 중개사 여부
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
                          -
                        </div>

                        {/* --- 행 4 (1x2로 표시: 제목 1칸, 내용 3칸 차지) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200 col-span-1">
                          환불 취소 정책
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 col-span-1 md:col-span-3">
                          -
                        </div>

                        {/* --- 행 5 (1x2) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200 col-span-1">
                          우천/천재지변 시 환불정책
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 col-span-1 md:col-span-3">
                          -
                        </div>
                      </div>
                    </section>

                    {/* 구분선 */}
                    <div className="my-3 h-[1px] bg-gray-300"></div>

                    <h2 className="text-xl font-bold text-black mb-6">
                      배송 정보
                    </h2>
                    <section>
                      <div className="grid grid-cols-2 md:grid-cols-4 border-t border-l border-gray-300 text-sm md:text-base">
                        {/* --- 행 1 (2컬럼) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          배송 대상
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 font-medium">
                          -
                        </div>
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          배송 방법
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
                          -
                        </div>

                        {/* --- 행 2 (2컬럼) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          배송 일정
                        </div>
                        <div className="px-4 py-4 border-b border-r border-gray-200">
                          -
                        </div>
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          배송비
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
                          -
                        </div>

                        {/* --- 행 3 (2컬럼) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          배송 가능 지역
                        </div>
                        <div className="px-4 py-4 border-b border-r border-gray-200">
                          -
                        </div>
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          배송지 변경 가능 기간
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
                          -
                        </div>

                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200 col-span-1">
                          미배송 시 보상기준
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 col-span-1 md:col-span-3">
                          -
                        </div>
                      </div>
                    </section>

                    {/* 구분선 */}
                    <div className="my-3 h-[1px] bg-gray-300"></div>

                    <h2 className="text-xl font-bold text-black mb-6">
                      판매자 정보
                    </h2>
                    <section>
                      <div className="grid grid-cols-2 md:grid-cols-4 border-t border-l border-gray-300 text-sm md:text-base">
                        {/* --- 행 1 (2컬럼) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          판매자 상호
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200 font-medium">
                          -
                        </div>
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          상업자 등록번호
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
                          -
                        </div>

                        {/* --- 행 2 (2컬럼) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          통신판매업 신고 번호
                        </div>
                        <div className="px-4 py-4 border-b border-r border-gray-200">
                          -
                        </div>
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          통신판매자 중개자 여부
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
                          -
                        </div>

                        {/* --- 행 3 (2컬럼) --- */}
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          고객센터
                        </div>
                        <div className="px-4 py-4 border-b border-r border-gray-200">
                          -
                        </div>
                        <div className="bg-gray-100 px-4 py-4 font-semibold text-gray-600 border-b border-r border-gray-200">
                          주소
                        </div>
                        <div className="px-4 py-4 text-gray-900 border-b border-r border-gray-200">
                          -
                        </div>
                      </div>
                    </section>
                  </div>
                )}
              </div>
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
                    {courses.map((opt) => (
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
                    {paces.map((opt) => (
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

              <Button
                variant="primary1"
                rounded="none"
                onClick={() => setIsUserModalOpen(true)}
              >
                신청하기
              </Button>

              <p className="text-center text-xs text-gray-400 mt-4">
                * 로그인이 필요합니다.
              </p>
            </div>
          </div>
        </aside>
        <div>
          <UserConfirmModal
            isOpen={isUserModalOpen}
            onClose={() => setIsUserModalOpen(false)}
            onConfirm={() => {
              setIsUserModalOpen(false);
              setIsEventModalOpen(true);
            }}
            data={userData}
            template={template}
          />
          <EventConfirmModal
            isOpen={isEventModalOpen}
            onClose={() => setIsEventModalOpen(false)}
            onConfirm={() => {
              setIsEventModalOpen(false);
              setIsOptionModalOpen(true);
            }}
            data={eventData}
            template={template}
          />
          <OptionConfirmModal
            isOpen={isOptionModalOpen}
            onClose={() => setIsOptionModalOpen(false)}
            onConfirm={() => {
              setIsOptionModalOpen(false);
              handleApply();
            }}
            data={optionData}
            template={template}
          />
        </div>
      </div>
    </div>
  );
}
