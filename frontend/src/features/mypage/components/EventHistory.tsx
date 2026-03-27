import React, { useState } from 'react';

import { APP_TYPE } from '@/types/constants';
import { Button } from '@/components/ui/button';

export const EventHistory = () => {
  const [searchType, setSearchType] = useState<string>('ALL');

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

  const appType = [
    { id: 'ALL', label: '전체' },
    { id: 'LOTTERY', label: '응모' },
    { id: 'FIRST_COME', label: '선착순' },
  ];

  return (
    <div className="max-w-6xl mx-auto p-6 min-h-screen">
      <h1 className="text-xl font-bold mb-2 text-gray-800">신청 내역</h1>

      <div className="flex flex-wrap gap-1 mb-2">
        {appType.map((type) => (
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
                  신청일
                </th>
                <th className="px-6 py-4 text-sm font-semibold text-gray-600">
                  신청한 이벤트
                </th>
                <th className="px-6 py-4 text-sm font-semibold text-gray-600 text-center">
                  모집 일정
                </th>
                <th className="px-6 py-4 text-sm font-semibold text-gray-600 text-center">
                  신청상태
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
                    <div className="text-sm text-black">{order.date}</div>
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
                        <h2 className="font-semibold text-black text-lg truncate mb-1">
                          서울 마라톤 2026
                        </h2>

                        {/* 주소 및 코스 */}
                        <div className="flex items-center text-font-medium text-sm min-w-0 mb-1">
                          <span className="truncate shrink-0 max-w-[100px] sm:max-w-[150px]">
                            2026.02.28 (토) 오전 9시
                          </span>
                          <span className="mx-1 shrink-0">·</span>
                          <span className="truncate text-gray-500">
                            서울 여의도 공원
                          </span>
                        </div>

                        {/* 날짜 정보 및 하단 영역 */}
                        <div className="mt-auto flex justify-between items-end">
                          <div className="flex items-center text-gray-500 text-sm">
                            <span>10km</span>
                            <span className="mx-1 shrink-0">·</span>
                            <span className="truncate text-gray-500">
                              5’30’’ ~ 6’30’’/km
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </td>

                  <td className="px-6 py-6 text-center text-sm">
                    <p>2026.02.28 (토)</p>
                    <p>~</p>
                    <p>2026.03.13 (금)</p>
                  </td>
                  <td className="px-6 text-center">
                    <p className="text-base font-semibold pb-1">응모 완료</p>
                    <p className="text-xs text-font-medium pb-1">
                      당첨 발표일: 2026.02.28 (토)
                    </p>
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
