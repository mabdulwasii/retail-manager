import React from 'react'
import { cn } from '@/lib/utils'

interface SeparatorProps extends React.HTMLAttributes<HTMLDivElement> {
  orientation?: 'horizontal' | 'vertical'
}

export const Separator: React.FC<SeparatorProps> = ({
  orientation = 'horizontal',
  className,
  ...props
}) => {
  return (
    <div
      className={cn(
        'bg-gray-200',
        orientation === 'horizontal' ? 'h-px w-full' : 'w-px h-full',
        className
      )}
      {...props}
    />
  )
}