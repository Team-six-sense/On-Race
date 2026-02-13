import { Trophy } from 'lucide-react';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/shadcn/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/shadcn/table';
import { Badge } from '@/components/shadcn/badge';
import { RunnerRecord } from '@/features/history/types/history';

export function RecordTab({ record }: { record: RunnerRecord }) {
  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="border-t-4 border-t-[#001E62]">
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-slate-500">
              총 완주 횟수
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-[#001E62]">
              {record.history.length}회
            </div>
          </CardContent>
        </Card>
        {/* 추가 카드들... */}
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-[#001E62]">과거 대회 기록</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>날짜</TableHead>
                <TableHead>대회명</TableHead>
                <TableHead>코스</TableHead>
                <TableHead className="text-right">기록</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {record.history.map((item) => (
                <TableRow key={item.id}>
                  <TableCell>{item.date}</TableCell>
                  <TableCell className="font-semibold">{item.event}</TableCell>
                  <TableCell>
                    <Badge variant="outline">{item.course}</Badge>
                  </TableCell>
                  <TableCell className="text-right font-mono font-bold text-[#001E62]">
                    {item.time}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  );
}
