'use client';

import { useRef, useCallback, useState, useEffect } from 'react';

export interface MouseLog {
  x: number;
  y: number;
  timestamp: number;
  eventType: string;
}

const THROTTLE_MS = 30;
const CLEANUP_THRESHOLD = 50; // 50개 쌓일 때마다 청소
const RETENTION_MS = 70 * 1000; // 70초 유지

export const useDetailedTracker = () => {
  const [isRecording, setIsRecording] = useState(false);
  // UI 표시용 로그 (필요 없다면 제거하여 성능을 대폭 향상시킬 수 있습니다)
  const [logs, setLogs] = useState<MouseLog[]>([]);

  const logBuffer = useRef<MouseLog[]>([]);
  const minDistanceRef = useRef(10);
  const lastLoggedPoint = useRef<{ x: number; y: number; time: number } | null>(
    null,
  );

  // 70초 지난 오래된 데이터를 삭제하는 함수
  const cleanupOldLogs = useCallback(() => {
    const now = Date.now();
    const cutoff = now - RETENTION_MS;

    // 효율적인 삭제를 위해 첫 번째로 유지해야 할 인덱스를 찾음
    const firstValidIndex = logBuffer.current.findIndex(
      (l) => l.timestamp >= cutoff,
    );

    if (firstValidIndex > 0) {
      logBuffer.current.splice(0, firstValidIndex);
    }
  }, []);

  const handlePointerEvent = useCallback(
    (e: PointerEvent) => {
      const now = Date.now(); // performance.now()보다 Unix Timestamp 계산에 더 직관적
      const x = e.pageX;
      const y = e.pageY;

      // 1. 필터링 로직 (Throttle & Distance)
      if (e.type === 'pointermove' && lastLoggedPoint.current) {
        const dx = x - lastLoggedPoint.current.x;
        const dy = y - lastLoggedPoint.current.y;
        const distFromLast = Math.sqrt(dx * dx + dy * dy);
        const timeFromLast = now - lastLoggedPoint.current.time;

        if (
          timeFromLast < THROTTLE_MS &&
          distFromLast < minDistanceRef.current
        ) {
          return;
        }
      }

      const newLog: MouseLog = {
        x,
        y,
        timestamp: now,
        eventType: e.type,
      };

      // 2. 데이터 저장
      logBuffer.current.push(newLog);

      // 3. 주기적 청소 (70초 경과 데이터 삭제)
      if (logBuffer.current.length % CLEANUP_THRESHOLD === 0) {
        cleanupOldLogs();
      }

      // 4. 상태 업데이트 (UI에 로그를 실시간으로 보여줄 필요가 없다면 이 줄을 주석 처리하세요)
      setLogs([...logBuffer.current]);

      lastLoggedPoint.current = { x, y, time: now };
    },
    [cleanupOldLogs],
  );

  const startTracking = useCallback(
    (minDistance: number) => {
      minDistanceRef.current = minDistance;
      setIsRecording(true);
      setLogs([]);
      logBuffer.current = [];
      lastLoggedPoint.current = null;

      window.addEventListener('pointermove', handlePointerEvent);
      window.addEventListener('pointerdown', handlePointerEvent);
      window.addEventListener('pointerup', handlePointerEvent);
    },
    [handlePointerEvent],
  );

  const stopTracking = useCallback(() => {
    setIsRecording(false);
    window.removeEventListener('pointermove', handlePointerEvent);
    window.removeEventListener('pointerdown', handlePointerEvent);
    window.removeEventListener('pointerup', handlePointerEvent);
    return logBuffer.current;
  }, [handlePointerEvent]);

  // 컴포넌트 언마운트 시 이벤트 리스너 제거 (메모리 누수 방지)
  useEffect(() => {
    return () => {
      window.removeEventListener('pointermove', handlePointerEvent);
      window.removeEventListener('pointerdown', handlePointerEvent);
      window.removeEventListener('pointerup', handlePointerEvent);
    };
  }, [handlePointerEvent]);

  const getFinalData = useCallback(() => {
    const cutoff = Date.now() - 60000; // 정확히 최근 60초 데이터만 추출
    return logBuffer.current.filter((log) => log.timestamp >= cutoff);
  }, []);

  return { isRecording, logs, startTracking, stopTracking, getFinalData };
};
