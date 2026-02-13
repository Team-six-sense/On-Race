'use client';

import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

export default function SelectDemo() {
  const options = [
    { value: 'apple', label: 'Apple' },
    { value: 'banana', label: 'Banana' },
    { value: 'orange', label: 'Orange' },
  ];

  return (
    <div className="p-10 space-y-12 bg-blue-50 min-h-screen text-gray-900">
      <div>
        <h1 className="text-3xl font-bold">Select Component System</h1>
      </div>

      {/* 1. 베리언트 확인 */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2">1. Variants</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 bg-white p-8 rounded-xl border">
          <div className="space-y-2">
            <label className="text-sm font-bold">Default Variant</label>
            <Select>
              <SelectTrigger variant="default">
                <SelectValue placeholder="과일을 선택하세요" />
              </SelectTrigger>
              <SelectContent>
                {options.map((opt) => (
                  <SelectItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-bold text-status-error">
              Error Variant
            </label>
            <Select>
              <SelectTrigger variant="error">
                <SelectValue placeholder="필수 선택 항목입니다" />
              </SelectTrigger>
              <SelectContent>
                {options.map((opt) => (
                  <SelectItem key={opt.value} value={opt.value}>
                    {opt.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <p className="text-xs text-status-error italic">
              항목을 반드시 선택해야 합니다.
            </p>
          </div>
        </div>
      </section>

      {/* 2. 사이즈 확인 */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2">2. Sizes</h2>
        <div className="flex flex-col gap-6 bg-white p-8 rounded-xl border max-w-md">
          <div className="space-y-1">
            <span className="text-xs text-gray-400 font-mono">sm (h-8)</span>
            <Select>
              <SelectTrigger selectSize="sm">
                <SelectValue placeholder="Small size" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="sm">Small Option</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-1">
            <span className="text-xs text-gray-400 font-mono">
              default (h-10)
            </span>
            <Select>
              <SelectTrigger selectSize="default">
                <SelectValue placeholder="Default size" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="def">Default Option</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="space-y-1">
            <span className="text-xs text-gray-400 font-mono">lg (h-12)</span>
            <Select>
              <SelectTrigger selectSize="lg">
                <SelectValue placeholder="Large size" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="lg">Large Option</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
      </section>

      {/* 3. 비활성화 상태 */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2">3. States</h2>
        <div className="bg-white p-8 rounded-xl border max-w-md">
          <div className="space-y-2">
            <label className="text-sm font-medium opacity-50">
              Disabled Select
            </label>
            <Select disabled>
              <SelectTrigger>
                <SelectValue placeholder="선택할 수 없음" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="none">None</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
      </section>

      {/* 4. 실무 예시 */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2">
          4. Practical Example
        </h2>
        <div className="bg-white p-8 rounded-xl border max-w-sm space-y-4 shadow-sm">
          <div className="space-y-2">
            <label className="text-sm font-bold">국가 선택</label>
            <Select defaultValue="kr">
              <SelectTrigger variant="default" selectSize="lg">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="kr">대한민국 (KR)</SelectItem>
                <SelectItem value="us">미국 (US)</SelectItem>
                <SelectItem value="jp">일본 (JP)</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <button className="w-full bg-black text-white h-12 rounded-md font-bold hover:bg-gray-800 transition-colors">
            저장하기
          </button>
        </div>
      </section>
    </div>
  );
}
