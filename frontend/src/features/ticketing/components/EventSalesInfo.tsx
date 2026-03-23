'use client';

import { useEffect, useState } from 'react';
import {
  OperatingPolicy,
  RefundPolicy,
  SellerInfo,
  ShippingDetails,
} from './details/sales';

export function EventSalesInfo() {
  // 하이드레이션 오류 방지를 위한 마운트 상태 관리
  const [mounted, setMounted] = useState(false);

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  // 아직 마운트되지 않았다면 껍데기(Skeleton) 혹은 null 반환
  if (!mounted) {
    return <div className="mb-6 min-h-[100px]" />; // 레이아웃 시프트를 방지하기 위해 최소 높이 설정
  }

  return (
    <div className="space-y-4">
      {/* 판매자 정보 */}
      <SellerInfo />

      {/* 구분선 */}
      <div className="my-3 h-[1px] bg-gray-300"></div>

      {/* 배송 정보 */}
      <ShippingDetails />

      {/* 구분선 */}
      <div className="my-3 h-[1px] bg-gray-300"></div>

      {/* 취소 및 환불 정책 */}
      <RefundPolicy />

      {/* 구분선 */}
      <div className="my-3 h-[1px] bg-gray-300"></div>

      {/* 판매 및 배송 정보 */}
      <OperatingPolicy />
    </div>
  );
}
