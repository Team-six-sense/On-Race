import { useState } from 'react';
import { RunnerRecord } from '@/features/records/types/records';
import { recordService } from '@/features/records/services';

export function useRecordSearch() {
  const [search, setSearch] = useState('');
  const [record, setRecord] = useState<RunnerRecord | null>(null);
  const [isSearched, setIsSearched] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!search.trim()) return;

    const result = await recordService.getRecordBySearch(search);
    setRecord(result.data);
    setIsSearched(true);
  };

  return { search, setSearch, record, isSearched, handleSearch };
}
