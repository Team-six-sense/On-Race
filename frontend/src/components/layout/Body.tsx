'use client';

import { useEffect, useState } from 'react';
import { Button } from '../ui/button';
import { MarathonEvent } from '@/features/schedule/types';
import { scheduleService } from '@/features/schedule/services';
import { useRouter } from 'next/navigation';

export default function Body() {
  const [events, setEvents] = useState<MarathonEvent[]>([]);
  const router = useRouter();

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

  return (
    <main className="py-4 flex flex-col ">
      {/* --- 1. 브랜드 소개 (Hero Section) --- */}
      <section className="relative h-[600px] flex items-center justify-center bg-primary">
        <div className="relative z-10 text-center text-black px-4">
          <span className="px-4 py-1 mb-4 bg-gray-200 text-black rounded-full text-sm font-bold ">
            브랜드 소개
          </span>
          <h2 className="text-5xl font-black pt-4 mb-6">
            달리는 즐거움, 함께 나누다
          </h2>
          <p className="text-lg mb-8 max-w-2xl mx-auto">
            온러닝과 함께하는 특별한 러닝 경험.
            <br className="hidden md:block" />
            전국 각지에서 펼쳐지는 다양한 러닝 이벤트에 참여하세요.
          </p>
          <div className="w-full h-[250px] rounded-2xl overflow-hidden bg-gray-200 mb-8 border-2 rounded-none">
            <div className="w-full h-full flex items-center justify-center text-gray-400 ">
              브랜드 이미지 영역
            </div>
          </div>
        </div>
      </section>

      {/* --- 2. 플랫폼 소개 (Platform Features) --- */}
      <section className="py-20 max-w-7xl mx-auto px-4 bg-secondary">
        <div className="text-center mb-16">
          <span className="px-4 py-1 mb-4 bg-gray-200 text-black rounded-full text-sm font-bold ">
            플랫폼 소개
          </span>
          <h2 className="text-3xl font-black pt-4 mb-6">
            러닝 이벤트를 더 쉽게
          </h2>
          <p className="text-lg mb-8 max-w-2xl mx-auto">
            온레이스 플랫폼에서는 이런 것들을 할 수 있어요
          </p>
        </div>
        <div className="grid md:grid-cols-3 gap-8">
          {[
            {
              icon: null,
              title: '간편한 티켓 예매',
              desc: '원하는 러닝 이벤트를 찾고 몇 번의 클릭으로 간편하게 티켓을 구매할 수 있습니다.',
            },
            {
              icon: null,
              title: '다양한 이벤트',
              desc: '전국 각지에서 열리는 다채로운 러닝 이벤트를 한눈에 확인하세요',
            },
            {
              icon: null,
              title: '커뮤니티',
              desc: '러너들과 함께 소통하며 러닝의 즐거움을 나눠보세요',
            },
          ].map((feature, idx) => (
            <div
              key={idx}
              className="bg-white p-8 rounded-2xl shadow-sm border border-gray-200"
            >
              <div className="mb-4 bg-gray-200 w-12 h-12 flex items-center justify-center rounded-lg">
                {feature.icon}
              </div>
              <h4 className="text-xl font-bold mb-2">{feature.title}</h4>
              <p className="text-slate-600 leading-relaxed">{feature.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* --- 3. 이벤트 목록 (Event List) --- */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex flex-col md:flex-row md:items-end justify-between mb-10 gap-6">
            <div>
              <span className="px-4 py-1 mb-4 bg-gray-200 text-black rounded-full text-sm font-bold ">
                이벤트 목록
              </span>
              <h2 className="text-3xl font-bold pt-4 text-slate-900">
                다가오는 러닝 이벤트
              </h2>
            </div>

            <div className="flex p-1 rounded-lg">
              <Button
                variant="outline"
                size="sm"
                onClick={() => router.push('/schedule')}
              >
                전체보기
              </Button>
            </div>
          </div>

          {/* Event Cards Grid */}
          <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
            {events.map((event) => (
              <div
                key={event.id}
                className="group cursor-pointer bg-white rounded-2xl overflow-hidden border border-gray-300 transition-all duration-300"
              >
                {/* 이미지 영역: 높이를 고정하고 group-hover 시 이미지가 미세하게 커지는 효과 */}
                <div className="relative aspect-[16/10] overflow-hidden bg-gray-200">
                  <div className="w-full h-full flex items-center justify-center text-gray-400 font-medium">
                    {/* 실제 이미지가 들어갈 자리 */}
                    <span className="text-sm">이벤트 썸네일 이미지</span>
                  </div>
                </div>

                {/* 콘텐츠 영역 */}
                <div className="p-5">
                  <div className="flex items-center text-sm text-slate-500 gap-2">
                    <span className="px-4 py-1 mb-4 bg-gray-200 text-black rounded-sm text-sm font-bold ">
                      {event.location}
                    </span>
                    <span className="px-4 py-1 mb-4 bg-gray-200 text-black rounded-sm text-sm font-bold ">
                      10K
                    </span>
                  </div>
                  <h5 className="text-lg font-bold text-black ">
                    {event.title}
                  </h5>

                  <div className="mt-3 space-y-1.5">
                    <div className="flex items-center text-sm text-gray-500">
                      {event.date}
                    </div>
                  </div>

                  <div className="mt-3 space-y-1.5">
                    <div className="flex items-center text-xl text-black">
                      25,000원
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>
    </main>
  );
}
