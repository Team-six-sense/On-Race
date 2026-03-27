'use client';

import Script from 'next/script';
import { useRef } from 'react';

export default function LocalLottie() {
  const containerRef = useRef<HTMLDivElement>(null);

  const initLottie = () => {
    if ((window as any).lottie && containerRef.current) {
      const anim = (window as any).lottie.loadAnimation({
        container: containerRef.current,
        renderer: 'svg',
        loop: true,
        autoplay: true,
        path: '/animations/queue.json', // public 폴더 내 JSON
      });

      anim.setSpeed(2.5); // 2배속 설정
    }
  };

  return (
    <div>
      {/* 우리 서버 내에 있는 파일을 불러오므로 외부 의존성 없음 */}
      <Script src="/js/lottie.min.js" onLoad={initLottie} />
      <div ref={containerRef} style={{ width: 500, height: 300 }} />
    </div>
  );
}
