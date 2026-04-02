import { cn } from '@/lib/utils';

export default function MilestonesContent() {
  const eventStatus = [
    {
      label: 'Upcoming Event',
      state: 'READY',
      count: 5,
      color: 'text-black bg-white',
    },
    {
      label: 'Live Now',
      count: 12,
      state: 'IN_PROGRESS',
      color: 'text-black bg-font-accent',
    },
    {
      label: 'Completed Event',
      state: 'END',
      count: 9,
      color: 'text-white bg-gray-800',
    },
  ];

  return (
    <section className="my-4 bg-black">
      <div className="w-full bg-black py-20 px-30 flex justify-center max-w-7xl mx-auto">
        <div className="w-full max-w-5xl flex items-center gap-6">
          <div className="flex justify-end pr-14">
            <div className="text-left">
              <h2 className="text-xl text-font-accent leading-tight whitespace-nowrap">
                Milestones
              </h2>
              <p className="text-4xl text-white font-medium leading-tight mt-1 whitespace-nowrap">
                우리가 함께한 순간들
              </p>
            </div>
          </div>

          {/* 2, 3, 4번 카드: 각각 동일한 비율(flex-1)로 배치 */}
          {eventStatus.map((item, index) => (
            <div
              key={index}
              className={cn(
                'flex-1 py-10 rounded-sm flex flex-col items-center justify-center text-center',
                item.color,
              )}
            >
              <span className="text-base font-semibold mb-1">{item.label}</span>
              <div className="flex items-baseline">
                <span className="text-5xl font-bold">{item.count}</span>

                {item.state == 'READY' && (
                  <span className="text-5xl font-bold"> + </span>
                )}
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
