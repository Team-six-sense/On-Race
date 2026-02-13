'use client';

import { useEffect, useState } from 'react';
import { Bell } from 'lucide-react';
import { Button } from '@/components/shadcn/button';
import { NoticeTable, NoticeFilters } from '@/features/notice/components';
import { useNoticeFilter } from '@/features/notice/hooks';
import { Notice } from '@/features/notice/types';
import { noticeService } from '@/features/notice/services';

export default function NoticePage() {
  const [initialData, setInitialData] = useState<Notice[]>([]);

  // 데이터 로드
  useEffect(() => {
    const fetchData = async () => {
      try {
        const result = await noticeService.getNotices();
        setInitialData(result.data);
      } catch (error) {
        console.error('데이터 로드 실패:', error);
      }
    };

    fetchData();
  }, []);

  const { activeTab, setActiveTab, search, setSearch, filteredNotices } =
    useNoticeFilter(initialData);

  return (
    <div className="min-h-screen bg-primary1">
      {/* 1. Page Header */}
      {/* <header className="bg-[#001E62] text-white py-8 px-4 shadow-lg">
        <div className="max-w-5xl mx-auto">
          <h1 className="text-3xl font-bold mb-2">RUNNER NEWS</h1>
          <p className="opacity-80">Notice & Updates</p>
        </div>
      </header> */}

      <main className="max-w-[1100px] mx-auto px-6 pt-6 pb-20">
        {/* 2. Filters */}
        <NoticeFilters
          activeTab={activeTab}
          onTabChange={setActiveTab}
          search={search}
          onSearchChange={setSearch}
        />

        {/* 3. Table */}
        <NoticeTable notices={filteredNotices} />

        {/* 4. Pagination (Logic 생략, UI만 유지) */}
        {/* <NoticePagination /> */}

        {/* 5. Bottom Banner */}
        <section className="mt-20 p-8 rounded-2xl bg-slate-900 text-white flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="flex items-center gap-4">
            <div className="p-3 bg-white/10 rounded-xl">
              <Bell className="text-orange-400" />
            </div>
            <div>
              <h3 className="text-lg font-bold">궁금한 점이 있으신가요?</h3>
              <p className="text-white/60 text-sm">
                자주 묻는 질문(FAQ) 또는 고객센터 1:1 문의를 이용해 주세요.
              </p>
            </div>
          </div>
          <Button className="bg-white text-slate-900 hover:bg-white/90 font-bold px-8 h-12 rounded-xl">
            고객센터 바로가기
          </Button>
        </section>
      </main>
    </div>
  );
}
