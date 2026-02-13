import { Search } from 'lucide-react';
import { Input } from '@/components/shadcn/input';
import { Button } from '@/components/shadcn/button';
import { cn } from '@/lib/utils';
import { NoticeCategory } from '@/features/notice/types/notice';

interface Props {
  activeTab: NoticeCategory;
  onTabChange: (tab: NoticeCategory) => void;
  search: string;
  onSearchChange: (val: string) => void;
}

const CATEGORIES: NoticeCategory[] = ['전체', '공지', '이벤트', '결과', '안내'];

export function NoticeFilters({
  activeTab,
  onTabChange,
  search,
  onSearchChange,
}: Props) {
  return (
    <div className="flex flex-col md:flex-row justify-between items-center gap-6 mb-8">
      <div className="flex gap-2 overflow-x-auto w-full md:w-auto pb-2 md:pb-0">
        {CATEGORIES.map((cat) => (
          <Button
            key={cat}
            variant={activeTab === cat ? 'default' : 'outline'}
            onClick={() => onTabChange(cat)}
            className={cn(
              'rounded-full px-6 font-bold transition-all',
              activeTab === cat
                ? 'bg-[#001E62] text-white'
                : 'bg-white text-slate-500 border-slate-200 hover:text-[#001E62]',
            )}
          >
            {cat}
          </Button>
        ))}
      </div>
      <div className="relative w-full md:w-80 group">
        <Search
          className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-[#001E62]"
          size={18}
        />
        <Input
          placeholder="공지사항 제목 검색"
          className="pl-12 h-12 bg-white rounded-xl"
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
        />
      </div>
    </div>
  );
}
