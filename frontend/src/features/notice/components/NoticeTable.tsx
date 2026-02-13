import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/shadcn/table';
import { Badge } from '@/components/shadcn/badge';
import { cn } from '@/lib/utils';
import { Notice } from '@/features/notice/types/notice';
import { isNewNotice } from '@/features/notice/utils/date';

export function NoticeTable({ notices }: { notices: Notice[] }) {
  return (
    <div className="bg-white rounded-2xl shadow-sm overflow-hidden border border-slate-100">
      <Table>
        <TableHeader className="bg-slate-50">
          <TableRow>
            <TableHead className="w-[80px] text-center">번호</TableHead>
            <TableHead className="w-[100px]">분류</TableHead>
            <TableHead>제목</TableHead>
            <TableHead className="w-[120px] text-center">등록일</TableHead>
            <TableHead className="w-[100px] text-center">조회수</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {notices.length > 0 ? (
            notices.map((notice) => (
              <TableRow
                key={notice.id}
                className={cn(
                  notice.isPinned ? 'bg-slate-50/80' : 'hover:bg-slate-50/50',
                )}
              >
                <TableCell className="text-center text-slate-400">
                  {notice.id}
                </TableCell>
                <TableCell>
                  <Badge
                    className={cn(
                      'font-bold',
                      notice.category === '공지'
                        ? 'bg-[#001E62]'
                        : notice.category === '이벤트'
                          ? 'bg-orange-500'
                          : 'bg-slate-400',
                    )}
                  >
                    {notice.category}
                  </Badge>
                </TableCell>
                <TableCell className="font-bold py-5">
                  <div className="flex items-center gap-2">
                    {notice.isPinned && (
                      <span className="text-[#001E62] text-xs font-black">
                        [중요]
                      </span>
                    )}
                    {notice.title}
                    {isNewNotice(notice.date) && (
                      <span className="w-1.5 h-1.5 bg-red-500 rounded-full animate-pulse" />
                    )}
                  </div>
                </TableCell>
                <TableCell className="text-center text-slate-400 text-sm">
                  {notice.date}
                </TableCell>
                <TableCell className="text-center text-slate-400 text-sm">
                  {notice.views.toLocaleString()}
                </TableCell>
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell
                colSpan={5}
                className="h-40 text-center text-slate-400"
              >
                검색 결과가 없습니다.
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  );
}
