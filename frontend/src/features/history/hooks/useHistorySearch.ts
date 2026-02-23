import { useState } from 'react';
import { historyService } from '@/features/history/services';
import { RunnerRecord, Application } from '@/features/history/types/history';

export function useHistorySearch() {
  const [search, setSearch] = useState('');
  const [showResult, setShowResult] = useState(false);
  const [data, setData] = useState<{
    record: RunnerRecord | null;
    applications: Application[];
  }>({
    record: null,
    applications: [],
  });

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!search.trim()) return;

    const [recordRes, appRes] = await Promise.all([
      historyService.getRecord(search),
      historyService.getApplication(search),
    ]);

    const result = {
      record: recordRes.data,
      applications: appRes.data,
    };
    setData(result);
    setShowResult(true);
  };

  return { search, setSearch, showResult, data, handleSearch };
}
