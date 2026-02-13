'use client';

import { Trophy, ClipboardList, Search as SearchIcon } from 'lucide-react';
import {
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/components/shadcn/tabs';

import {
  HistorySearch,
  RecordTab,
  ApplicationTab,
} from '@/features/history/components';
import { useHistorySearch } from '@/features/history/hooks';

export default function MarathonPortalPage() {
  const { search, setSearch, showResult, data, handleSearch } =
    useHistorySearch();

  return (
    <div className="min-h-screen bg-primary">
      {/* <header className="bg-[#001E62] text-white py-10 px-4 shadow-lg">
        <div className="max-w-5xl mx-auto">
          <h1 className="text-3xl font-bold mb-2 tracking-tight">
            RUNNER PORTAL
          </h1>
          <p className="opacity-80">
            마라톤 기록 조회 및 대회 신청 현황을 관리하세요.
          </p>
        </div>
      </header> */}

      <main className="max-w-5xl mx-auto px-4 py-8">
        <HistorySearch
          value={search}
          onChange={setSearch}
          onSubmit={handleSearch}
        />

        {showResult && data.record ? (
          <Tabs
            defaultValue="records"
            className="space-y-6 animate-in fade-in duration-500"
          >
            <TabsList className="grid w-full max-w-[400px] grid-cols-2 bg-slate-200">
              <TabsTrigger
                value="records"
                className="data-[state=active]:bg-[#001E62] data-[state=active]:text-white"
              >
                <Trophy className="w-4 h-4 mr-2" /> 기록 조회
              </TabsTrigger>
              <TabsTrigger
                value="applications"
                className="data-[state=active]:bg-[#001E62] data-[state=active]:text-white"
              >
                <ClipboardList className="w-4 h-4 mr-2" /> 신청 내역
              </TabsTrigger>
            </TabsList>

            <TabsContent value="records">
              <RecordTab record={data.record} />
            </TabsContent>

            <TabsContent value="applications">
              <ApplicationTab applications={data.applications} />
            </TabsContent>
          </Tabs>
        ) : (
          <div className="text-center py-20 border-2 border-dashed border-slate-200 rounded-xl bg-white">
            <SearchIcon className="w-12 h-12 text-slate-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-slate-900">
              조회 정보가 없습니다
            </h3>
            <p className="text-slate-500">
              본인의 이름과 배번호를 통해 통합 조회를 시작하세요.
            </p>
          </div>
        )}
      </main>
    </div>
  );
}
