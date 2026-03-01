'use client';

import { useRef, useCallback, useState } from 'react';

export interface MouseLog {
  x: number;
  y: number;
  target: string;
  timestamp: number;
  isTrusted: boolean;
  distance: number;
  // --- 이벤트 타입별 카운트 추가 ---
  moveCount: number;
  downCount: number;
  upCount: number;
  lastEventType: string;
  // ---------------------------
  avgVelocity: number;
}

export const useDetailedTracker = () => {
  const [isRecording, setIsRecording] = useState(false);
  const [logs, setLogs] = useState<MouseLog[]>([]);

  const logBuffer = useRef<MouseLog[]>([]);
  const currentPos = useRef({ x: 0, y: 0, target: 'none', isTrusted: true });
  const accumDistance = useRef(0);
  const lastEventType = useRef('');

  // 인터벌 내 이벤트 발생 횟수 추적
  const counts = useRef({ move: 0, down: 0, up: 0 });
  const timerRef = useRef<NodeJS.Timeout | null>(null);
  const lastTimestamp = useRef(performance.now());

  const handlePointerEvent = useCallback((e: PointerEvent) => {
    const target = e.target as HTMLElement;

    // 1. 타입별 카운팅
    if (e.type === 'pointermove') {
      counts.current.move++;
      accumDistance.current += Math.sqrt(
        Math.pow(e.movementX, 2) + Math.pow(e.movementY, 2),
      );
    } else if (e.type === 'pointerdown') {
      counts.current.down++;
    } else if (e.type === 'pointerup') {
      counts.current.up++;
    }

    lastEventType.current = e.type;

    // 2. 상태 업데이트
    currentPos.current = {
      x: e.pageX,
      y: e.pageY,
      target: `${target.tagName.toLowerCase()}${target.id ? `#${target.id}` : ''}`,
      isTrusted: e.isTrusted,
    };
  }, []);

  const startTracking = useCallback(
    (intervalMs: number = 100) => {
      setIsRecording(true);
      logBuffer.current = [];
      setLogs([]);
      counts.current = { move: 0, down: 0, up: 0 };
      accumDistance.current = 0;
      lastTimestamp.current = performance.now();

      window.addEventListener('pointermove', handlePointerEvent);
      window.addEventListener('pointerdown', handlePointerEvent);
      window.addEventListener('pointerup', handlePointerEvent);

      timerRef.current = setInterval(() => {
        const now = performance.now();
        const dt = now - lastTimestamp.current;

        const newLog: MouseLog = {
          ...currentPos.current,
          timestamp: now,
          distance: Number(accumDistance.current.toFixed(2)),
          moveCount: counts.current.move,
          downCount: counts.current.down,
          upCount: counts.current.up,
          lastEventType: lastEventType.current,
          avgVelocity: Number((accumDistance.current / dt).toFixed(4)),
        };

        logBuffer.current.push(newLog);
        setLogs([...logBuffer.current]);

        // 다음 인터벌을 위한 리셋
        accumDistance.current = 0;
        counts.current = { move: 0, down: 0, up: 0 };
        lastTimestamp.current = now;
      }, intervalMs);
    },
    [handlePointerEvent],
  );

  const stopTracking = useCallback(() => {
    setIsRecording(false);
    if (timerRef.current) clearInterval(timerRef.current);
    window.removeEventListener('pointermove', handlePointerEvent);
    window.removeEventListener('pointerdown', handlePointerEvent);
    window.removeEventListener('pointerup', handlePointerEvent);
    return logBuffer.current;
  }, [handlePointerEvent]);

  return { isRecording, logs, startTracking, stopTracking };
};
