'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { getQueueStatus } from '../services/getQueueStatus';

export const useQueue = (eventId: string) => {
  const router = useRouter();
  const [status, setStatus] = useState<any>(null);
  const [progress, setProgress] = useState(0);

  // [추가] 처음 진입했을 때의 내 순번을 기억합니다.
  const initialPosition = useRef<number | null>(null);
  const isRedirecting = useRef(false);

  useEffect(() => {
    let pollingTimer: NodeJS.Timeout;
    let smoothProgressTimer: NodeJS.Timer;

    const fetchStatus = async () => {
      try {
        const data: any = await getQueueStatus(eventId);

        // 1. 처음 들어왔을 때만 초기 순번을 고정합니다.
        if (initialPosition.current === null) {
          initialPosition.current = data.position;
        }

        setStatus(data);

        if (data.position <= 0 && !isRedirecting.current) {
          isRedirecting.current = true;
          setProgress(100);
          setTimeout(() => router.push(`/ticketing/${eventId}/payment`), 500);
          return;
        }

        // 2. [수정된 공식] 상대적 진행률 계산
        // (초기순번 - 현재순번) / 초기순번 * 100
        // 예: 처음에 100번이었는데 지금 80번이면 (100-80)/100 = 20%
        const startPos = initialPosition.current ?? data.position;
        const currentPos = data.position;

        // 처음 위치와 현재 위치의 차이를 이용해 0%부터 시작하게 함
        const baseProgress =
          startPos > 0 ? ((startPos - currentPos) / startPos) * 100 : 0;

        if (progress < baseProgress) {
          setProgress(baseProgress);
        }

        pollingTimer = setTimeout(fetchStatus, 100);
      } catch (error) {
        console.error(error);
      }
    };

    // 0.1초마다 조금씩 채워주는 로직 (더 부드럽게)
    smoothProgressTimer = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 99.9) return prev;
        // 아주 미세하게 증가시켜서 바가 멈추지 않게 함
        return prev + 0.02;
      });
    }, 100);

    fetchStatus();

    return () => {
      clearTimeout(pollingTimer);
      clearInterval(smoothProgressTimer as any);
    };
  }, [eventId]);

  return { status, progress };
};
