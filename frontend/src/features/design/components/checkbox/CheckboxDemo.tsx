import { Checkbox } from '@/components/ui/checkbox';

export default function CheckboxDemo() {
  return (
    <div className="p-10 space-y-12 bg-blue-50 min-h-screen">
      <h1 className="text-3xl font-bold mb-8 text-gray-800">
        Checkbox Component System
      </h1>

      {/* 1. Variants Showcase */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          1. Variants (Checked State)
        </h2>
        <div className="flex gap-10 bg-white p-8 rounded-xl shadow-sm border border-gray-100">
          <div className="flex items-center space-x-2">
            <Checkbox id="p1" variant="primary" defaultChecked />
            <label htmlFor="p1" className="text-sm font-medium">
              Primary Variant
            </label>
          </div>
          <div className="flex items-center space-x-2">
            <Checkbox id="e1" variant="error" defaultChecked />
            <label
              htmlFor="e1"
              className="text-sm font-medium text-status-error"
            >
              Error Variant
            </label>
          </div>
        </div>
      </section>

      {/* 2. Sizes Showcase */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          2. Sizes
        </h2>
        <div className="flex items-end gap-10 bg-white p-8 rounded-xl shadow-sm border border-gray-100">
          <div className="flex flex-col items-center gap-2">
            <Checkbox size="sm" defaultChecked />
            <span className="text-xs text-gray-400">sm (3x3)</span>
          </div>
          <div className="flex flex-col items-center gap-2">
            <Checkbox size="default" defaultChecked />
            <span className="text-xs text-gray-400">default (4x4)</span>
          </div>
          <div className="flex flex-col items-center gap-2">
            <Checkbox size="lg" defaultChecked />
            <span className="text-xs text-gray-400">lg (6x6)</span>
          </div>
        </div>
      </section>

      {/* 3. States Showcase */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          3. Interaction States
        </h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-6 bg-white p-8 rounded-xl shadow-sm border border-gray-100">
          <div className="space-y-2">
            <p className="text-xs font-bold text-gray-400 uppercase">
              Unchecked
            </p>
            <Checkbox />
          </div>
          <div className="space-y-2">
            <p className="text-xs font-bold text-gray-400 uppercase">Checked</p>
            <Checkbox defaultChecked />
          </div>
          <div className="space-y-2">
            <p className="text-xs font-bold text-gray-400 uppercase">
              Disabled Off
            </p>
            <Checkbox disabled />
          </div>
          <div className="space-y-2">
            <p className="text-xs font-bold text-gray-400 uppercase">
              Disabled On
            </p>
            <Checkbox disabled defaultChecked />
          </div>
        </div>
      </section>

      {/* 4. Real-world Example (Terms of Service) */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          4. Form Example
        </h2>
        <div className="max-w-sm bg-white p-6 rounded-xl shadow-md border border-gray-100 space-y-4">
          <h3 className="font-bold text-lg">약관 동의</h3>
          <div className="space-y-3">
            <div className="flex items-center space-x-3 p-2 rounded-lg hover:bg-gray-50 transition-colors">
              <Checkbox id="all" size="lg" />
              <label htmlFor="all" className="text-base font-bold">
                전체 동의하기
              </label>
            </div>
            <div className="h-[1px] bg-gray-100 w-full" />
            <div className="flex items-center space-x-3 px-2">
              <Checkbox id="terms" size="default" />
              <label htmlFor="terms" className="text-sm">
                이용약관 동의 (필수)
              </label>
            </div>
            <div className="flex items-center space-x-3 px-2">
              <Checkbox id="marketing" size="default" variant="primary" />
              <label htmlFor="marketing" className="text-sm">
                마케팅 정보 수신 동의 (선택)
              </label>
            </div>
            <div className="flex items-center space-x-3 px-2">
              <Checkbox id="privacy" size="default" variant="error" />
              <label
                htmlFor="privacy"
                className="text-sm text-status-error font-medium"
              >
                개인정보 처리방침 (미동의 시 가입 불가)
              </label>
            </div>
          </div>
        </div>
      </section>
    </div>
  );
}
