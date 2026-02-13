'use client';

import React, { useState } from 'react';
import { RadioGroup, RadioGroupItem } from '@/components/ui/radioGroup';

export default function RadioGroupDemo() {
  const [selectedValue, setSelectedValue] = useState('card');

  return (
    <div className="p-10 space-y-12 bg-blue-50 min-h-screen">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Radio Group System</h1>
        <p className="text-gray-500 mt-2">
          모든 RadioGroupItem은 RadioGroup 내부에서 렌더링되어야 합니다.
        </p>
      </div>

      {/* 1. 기본 베리언트 (Primary vs Error) */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          1. Variants
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
          {/* Primary */}
          <div className="space-y-4">
            <h3 className="text-sm font-bold text-gray-400 uppercase tracking-tight">
              Primary (Default)
            </h3>
            <RadioGroup defaultValue="p1">
              <div className="flex items-center space-x-3">
                <RadioGroupItem value="p1" id="p1" variant="primary" />
                <label
                  htmlFor="p1"
                  className="text-sm font-medium cursor-pointer"
                >
                  기본 옵션 1
                </label>
              </div>
              <div className="flex items-center space-x-3">
                <RadioGroupItem value="p2" id="p2" variant="primary" />
                <label
                  htmlFor="p2"
                  className="text-sm font-medium cursor-pointer"
                >
                  기본 옵션 2
                </label>
              </div>
            </RadioGroup>
          </div>

          {/* Error (using isError prop) */}
          <div className="space-y-4">
            <h3 className="text-sm font-bold text-red-500 uppercase tracking-tight">
              Error (via isError prop)
            </h3>
            <RadioGroup defaultValue="e1">
              <div className="flex items-center space-x-3">
                <RadioGroupItem value="e1" id="e1" isError={true} />
                <label
                  htmlFor="e1"
                  className="text-sm font-medium text-status-error cursor-pointer"
                >
                  에러 옵션 1
                </label>
              </div>
              <div className="flex items-center space-x-3">
                <RadioGroupItem value="e2" id="e2" isError={true} />
                <label
                  htmlFor="e2"
                  className="text-sm font-medium text-status-error cursor-pointer"
                >
                  에러 옵션 2
                </label>
              </div>
            </RadioGroup>
          </div>
        </div>
      </section>

      {/* 2. 사이즈 (SM, Default, LG) */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          2. Sizes
        </h2>
        <div className="flex flex-wrap items-end gap-12 bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
          <div className="flex flex-col items-center gap-3">
            <RadioGroup defaultValue="sm">
              <RadioGroupItem value="sm" size="sm" />
            </RadioGroup>
            <span className="text-xs font-bold text-gray-500">Small</span>
          </div>
          <div className="flex flex-col items-center gap-3">
            <RadioGroup defaultValue="default">
              <RadioGroupItem value="default" size="default" />
            </RadioGroup>
            <span className="text-xs font-bold text-gray-500">Default</span>
          </div>
          <div className="flex flex-col items-center gap-3">
            <RadioGroup defaultValue="lg">
              <RadioGroupItem value="lg" size="lg" />
            </RadioGroup>
            <span className="text-xs font-bold text-gray-500">Large</span>
          </div>
        </div>
      </section>

      {/* 3. 상태별 (Disabled / Checked) */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          3. States
        </h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 bg-white p-8 rounded-2xl shadow-sm border border-gray-100">
          <div className="space-y-2 text-center">
            <p className="text-xs font-bold text-gray-400">Unchecked</p>
            <RadioGroup>
              <RadioGroupItem value="u1" className="mx-auto" />
            </RadioGroup>
          </div>
          <div className="space-y-2 text-center">
            <p className="text-xs font-bold text-gray-400">Checked</p>
            <RadioGroup defaultValue="c1">
              <RadioGroupItem value="c1" className="mx-auto" />
            </RadioGroup>
          </div>
          <div className="space-y-2 text-center">
            <p className="text-xs font-bold text-gray-400">Disabled Off</p>
            <RadioGroup disabled>
              <RadioGroupItem value="d1" className="mx-auto" />
            </RadioGroup>
          </div>
          <div className="space-y-2 text-center">
            <p className="text-xs font-bold text-gray-400">Disabled On</p>
            <RadioGroup disabled defaultValue="d2">
              <RadioGroupItem value="d2" className="mx-auto" />
            </RadioGroup>
          </div>
        </div>
      </section>

      {/* 4. 실무 적용 예시 */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          4. Real-world Example
        </h2>
        <div className="max-w-md bg-white p-6 rounded-2xl shadow-lg border border-gray-100 space-y-6">
          <h3 className="text-lg font-bold">결제 수단</h3>
          <RadioGroup
            value={selectedValue}
            onValueChange={setSelectedValue}
            className="gap-3"
          >
            {[
              { id: 'card', label: '신용카드' },
              { id: 'transfer', label: '계좌이체' },
              { id: 'kakao', label: '카카오페이' },
            ].map((item) => (
              <label
                key={item.id}
                htmlFor={item.id}
                className="flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 has-[:checked]:border-black"
              >
                <RadioGroupItem value={item.id} id={item.id} />
                <span className="text-sm font-medium">{item.label}</span>
              </label>
            ))}
          </RadioGroup>
        </div>
      </section>
    </div>
  );
}
