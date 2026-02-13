import { Button } from '@/components/ui/button';
import { Mail } from 'lucide-react'; // 아이콘 예시용

export default function ButtonDemo() {
  const variants = [
    'primary1',
    'primary2',
    'secondary',
    'outline',
    'ghost',
    'text',
    'link',
    'destructive',
  ] as const;
  const sizes = ['sm', 'default', 'lg', 'icon'] as const;

  return (
    <div className="p-10 space-y-12 bg-blue-50 min-h-screen">
      <h1 className="text-3xl font-bold mb-8">Button Component System</h1>

      {/* 1. Variants Showcase */}
      <section>
        <h2 className="text-xl font-semibold mb-4 border-b pb-2 text-gray-700">
          1. Variants (Rounded: Full)
        </h2>
        <div className="flex flex-wrap gap-4 items-center bg-white p-6 rounded-xl shadow-sm">
          {variants.map((v) => (
            <div key={v} className="flex flex-col items-center gap-2">
              <Button variant={v}>{v}</Button>
              <span className="text-xs text-gray-400">{v}</span>
            </div>
          ))}
        </div>
      </section>

      {/* 2. Sizes Showcase */}
      <section>
        <h2 className="text-xl font-semibold mb-4 border-b pb-2 text-gray-700">
          2. Sizes (Variant: Primary1)
        </h2>
        <div className="flex flex-wrap gap-6 items-end bg-white p-6 rounded-xl shadow-sm">
          {sizes.map((s) => (
            <div key={s} className="flex flex-col items-center gap-2">
              <Button size={s}>{s === 'icon' ? <Mail /> : `Size ${s}`}</Button>
              <span className="text-xs text-gray-400">{s}</span>
            </div>
          ))}
        </div>
      </section>

      {/* 3. Rounded Options */}
      <section>
        <h2 className="text-xl font-semibold mb-4 border-b pb-2 text-gray-700">
          3. Rounded Options
        </h2>
        <div className="flex gap-4 bg-white p-6 rounded-xl shadow-sm">
          <div className="flex flex-col items-center gap-2">
            <Button rounded="sm">Rounded Sm</Button>
            <span className="text-xs text-gray-400">sm</span>
          </div>
          <div className="flex flex-col items-center gap-2">
            <Button rounded="lg">Rounded Lg</Button>
            <span className="text-xs text-gray-400">lg</span>
          </div>
          <div className="flex flex-col items-center gap-2">
            <Button rounded="full">Rounded Full</Button>
            <span className="text-xs text-gray-400">full</span>
          </div>
          <div className="flex flex-col items-center gap-2">
            <Button rounded="none">Rounded None</Button>
            <span className="text-xs text-gray-400">none</span>
          </div>
        </div>
      </section>

      {/* 4. Interaction States */}
      <section>
        <h2 className="text-xl font-semibold mb-4 border-b pb-2 text-gray-700">
          4. Disabled States
        </h2>
        <div className="flex gap-4 bg-white p-6 rounded-xl shadow-sm">
          <div className="flex flex-col items-center gap-2">
            <Button variant="primary1" disabled>
              Primary1 Disabled
            </Button>
            <span className="text-xs text-gray-400">
              disabled:bg-status-disabled1
            </span>
          </div>
          <div className="flex flex-col items-center gap-2">
            <Button variant="destructive" disabled>
              Destructive Disabled
            </Button>
            <span className="text-xs text-gray-400">
              disabled:text-black/24
            </span>
          </div>
        </div>
      </section>
    </div>
  );
}
