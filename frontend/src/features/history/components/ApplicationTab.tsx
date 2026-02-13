import { Calendar, CheckCircle2, Clock } from 'lucide-react';
import { Card } from '@/components/shadcn/card';
import { Badge } from '@/components/shadcn/badge';
import { Button } from '@/components/shadcn/button';
import { Progress } from '@/components/shadcn/progress';
import { Application } from '@/features/history/types/history';

export function ApplicationTab({
  applications,
}: {
  applications: Application[];
}) {
  return (
    <div className="space-y-4">
      {applications.map((app) => (
        <Card key={app.id} className="overflow-hidden">
          <div className="flex flex-col md:flex-row">
            <div className="bg-slate-50 p-6 flex flex-col justify-center items-center border-r border-slate-100 min-w-[180px]">
              <Calendar className="w-6 h-6 text-[#001E62] mb-2" />
              <span className="text-sm font-medium text-slate-500">
                대회 일시
              </span>
              <span className="text-lg font-bold text-[#001E62]">
                {app.date}
              </span>
            </div>
            <div className="p-6 flex-1">
              <div className="flex justify-between items-start mb-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <Badge
                      className={
                        app.status === '결제완료'
                          ? 'bg-green-500'
                          : 'bg-amber-500'
                      }
                    >
                      {app.status === '결제완료' ? (
                        <CheckCircle2 className="w-3 h-3 mr-1" />
                      ) : (
                        <Clock className="w-3 h-3 mr-1" />
                      )}
                      {app.status}
                    </Badge>
                    <span className="text-xs text-slate-400">
                      신청번호: {app.id}
                    </span>
                  </div>
                  <h3 className="text-xl font-bold text-slate-900">
                    {app.event}
                  </h3>
                </div>
                <div className="text-right">
                  <span className="text-sm text-slate-500">참가비</span>
                  <p className="font-bold text-slate-900">{app.price}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                <div className="col-span-2">
                  <p className="text-xs text-slate-500 uppercase mb-2">
                    진행 단계
                  </p>
                  <Progress value={app.step} className="h-2" />
                </div>
              </div>
              <div className="flex gap-2">
                <Button
                  variant="outline"
                  size="sm"
                  className="border-[#001E62] text-[#001E62]"
                >
                  상세 보기
                </Button>
                {app.status === '입금대기' && (
                  <Button size="sm" className="bg-[#001E62]">
                    결제하기
                  </Button>
                )}
              </div>
            </div>
          </div>
        </Card>
      ))}
    </div>
  );
}
