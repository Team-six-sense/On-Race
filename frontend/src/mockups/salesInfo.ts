import { SalesInfo } from '@/features/event/types';

export const SALES_INFO: SalesInfo = {
  refundPolicy: {
    isRefundable: true,
    isTransferable: false,
    refundStartAt: '2026-03-01T00:00:00',
    refundEndAt: '2026-04-10T23:59:59',
    nonRefundableAt: '2026-04-11T00:00:00',
    cancellationFee: '접수 후 7일 이내 무료, 이후 10%',
    refundPolicy: '대회 취소 시 전액 환불',
    weatherRefund: '우천 시 대회 취소 및 전액 환불',
  },
  delivery: {
    deliveryTarget: '참가 키트 (티셔츠, 배번호, 기념품)',
    deliveryMethod: '택배',
    deliveryStartAt: '2026-04-01T00:00:00',
    deliveryEndAt: '2026-04-15T23:59:59',
    deliveryFee: '무료',
    deliveryArea: '전국',
    addressChangePeriod: '2026-03-25T23:59:59',
    deliveryCompensation: '미배송 시 대회 당일 현장 수령 가능',
  },
  seller: {
    sellerName: '(주)온레이스',
    businessNo: '123-45-67890',
    ecommerceNo: '2026-서울강남-01234',
    isEcommerceMediator: true,
    customerService: '02-1234-5678',
    sellerAddress: '서울특별시 강남구 테헤란로 123',
  },
};
