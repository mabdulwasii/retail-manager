import React from 'react'
import { cn } from '@/lib/utils'

interface SwitchProps extends React.InputHTMLAttributes<HTMLInputElement> {
  className?: string
}

export const Switch: React.FC<SwitchProps> = ({
  className,
  checked,
  onChange,
  disabled,
  ...props
}) => {
  return (
    <label
      className={cn(
        'relative inline-flex cursor-pointer items-center',
        disabled && 'cursor-not-allowed opacity-50',
        className
      )}
    >
      <input
        type="checkbox"
        className="sr-only"
        checked={checked}
        onChange={onChange}
        disabled={disabled}
        {...props}
      />
      <div
        className={cn(
          'relative h-6 w-11 rounded-full transition-colors',
          checked
            ? 'bg-blue-500'
            : 'bg-gray-200',
          disabled && 'cursor-not-allowed'
        )}
      >
        <div
          className={cn(
            'absolute top-0.5 left-0.5 h-5 w-5 rounded-full bg-white transition-transform',
            checked && 'translate-x-5'
          )}
        />
      </div>
    </label>
  )
}