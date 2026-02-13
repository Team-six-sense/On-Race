import { Trophy, Timer, MapPin } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/shadcn/card';
import { RunnerRecord } from '@/features/records/types';

export function RecordSummary({ record }: { record: RunnerRecord }) {
  const fullCourseCount = record.history.filter(
    (h) => h.course === 'Full',
  ).length;

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
      <SummaryCard
        title="개인 최고 기록 (Full)"
        value={record.bestRecord}
        sub="JTBC 서울 마라톤 (2023)"
        Icon={Trophy}
      />
      <SummaryCard
        title="평균 페이스"
        value={record.averagePace}
        sub="전체 대회 평균 기준"
        Icon={Timer}
      />
      <SummaryCard
        title="총 완주 횟수"
        value={`${record.totalRaces}회`}
        sub={`풀코스 ${fullCourseCount}회 포함`}
        Icon={MapPin}
      />
    </div>
  );
}

function SummaryCard({ title, value, sub, Icon }: any) {
  return (
    <Card className="border-t-4 border-t-[#001E62]">
      <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
        <CardTitle className="text-sm font-medium text-slate-500">
          {title}
        </CardTitle>
        <Icon className="w-4 h-4 text-[#001E62]" />
      </CardHeader>
      <CardContent>
        <div className="text-2xl font-bold text-[#001E62]">{value}</div>
        <p className="text-xs text-slate-400 mt-1">{sub}</p>
      </CardContent>
    </Card>
  );
}
