'use client';

import { Button } from '@/components/ui/button';
import {
  EventHistoryPage,
  PaymentHistoryPage,
  AccountSettings,
} from '@/features/mypage/components';
import { useState } from 'react';

export default function MyPage() {
  const [activeTab, setActiveTab] = useState('account');

  const menuItems = [
    { id: 'account', label: '회원정보 수정' },
    { id: 'event', label: '신청내역' },
    { id: 'payment', label: '결제내역' },
    { id: 'support', label: '고객지원' },
  ];

  const renderContent = () => {
    switch (activeTab) {
      case 'account':
        return <AccountSettings />;
      case 'event':
        return <EventHistoryPage />;
      case 'payment':
        return <PaymentHistoryPage />;
      case 'support':
        return <div>고객지원 콘텐츠</div>;
      default:
        return <AccountSettings />;
    }
  };

  return (
    <div className="max-w-6xl mx-auto my-10  flex flex-col gap-8 min-h-screen">
      <header className="text-black px-4">
        <h2 className="text-3xl font-bold">마이페이지</h2>
        <div className="my-2 h-[2px] bg-black"></div>
      </header>

      <div className="flex flex-row gap-8">
        {/* 왼쪽 사이드바 메뉴 */}

        <nav className="flex flex-col gap-2">
          {menuItems.map((item) => (
            <Button
              key={item.id}
              variant="text"
              className="text-lg justify-start"
              onClick={() => setActiveTab(item.id)}
            >
              {item.label}
            </Button>
          ))}
        </nav>

        {/* 오른쪽 콘텐츠 영역 */}
        <section className="flex-1 rounded-sm bg-white">
          {renderContent()}
        </section>
      </div>
    </div>
  );
}
