import React, { useEffect, useState } from 'react';
import {
  DATE_FILTER_OPTIONS,
  getAppTypeLabel,
  getStatusConfig,
  getStatusLabel,
} from '@/types/constants';
import { Button } from '@/components/ui/button';
import { LuChevronLeft, LuChevronRight } from 'react-icons/lu';
import { cn } from '@/lib/utils';
import { formatKoreanDate } from '@/features/ticketing/utils/date';
import { myPageService } from '../services';
import { OrderHistory } from '../types';

export const PaymentHistoryPage = () => {
  const [mounted, setMounted] = useState(false);
  const [searchType, setSearchType] = useState<string>('ALL');
  const [paymentHistory, setPaymentHistory] = useState<OrderHistory[]>([]);

  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  // 상단 주문 프로세스 데이터
  const orderSteps = [
    { label: '입금대기', count: 1 },
    { label: '결제완료', count: 0 },
    { label: '상품준비중', count: 2 },
    { label: '배송중', count: 1 },
    { label: '배송완료', count: 12 },
    {
      label: '구매확정',
      count: 20,
    },
  ];

  // 상단 CS 데이터
  const csSteps = [
    {
      label: '취소',
      count: 1,
    },
    {
      label: '교환',
      count: 0,
    },
    {
      label: '반품',
      count: 0,
    },
  ];

  // 컴포넌트가 마운트된 후에만 렌더링을 허용
  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    const fetchData = async () => {
      if (!mounted) return;

      try {
        const response = await myPageService.getOrderHistory();
        setPaymentHistory(response.data);
      } catch (error) {
        console.error('데이터 로드 실패:', error);
      }
    };

    fetchData();
  }, [mounted]);

  const filteredList = paymentHistory.filter((event) =>
    searchType === 'ALL' ? true : event.appType === searchType,
  );

  const totalPages = Math.ceil(filteredList.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentItems = filteredList.slice(
    startIndex,
    startIndex + itemsPerPage,
  );

  const displayStatusLabel = (status: string) => {
    let displayLabel = '';

    switch (status) {
      case 'READY':
        const dateStr = '2026-03-15T09:00:00';
        const date = new Date(dateStr);

        const formattedDate = new Intl.DateTimeFormat('ko-KR', {
          month: 'long', // "3월"
          day: 'numeric', // "15일"
          hour: 'numeric', // "9시"
          minute: 'numeric',
          hour12: false, // 24시간 형식
        }).format(date);

        displayLabel = `${formattedDate} ${getStatusLabel(status)}`;
        break;
      case 'CLOSING_SOON':
        displayLabel = `내일 ${getStatusLabel(status)}`;
        break;
      case 'DRAW_COMPLETED':
        displayLabel = getStatusLabel('END');
        break;
      default:
        displayLabel = getStatusLabel(status);
    }

    return displayLabel;
  };

  return (
    <div className="max-w-6xl mx-auto min-h-screen">
      <h1 className="text-xl font-bold mb-2 text-gray-800">결제 내역</h1>

      {/* 상단 상태 카드 섹션 */}
      <div className="flex flex-col lg:flex-row mb-10 border border-cta-outline rounded-sm">
        {/* 주문 프로세스 */}
        <div className="flex-[3] bg-white p-6 flex justify-between items-center relative overflow-hidden">
          {orderSteps.map((step, index) => (
            <React.Fragment key={index}>
              <div className="flex flex-col items-center flex-1 z-10">
                <span
                  className={cn(
                    'text-xl border-b mb-1',
                    step.count === 0
                      ? 'text-font-disabled border-font-disabled'
                      : 'text-font-medium border-font-medium',
                  )}
                >
                  {step.count}
                </span>
                <span className="text-base font-medium">{step.label}</span>
              </div>
              {index < orderSteps.length - 1 && (
                <LuChevronRight className="text-gray-400" size={16} />
              )}
            </React.Fragment>
          ))}
        </div>

        {/* CS 현황 */}
        <div className="flex-1 bg-secondary p-6">
          {csSteps.map((step, index) => (
            <div key={index} className="flex flex-row items-center">
              <div className="flex items-center justify-between w-full">
                <span className="text-base font-medium">{step.label}</span>
                <span
                  className={cn(
                    'text-base border-b',
                    step.count === 0
                      ? 'text-font-disabled border-font-disabled'
                      : 'text-font-medium border-font-medium',
                  )}
                >
                  {step.count}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="flex flex-wrap gap-1 mb-2">
        {DATE_FILTER_OPTIONS.map((type) => (
          <Button
            key={type.id}
            variant="outline"
            size="fit"
            rounded="full"
            onClick={() => {
              setSearchType(type.id);
            }}
            className={cn(
              'border',
              // searchType !== type.id && 'border-gray-400',

              searchType === type.id
                ? 'border-2 text-black border-black'
                : 'border text-font-low border-cta-outline',
            )}
          >
            {type.label}
          </Button>
        ))}
      </div>

      {/* 하단 결제 리스트 섹션 */}
      <div className="bg-white rounded-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-gray-100 border-t border-b border-gray-300 font-medium text-sm text-font-medium text-center">
                <th className="py-4">주문번호/주문일</th>
                <th className="py-4">주문상품</th>
                <th className="py-4">주문상태</th>
                <th className="py-4">결제금액</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {currentItems.length > 0 ? (
                currentItems.map((order, index) => (
                  <tr
                    key={`${order.id}-${index}`} // 중복 ID로 인한 렌더링 오류 방지
                    className="transition-colors"
                  >
                    <td className="px-6 py-6 text-center">
                      <div className="text-sm text-font-medium mb-2">
                        <span className="border-b border-font-medium">
                          {order.id}
                        </span>
                      </div>
                      <div className="text-sm text-black">
                        {formatKoreanDate(order.date)}
                      </div>
                    </td>

                    <td className="px-6 py-6">
                      <div className="flex gap-4 items-start">
                        <div className="relative w-24 h-24 shrink-0 overflow-hidden rounded-sm">
                          <img
                            src={'/image/default.png'}
                            alt={'이벤트'}
                            className="absolute inset-0 w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
                          />
                        </div>

                        <div className="flex flex-col flex-1 min-w-0 py-0.5">
                          <div className="flex gap-1 mb-1">
                            <div
                              className={cn(
                                'inline-block px-2 py-0.5 rounded-sm text-[10px] font-bold bg-gray-100 text-gray-500',
                                getStatusConfig(order.status),
                              )}
                            >
                              {getStatusLabel(order.status)}
                            </div>
                            <div className="inline-block px-2 py-0.5 rounded-sm text-[10px] font-bold bg-gray-100 text-gray-500">
                              {getAppTypeLabel(order.appType)}
                            </div>
                          </div>

                          <h2 className="font-bold text-black text-lg">
                            {order.title}
                          </h2>

                          <div className="flex items-center text-font-medium text-sm min-w-0">
                            <span className="truncate shrink-0 max-w-[100px] sm:max-w-[150px]">
                              {formatKoreanDate(order.eventAt)}
                            </span>
                            <span className="mx-1 shrink-0">·</span>
                            <span>{order.venue}</span>
                          </div>

                          <div className="mt-auto flex justify-between items-end">
                            <div className="flex items-center text-gray-500 text-sm">
                              <span>{order.course}</span>
                              <span className="mx-1 shrink-0">·</span>
                              <span>{order.pace}</span>
                            </div>
                          </div>
                        </div>
                      </div>
                    </td>

                    <td className="px-6 py-6 text-center text-sm">
                      {order.orderStatus}
                      {(order.orderStatus === '배송중' ||
                        order.orderStatus === '배송완료' ||
                        order.orderStatus === '구매확정') && (
                        <div className="pt-1">
                          <Button variant="outline" size="fit" rounded="sm">
                            배송조회
                          </Button>
                        </div>
                      )}

                      {order.orderStatus === '결제취소' && (
                        <div className="pt-1">
                          <Button variant="outline" size="fit" rounded="sm">
                            취소내역
                          </Button>
                        </div>
                      )}

                      {order.orderStatus === '교환완료' && (
                        <div className="pt-1">
                          <Button variant="outline" size="fit" rounded="sm">
                            교환내역
                          </Button>
                        </div>
                      )}

                      {order.orderStatus === '환불완료' && (
                        <div className="pt-1">
                          <Button variant="outline" size="fit" rounded="sm">
                            환불내역
                          </Button>
                        </div>
                      )}
                    </td>

                    <td className="px-6 py-6 font-semibold text-center text-base">
                      {order.price.toLocaleString('ko-KR')}원
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td
                    colSpan={4}
                    className="py-20 text-center text-gray-500 text-sm"
                  >
                    결제 내역이 없습니다.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        {/* 페이지네이션 컨트롤 */}
        {totalPages > 1 && (
          <div className="flex justify-center items-center gap-2 py-8 border-t border-gray-100">
            {/* [이전] 버튼: 현재 그룹의 첫 페이지보다 하나 앞으로 이동 */}
            <Button
              variant="ghost"
              size="iconSm"
              onClick={() => setCurrentPage((prev) => Math.max(prev - 1, 1))}
              disabled={currentPage === 1}
            >
              <LuChevronLeft />
            </Button>

            {/* 숫자 버튼 로직: 10개 단위 그룹화 */}
            {(() => {
              const pageGroupSize = 10; // 한 번에 보여줄 페이지 수
              // 현재 페이지가 속한 그룹의 시작 번호 계산 (1, 11, 21...)
              const currentGroup = Math.floor(
                (currentPage - 1) / pageGroupSize,
              );
              const startPage = currentGroup * pageGroupSize + 1;
              // 끝 번호 계산 (10, 20, 30... 단, totalPages를 넘지 않음)
              const endPage = Math.min(
                startPage + pageGroupSize - 1,
                totalPages,
              );

              return Array.from(
                { length: endPage - startPage + 1 },
                (_, i) => startPage + i,
              ).map((pageNum) => (
                <Button
                  key={pageNum}
                  variant={currentPage === pageNum ? 'primary1' : 'ghost'}
                  size="sm"
                  className="w-8 h-8 p-0"
                  onClick={() => setCurrentPage(pageNum)}
                >
                  {pageNum}
                </Button>
              ));
            })()}

            {/* [다음] 버튼: 현재 그룹의 마지막 페이지보다 하나 뒤로 이동 */}
            <Button
              variant="ghost"
              size="iconSm"
              onClick={() =>
                setCurrentPage((prev) => Math.min(prev + 1, totalPages))
              }
              disabled={currentPage === totalPages}
            >
              <LuChevronRight />
            </Button>
          </div>
        )}
      </div>
    </div>
  );
};
