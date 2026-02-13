import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils';

// 1. CVA를 사용하여 Input 스타일 변종 정의
const inputVariants = cva(
  'flex w-full rounded-md border border-input bg-background px-3 py-2 text-sm ' +
    'focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-status-focus ' +
    '',
  {
    variants: {
      variant: {
        primary: 'border-input ',
        error:
          'focus-visible:ring-1 border-status-error focus-visible:ring-status-error',
      },
      inputSize: {
        default: 'h-10 text-sm',
        sm: 'h-8 px-2 text-xs',
        lg: 'h-12 px-4 text-base',
      },
    },
    defaultVariants: {
      variant: 'primary',
      inputSize: 'default',
    },
  },
);

export interface InputProps
  extends
    Omit<React.InputHTMLAttributes<HTMLInputElement>, 'size'>,
    VariantProps<typeof inputVariants> {
  label?: string;
  helperText?: string;
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, variant, inputSize, label, helperText, ...props }, ref) => {
    return (
      <div className="w-full space-y-1.5">
        {label && (
          <label className="text-sm font-medium leading-none text-gray-700">
            {label}
          </label>
        )}
        <input
          className={cn(inputVariants({ variant, inputSize, className }))}
          ref={ref}
          {...props}
        />
        {helperText && (
          <p
            className={cn(
              'text-xs',
              variant === 'error' ? 'text-status-error' : 'text-gray-500',
            )}
          >
            {helperText}
          </p>
        )}
      </div>
    );
  },
);
Input.displayName = 'Input';

export { Input, inputVariants };
