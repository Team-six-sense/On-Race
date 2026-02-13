'use client';

import * as React from 'react';
import * as CheckboxPrimitive from '@radix-ui/react-checkbox';
import { Check as CheckIcon } from 'lucide-react';
import { cva, type VariantProps } from 'class-variance-authority';

import { cn } from '@/lib/utils';

// 1. CVA를 사용하여 스타일 변종(Variants) 정의
const checkboxVariants = cva(
  'peer shrink-0 rounded-xs border border-primary ring-offset-background ' +
    'focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-status-focus ' +
    'disabled:data-[state=checked]:bg-status-disabled2 disabled:data-[state=checked]:border-status-disabled2 ' +
    'disabled:data-[state=checked]:text-status-disabled1',
  {
    variants: {
      variant: {
        primary:
          'border-cta-primary1 data-[state=checked]:bg-cta-primary1 data-[state=checked]:text-white ',
        error:
          'border-status-error data-[state=checked]:bg-status-error data-[state=checked]:text-white ',
      },
      size: {
        default: 'h-4 w-4',
        sm: 'h-3 w-3',
        lg: 'h-6 w-6',
      },
    },
    defaultVariants: {
      variant: 'primary',
      size: 'default',
    },
  },
);

// 2. Props 인터페이스 확장
interface CheckboxProps
  extends
    React.ComponentPropsWithoutRef<typeof CheckboxPrimitive.Root>,
    VariantProps<typeof checkboxVariants> {}

const Checkbox = React.forwardRef<
  React.ElementRef<typeof CheckboxPrimitive.Root>,
  CheckboxProps
>(({ className, variant, size, ...props }, ref) => (
  <CheckboxPrimitive.Root
    ref={ref}
    // 3. checkboxVariants 함수를 사용하여 클래스 병합
    className={cn(checkboxVariants({ variant, size, className }))}
    {...props}
  >
    <CheckboxPrimitive.Indicator
      className={cn('flex items-center justify-center text-current')}
    >
      <CheckIcon
        className={cn(
          size === 'lg' ? 'h-4 w-4' : size === 'sm' ? 'h-2 w-2' : 'h-3.5 w-3.5',
        )}
      />
    </CheckboxPrimitive.Indicator>
  </CheckboxPrimitive.Root>
));
Checkbox.displayName = CheckboxPrimitive.Root.displayName;

export { Checkbox };
