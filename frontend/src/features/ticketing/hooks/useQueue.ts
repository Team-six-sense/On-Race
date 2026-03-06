'use client';

import { useState, useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { getQueueStatus } from '../services/getQueueStatus';

export const useQueue = (eventId: string) => {
  const router = useRouter();
  const [status, setStatus] = useState<any>(null);
  const [progress, setProgress] = useState(0);

  const initialPosition = useRef<number | null>(null);
  const isRedirecting = useRef(false);
  const timerRef = useRef<NodeJS.Timeout | null>(null); // 타이머 관리를 위한 Ref

  const resetQueue = () => {
    initialPosition.current = null;
    isRedirecting.current = false;
    setStatus(null);
    setProgress(0);
    if (timerRef.current) clearTimeout(timerRef.current);
  };

  useEffect(() => {
    let smoothProgressTimer: NodeJS.Timeout;

    const fetchStatus = async () => {
      // 리다이렉트 중이면 더 이상 API를 호출하지 않음
      if (isRedirecting.current) return;

      try {
        const data: any = await getQueueStatus(eventId);

        if (initialPosition.current === null) {
          initialPosition.current = data.position;
        }

        setStatus(data);

        // 대기 종료 조건
        if (data.position <= 0) {
          isRedirecting.current = true;
          setProgress(100);

          // 이동 전 모든 타이머 정리
          clearInterval(smoothProgressTimer);

          // 결제 페이지로 이동
          router.push(`/ticketing/${eventId}/payment`);
          return;
        }

        const startPos = initialPosition.current ?? data.position;
        const currentPos = data.position;
        const baseProgress =
          startPos > 0 ? ((startPos - currentPos) / startPos) * 100 : 0;

        if (progress < baseProgress) {
          setProgress(Math.min(baseProgress, 99)); // 100%는 이동 직전에만
        }

        // 폴링 간격: 0.1초는 서버에 부하가 큽니다. 보통 1~3초를 권장합니다.
        timerRef.current = setTimeout(fetchStatus, 1000);
      } catch (error) {
        console.error('Queue fetch error:', error);
        // 에러 시 재시도 로직
        timerRef.current = setTimeout(fetchStatus, 5000);
      }
    };

    smoothProgressTimer = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 99) return prev;
        return prev + 0.05;
      });
    }, 200);

    fetchStatus();

    // 언마운트 시 클린업 (이동 시 자동으로 실행됨)
    return () => {
      if (timerRef.current) clearTimeout(timerRef.current);
      clearInterval(smoothProgressTimer);
    };
  }, [eventId, router]); // router 의존성 추가

  return { status, progress, resetQueue };
};
