// features/ticketing/hooks/useQueue.ts
import { useQuery } from '@tanstack/react-query';

import { useState } from 'react';
import { getQueueStatus } from '../services/getQueueStatus';
import { QueueStatus } from '../types';

export const useQueue = (eventId: string) => {
  // 사용자가 수동으로 멈췄는지 확인하는 상태
  const [isStoppedByUser, setIsStoppedByUser] = useState(false);

  const query = useQuery<QueueStatus>({
    queryKey: ['queue', eventId],
    queryFn: () => getQueueStatus(eventId) as Promise<QueueStatus>,
    // 사용자가 멈추지 않았을 때만 폴링 진행
    refetchInterval: (query) => {
      if (isStoppedByUser) return false;
      if (query.state.data?.status === 'passed') return false;
      return 2000;
    },
    refetchIntervalInBackground: true,
    // 사용자가 중단했다면 아예 쿼리 자체를 비활성화(선택 사항)
    enabled: !isStoppedByUser,
  });

  return {
    ...query,
    status: query.isLoading ? null : query.data,
    progress: query.data
      ? Math.min(100, 100 - (query.data as any).position)
      : 0,
    // 수동 중단 함수
    stopPolling: () => setIsStoppedByUser(true),
    isStoppedByUser,
  };
};
