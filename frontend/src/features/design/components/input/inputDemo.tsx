import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

export default function InputDemo() {
  return (
    <div className="p-10 space-y-12 bg-blue-50 min-h-screen">
      <h1 className="text-3xl font-bold mb-8 text-gray-800">
        Input Component System
      </h1>

      {/* 1. Variants Showcase */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          1. Variants
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <div className="space-y-4">
            <h3 className="text-sm font-bold text-blue-600 uppercase tracking-wider">
              Primary (Default)
            </h3>
            <Input
              variant="primary"
              placeholder="이메일을 입력하세요"
              label="이메일"
              helperText="회사 이메일 주소를 입력해 주세요."
            />
          </div>
          <div className="space-y-4">
            <h3 className="text-sm font-bold text-red-600 uppercase tracking-wider">
              Error State
            </h3>
            <Input
              variant="error"
              placeholder="잘못된 형식입니다"
              label="이메일"
              defaultValue="invalid-email@"
              helperText="유효한 이메일 형식이 아닙니다."
            />
          </div>
        </div>
      </section>

      {/* 2. Sizes Showcase */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          2. Sizes
        </h2>
        <div className="flex flex-col gap-6 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <div className="flex items-end gap-4">
            <div className="flex-1 max-w-[200px]">
              <Input
                inputSize="sm"
                label="Small (h-8)"
                placeholder="Small size..."
              />
            </div>
            <div className="flex-1 max-w-[200px]">
              <Input
                inputSize="default"
                label="Default (h-10)"
                placeholder="Default size..."
              />
            </div>
            <div className="flex-1 max-w-[200px]">
              <Input
                inputSize="lg"
                label="Large (h-12)"
                placeholder="Large size..."
              />
            </div>
          </div>
        </div>
      </section>

      {/* 3. Interaction States */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          3. States
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <Input label="Disabled" disabled placeholder="입력할 수 없습니다" />
          <Input label="Read Only" readOnly value="수정할 수 없는 값" />
          <Input label="Password" type="password" defaultValue="password123" />
        </div>
      </section>

      {/* 4. Real-world Example (Login Form) */}
      <section className="space-y-4">
        <h2 className="text-xl font-semibold border-b pb-2 text-gray-700">
          4. Form Example
        </h2>
        <div className="max-w-md mx-auto bg-white p-8 rounded-2xl shadow-lg border border-gray-100 space-y-6">
          <h3 className="text-2xl font-bold text-center">로그인</h3>
          <div className="space-y-4">
            <Input label="아이디" placeholder="ID를 입력하세요" />
            <Input
              label="비밀번호"
              type="password"
              variant="error"
              helperText="비밀번호가 일치하지 않습니다."
            />
          </div>
          <Button className="w-full">로그인하기</Button>
        </div>
      </section>
    </div>
  );
}
