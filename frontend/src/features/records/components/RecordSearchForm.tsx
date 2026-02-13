import { Search } from 'lucide-react';
import { Input } from '@/components/shadcn/input';
import { Button } from '@/components/shadcn/button';

interface Props {
  value: string;
  onChange: (val: string) => void;
  onSubmit: (e: React.FormEvent) => void;
}

export function RecordSearchForm({ value, onChange, onSubmit }: Props) {
  return (
    <form
      onSubmit={onSubmit}
      className="flex gap-2 max-w-md mx-auto sm:mx-0 mb-10"
    >
      <div className="relative flex-1">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 w-4 h-4" />
        <Input
          placeholder="배번호 또는 이름 입력"
          className="pl-10 border-slate-300 focus:ring-[#001E62]"
          value={value}
          onChange={(e) => onChange(e.target.value)}
        />
      </div>
      <Button type="submit" className="bg-[#001E62] hover:bg-[#001E62]/90">
        조회하기
      </Button>
    </form>
  );
}
