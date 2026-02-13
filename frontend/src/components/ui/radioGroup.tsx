'use client';

import * as React from 'react';
import * as RadioGroupPrimitive from '@radix-ui/react-radio-group';
import { Circle } from 'lucide-react';
import { cva, type VariantProps } from 'class-variance-authority';

import { cn } from '@/lib/utils';

const RadioGroup = React.forwardRef<
  React.ElementRef<typeof RadioGroupPrimitive.Root>,
  React.ComponentPropsWithoutRef<typeof RadioGroupPrimitive.Root>
>(({ className, ...props }, ref) => {
  return (
    <RadioGroupPrimitive.Root
      className={cn('grid gap-2', className)}
      {...props}
      ref={ref}
    />
  );
});
RadioGroup.displayName = RadioGroupPrimitive.Root.displayName;

// 1. RadioGroupItem을 위한 CVA 정의
const radioItemVariants = cva(
  'aspect-square rounded-full border border-primary text-primary ' +
    'focus:outline-none focus-visible:ring-2 focus-visible:ring-status-focus ' +
    'disabled:border-status-disabled1 disabled:text-status-disabled1',
  {
    variants: {
      variant: {
        primary: 'border-gray-300 text-black',
        error: 'border-status-error text-status-error',
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

interface RadioGroupItemProps
  extends
    React.ComponentPropsWithoutRef<typeof RadioGroupPrimitive.Item>,
    VariantProps<typeof radioItemVariants> {
  isError?: boolean;
}

const RadioGroupItem = React.forwardRef<
  React.ElementRef<typeof RadioGroupPrimitive.Item>,
  RadioGroupItemProps
>(({ className, variant, size, isError, ...props }, ref) => {
  return (
    <RadioGroupPrimitive.Item
      ref={ref}
      className={cn(
        radioItemVariants({
          variant: isError ? 'error' : variant,
          size,
          className,
        }),
      )}
      {...props}
    >
      <RadioGroupPrimitive.Indicator className="flex items-center justify-center">
        {/* 선택되었을 때 나타나는 내부 점의 크기도 size에 맞춰 조절 */}
        <Circle
          className={cn(
            'fill-current',
            size === 'lg'
              ? 'h-3 w-3'
              : size === 'sm'
                ? 'h-1.5 w-1.5'
                : 'h-2.5 w-2.5',
          )}
        />
      </RadioGroupPrimitive.Indicator>
    </RadioGroupPrimitive.Item>
  );
});
RadioGroupItem.displayName = RadioGroupPrimitive.Item.displayName;

export { RadioGroup, RadioGroupItem };
