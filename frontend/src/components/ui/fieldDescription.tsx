import * as React from 'react';
import { cn } from '@/lib/utils';

const FieldDescription = React.forwardRef<
  HTMLParagraphElement,
  React.HTMLAttributes<HTMLParagraphElement>
>(({ className, ...props }, ref) => {
  return (
    <p
      ref={ref}
      className={cn(
        'text-[0.8rem] font-medium text-muted-foreground',
        className,
      )}
      {...props}
    />
  );
});
FieldDescription.displayName = 'FieldDescription';

export { FieldDescription };
