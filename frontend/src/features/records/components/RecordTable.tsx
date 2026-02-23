import { Calendar } from 'lucide-react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/shadcn/table';
import { Badge } from '@/components/shadcn/badge';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/shadcn/card';
import { RunnerRecord } from '@/features/records/types';

export function RecordTable({ record }: { record: RunnerRecord }) {
  return (
    <Card>
      <CardHeader>
        <div className="flex justify-between items-center">
          <div>
            <CardTitle className="text-[#001E62]">대회 기록 상세</CardTitle>
            <CardDescription>
              {record.name} 러너님의 공식 기록입니다.
            </CardDescription>
          </div>
          <Badge variant="outline" className="text-[#001E62] border-[#001E62]">
            BIB: {record.bib}
          </Badge>
        </div>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow className="bg-slate-50">
              <TableHead>
                <Calendar className="w-3 h-3 inline mr-1" />
                날짜
              </TableHead>
              <TableHead>대회명</TableHead>
              <TableHead>코스</TableHead>
              <TableHead className="text-right">기록</TableHead>
              <TableHead className="text-right">페이스</TableHead>
              <TableHead className="text-center">결과</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {record.history.map((item) => (
              <TableRow key={item.id} className="hover:bg-slate-50/50">
                <TableCell className="font-medium">{item.date}</TableCell>
                <TableCell className="font-semibold">{item.event}</TableCell>
                <TableCell>
                  <Badge
                    variant="secondary"
                    className={
                      item.course === 'Full'
                        ? 'bg-orange-100 text-orange-700'
                        : 'bg-blue-100 text-blue-700'
                    }
                  >
                    {item.course}
                  </Badge>
                </TableCell>
                <TableCell className="text-right font-mono font-bold text-[#001E62]">
                  {item.time}
                </TableCell>
                <TableCell className="text-right text-slate-500">
                  {item.pace}
                </TableCell>
                <TableCell className="text-center">
                  <span className="text-xs px-2 py-1 rounded-full bg-green-100 text-green-700 font-medium">
                    {item.status}
                  </span>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}
