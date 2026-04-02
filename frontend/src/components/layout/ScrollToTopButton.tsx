'use client'; // 클라이언트 컴포넌트 선언 필수

import { useEffect, useState } from 'react';
import { LuChevronsUp } from 'react-icons/lu';
import { Button } from '../ui/button';

export default function ScrollToTopButton() {
  const [isVisible, setIsVisible] = useState(false);

  // 스크롤 위치에 따라 버튼 표시 여부 결정
  useEffect(() => {
    const toggleVisibility = () => {
      if (window.scrollY > 300) {
        setIsVisible(true);
      } else {
        setIsVisible(false);
      }
    };

    window.addEventListener('scroll', toggleVisibility);
    return () => window.removeEventListener('scroll', toggleVisibility);
  }, []);

  const scrollToTop = () => {
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    });
  };

  if (!isVisible) return null;

  return (
    <Button
      onClick={scrollToTop}
      variant="primary1"
      size="icon"
      rounded="full"
      className="fixed bottom-10 right-10 z-50 border border-white"
      aria-label="Scroll to top"
    >
      <LuChevronsUp />
    </Button>
  );
}
