'use client';

import React, { useState } from 'react';
import { cn } from '@/lib/utils';
import { ShadowTable } from '@/features/design/components/shadow';
import { MenuType } from '@/types/constants';

import { IconTable } from '@/features/design/components/icon';
import ButtomDemo from '@/features/design/components/button/ButtonDemo';
import InputDemo from '@/features/design/components/input/inputDemo';
import CheckboxDemo from '@/features/design/components/checkbox/CheckboxDemo';
import RadioGroupDemo from '@/features/design/components/radioGroup/RadioGroupDemo';
import SelectDemo from '@/features/design/components/select/SelectDemo';
import ModalDemo from '@/features/design/components/modal/modalDemo';

const MENU_ITEMS: { id: MenuType; label: string; description: string }[] = [
  {
    id: 'icon',
    label: 'Icon',
    description: '',
  },
  {
    id: 'shadow',
    label: 'Shadow',
    description: '',
  },
  {
    id: 'button',
    label: 'Button',
    description: '',
  },

  {
    id: 'input',
    label: 'Input',
    description: '',
  },
  {
    id: 'checkbox',
    label: 'Checkbox',
    description: '',
  },
  {
    id: 'radioGroup',
    label: 'RadioGroup',
    description: '',
  },
  {
    id: 'selectbox',
    label: 'Selectbox',
    description: '',
  },
  {
    id: 'modal',
    label: 'Modal',
    description: '',
  },
];

export default function ButtonShowcasePage() {
  const [activeMenu, setActiveMenu] = useState<MenuType>('icon');

  const currentMenu =
    MENU_ITEMS.find((item) => item.id === activeMenu) || MENU_ITEMS[0];

  return (
    <div className="flex min-h-screen bg-white">
      {/* 사이드바 */}
      <aside className="w-64 border-r bg-zinc-50/50 p-6 flex flex-col gap-2">
        <h2 className="text-xs font-semibold text-zinc-400 mb-4 px-2 uppercase tracking-[0.2em]">
          UI Library
        </h2>
        <nav className="flex flex-col gap-1">
          {MENU_ITEMS.map((menu) => (
            <button
              key={menu.id}
              onClick={() => setActiveMenu(menu.id)}
              className={cn(
                'text-left px-4 py-2 rounded-md text-sm font-medium transition-all duration-200',
                activeMenu === menu.id
                  ? 'bg-black text-white shadow-md'
                  : 'text-zinc-600 hover:bg-zinc-200',
              )}
            >
              {menu.label}
            </button>
          ))}
        </nav>
      </aside>

      {/* 메인 콘텐츠 */}

      <main className="flex-1 p-10 overflow-x-auto">
        <div className="max-w-5xl">
          <header className="mb-10">
            <h1 className="text-3xl font-bold capitalize">
              {currentMenu.label}
            </h1>
            <p className="text-zinc-500 mt-2">{currentMenu.description}</p>
          </header>

          {/* 조건부 렌더링 영역 */}

          <div className="rounded-xl border border-zinc-100 bg-white p-2">
            {
              {
                icon: <IconTable />,
                shadow: <ShadowTable />,
                button: <ButtomDemo />,
                input: <InputDemo />,
                checkbox: <CheckboxDemo />,
                radioGroup: <RadioGroupDemo />,
                selectbox: <SelectDemo />,
                modal: <ModalDemo />,
              }[activeMenu] || <IconTable /> // 기본값 설정 가능
            }
          </div>
        </div>
      </main>
    </div>
  );
}
