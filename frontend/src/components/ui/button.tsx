import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '@/lib/utils';

// 1. CVA로 스타일 정의
export const buttonVariants = cva(
  'inline-flex items-center justify-center w-full text-sm font-medium transition-colors disabled:pointer-events-none ' +
    'focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-status-focus ' +
    'disabled:bg-status-disabled2 disabled:text-status-disabled1',
  {
    variants: {
      variant: {
        primary1:
          'bg-cta-primary1 text-white hover:bg-status-hover1 active:bg-status-press1 active:scale-95' +
          ' disabled:bg-status-disabled1 disabled:text-status-disabled2',
        primary2:
          'bg-cta-primary2 text-black hover:bg-status-hover2 active:bg-status-press2 active:scale-95',
        secondary:
          'bg-cta-secondary text-black hover:bg-status-hover-secondary active:bg-status-press-secondary active:scale-95',
        outline:
          'bg-cta-primary2 text-black hover:bg-status-hover2 active:bg-status-press2 active:scale-95' +
          ' border border-black',
        ghost:
          'bg-transparent text-black hover:bg-status-hover2 active:bg-status-press2 active:scale-95',
        link: 'bg-transparent text-black underline underline-offset-4 hover:text-status-hover1 active:text-status-press1 active:scale-95',
        text: 'bg-transparent text-black hover:text-status-hover1 active:text-status-press1 active:scale-95',
        destructive:
          'bg-cta-destructive text-white hover:bg-status-hover-destructive active:bg-status-press-destructive active:scale-95' +
          ' disabled:bg-status-disabled-destructive disabled:text-black/24',
      },
      size: {
        default: 'h-10 py-2 px-4',
        sm: 'h-9 px-3',
        lg: 'h-12 px-8 text-base',
        icon: 'h-10 w-10 [&_svg]:w-5 [&_svg]:h-5',
        fit: 'h-9 px-4 py-2 text-sm w-fit min-w-max',
      },
      rounded: {
        sm: 'rounded-sm',
        lg: 'rounded-lg',
        full: 'rounded-full',
        none: 'rounded-none',
      },
    },
    defaultVariants: {
      variant: 'primary1',
      size: 'default',
      rounded: 'lg',
    },
  },
);

// 2. Props 타입 정의
export interface ButtonProps
  extends
    React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, rounded, ...props }, ref) => {
    return (
      <button
        ref={ref}
        // 중요: props를 className보다 먼저 쓰거나, disabled 속성이 확실히 전달되게 합니다.
        className={cn(buttonVariants({ variant, size, rounded, className }))}
        {...props}
      />
    );
  },
);

export { Button };
