'use client';

import { Search } from 'lucide-react';
import {
  RecordSearchForm,
  RecordSummary,
  RecordTable,
} from '@/features/records/components';
import { useRecordSearch } from '@/features/records/hooks';

export default function MarathonRecordPage() {
  const { search, setSearch, record, isSearched, handleSearch } =
    useRecordSearch();

  return (
    <div className="min-h-screen bg-primary1">
      {/* <header className="bg-[#001E62] text-white py-8 px-4 shadow-lg">
        <div className="max-w-5xl mx-auto">
          <h1 className="text-3xl font-bold mb-2">RUNNER RECORDS</h1>
          <p className="opacity-80">
            당신의 열정과 노력이 담긴 기록을 확인하세요.
          </p>
        </div>
      </header> */}

      <main className="max-w-5xl mx-auto px-4 py-8">
        <RecordSearchForm
          value={search}
          onChange={setSearch}
          onSubmit={handleSearch}
        />

        {isSearched && record ? (
          <div className="animate-in fade-in duration-500">
            <RecordSummary record={record} />
            <RecordTable record={record} />
          </div>
        ) : (
          <div className="text-center py-20 border-2 border-dashed border-slate-200 rounded-xl bg-white">
            <Search className="w-12 h-12 text-slate-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-slate-900">
              기록을 조회해 보세요
            </h3>
            <p className="text-slate-500">
              배번호 또는 이름을 입력하여 지금까지의 여정을 확인하세요.
            </p>
          </div>
        )}
      </main>

      <footer className="mt-auto py-8 text-center text-slate-400 text-sm">
        © 2024 Marathon Record Service. All rights reserved.
      </footer>
    </div>
  );
}
