import React, { useState } from 'react';
import { DATE_FILTER_OPTIONS } from '@/types/constants';
import { Button } from '@/components/ui/button';
import { LuChevronRight } from 'react-icons/lu';

export const PaymentHistory = () => {
  const [searchType, setSearchType] = useState<string>('ALL');

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

  // 하단 결제 리스트 더미 데이터
  const orderList = [
    {
      id: 'ORD-20231025-001',
      date: '2023.10.25',
      name: '프리미엄 코튼 티셔츠 외 1건',
      option: '화이트 / L',
      status: '입금 대기',
      price: '35,000원',
    },
    {
      id: 'ORD-20231020-042',
      date: '2023.10.20',
      name: '울 블렌드 카디건',
      option: '네이비 / M',
      status: '결제 완료',
      price: '89,000원',
    },
    {
      id: 'ORD-20231015-011',
      date: '2023.10.15',
      name: '데님 팬츠',
      option: '중청 / 32',
      status: '상품 준비중',
      price: '42,000원',
    },
  ];

  return (
    <div className="max-w-6xl mx-auto p-6 min-h-screen">
      <h1 className="text-2xl font-bold mb-2 text-gray-800">결제 내역</h1>

      {/* 상단 상태 카드 섹션 */}
      <div className="flex flex-col lg:flex-row mb-10 border border-gray-200 rounded-sm">
        {/* 주문 프로세스 */}
        <div className="flex-[3] bg-white p-6 flex justify-between items-center relative overflow-hidden">
          {orderSteps.map((step, index) => (
            <React.Fragment key={index}>
              <div className="flex flex-col items-center flex-1 z-10">
                <span
                  className={`text-xl border-b-2 mb-1 ${step.count === 0 ? 'text-gray-300' : 'text-gray-600'}`}
                >
                  {step.count}
                </span>
                <span className="text-xs font-medium">{step.label}</span>
              </div>
              {index < orderSteps.length - 1 && (
                <LuChevronRight className="text-gray-400" size={16} />
              )}
            </React.Fragment>
          ))}
        </div>

        {/* CS 현황 */}
        <div className="flex-1 bg-gray-50 p-6">
          {csSteps.map((step, index) => (
            <div key={index} className="flex flex-row items-center">
              <div className="flex items-center justify-between w-full">
                <span className="text-xs font-medium">{step.label}</span>
                <span
                  className={`border-b-2 pb-0.5 ${step.count === 0 ? 'text-gray-300' : 'text-gray-600'}`}
                >
                  {step.count}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="flex flex-wrap gap-2 mb-2">
        {DATE_FILTER_OPTIONS.map((type) => (
          <Button
            key={type.id}
            variant={searchType === type.id ? 'primary1' : 'outline'}
            size="fit"
            rounded="full"
            onClick={() => {
              setSearchType(type.id);
            }}
            className={`
            ${
              searchType === type.id
                ? '' // 선택되었을 때 스타일
                : 'border-gray-400' // 비선택 스타일
            }
            border
          `}
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
              <tr className="bg-gray-100 border-t border-b border-gray-300">
                <th className="px-6 py-4 text-sm font-semibold text-gray-600 text-center">
                  주문번호/주문일
                </th>
                <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                  주문상품
                </th>
                <th className="px-6 py-4 text-sm font-semibold text-gray-600 text-center">
                  주문상태
                </th>
                <th className="px-6 py-4 text-sm font-semibold text-gray-600 text-center">
                  결제금액
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {orderList.map((order) => (
                <tr
                  key={order.id}
                  className="hover:bg-gray-50 transition-colors"
                >
                  <td className="px-6 py-6 text-center">
                    <div className="text-xs text-gray-600 mb-2">
                      <span className="border-b">{order.id}</span>
                    </div>
                    <div className="text-sm font-medium text-gray-800">
                      {order.date}
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

                      {/* 오른쪽: 카드 바디 (내용) 영역 */}
                      <div className="flex flex-col flex-1 min-w-0 py-0.5">
                        {/* 라벨들 */}
                        <div className="flex gap-1 mb-1">
                          <div className="inline-block px-2 py-0.5 rounded-sm text-[10px] font-bold bg-gray-100 text-gray-500">
                            모집마감
                          </div>
                          <div className="inline-block px-2 py-0.5 rounded-sm text-[10px] font-bold bg-gray-100 text-gray-500">
                            추첨
                          </div>
                        </div>

                        {/* 제목 */}
                        <h2 className="font-bold text-black text-base sm:text-lg leading-tight truncate mb-1">
                          서울 마라톤 2026
                        </h2>

                        {/* 주소 및 코스 */}
                        <div className="flex items-center text-gray-500 text-xs sm:text-sm min-w-0 mb-1">
                          <span className="truncate shrink-0 max-w-[100px] sm:max-w-[150px]">
                            2026.02.28 (토) 오전 9시
                          </span>
                          <span className="mx-1 shrink-0">·</span>
                          <span className="truncate text-gray-400">
                            서울 여의도 공원
                          </span>
                        </div>

                        {/* 날짜 정보 및 하단 영역 */}
                        <div className="mt-auto flex justify-between items-end">
                          <div className="flex items-center text-gray-400 text-xs sm:text-sm">
                            <span>10km</span>
                            <span className="mx-1 shrink-0">·</span>
                            <span className="truncate text-gray-400">
                              5’30’’ ~ 6’30’’/km
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </td>

                  <td className="px-6 py-6 text-center text-sm">
                    {order.status}
                  </td>
                  <td className="px-6 py-6 font-semibold text-center text-sm">
                    {order.price}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
