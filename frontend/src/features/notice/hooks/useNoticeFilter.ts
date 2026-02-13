import { useState, useMemo } from 'react';
import { Notice, NoticeCategory } from '@/features/notice/types/notice';

export function useNoticeFilter(initialData: Notice[]) {
  const [activeTab, setActiveTab] = useState<NoticeCategory>('전체');
  const [search, setSearch] = useState('');

  const filteredNotices = useMemo(() => {
    return initialData.filter((notice) => {
      const matchesTab = activeTab === '전체' || notice.category === activeTab;
      const matchesSearch = notice.title
        .toLowerCase()
        .includes(search.toLowerCase());
      return matchesTab && matchesSearch;
    });
  }, [initialData, activeTab, search]);

  return {
    activeTab,
    setActiveTab,
    search,
    setSearch,
    filteredNotices,
  };
}
