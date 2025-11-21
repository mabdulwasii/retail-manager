import React, { createContext, useContext, useState, useRef, useEffect, useCallback, useLayoutEffect } from 'react'
import { cn } from '@/lib/utils'

interface SelectContextType {
  value?: string | undefined
  onValueChange?: ((value: string) => void) | undefined
  open: boolean
  onOpenChange: (open: boolean) => void
  items: Map<string, React.ReactNode>
  registerItem: (value: string, label: React.ReactNode) => void
  unregisterItem: (value: string) => void
}

const SelectContext = createContext<SelectContextType | undefined>(undefined)

interface SelectProps {
  children: React.ReactNode
  value?: string | undefined
  onValueChange?: (value: string) => void
  disabled?: boolean
}

export const Select: React.FC<SelectProps> = ({
  children,
  value,
  onValueChange,
  disabled
}) => {
  const [open, setOpen] = useState(false)
  const itemsRef = useRef<Map<string, React.ReactNode>>(new Map())
  const containerRef = useRef<HTMLDivElement>(null)
  const [, forceUpdate] = useState({})

  // Close on outside click
  useEffect(() => {
    if (!open) return

    const handleClickOutside = (event: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setOpen(false)
      }
    }

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setOpen(false)
      }
    }

    document.addEventListener('mousedown', handleClickOutside)
    document.addEventListener('keydown', handleEscape)

    return () => {
      document.removeEventListener('mousedown', handleClickOutside)
      document.removeEventListener('keydown', handleEscape)
    }
  }, [open])

  // Stable callbacks using useCallback
  const registerItem = useCallback((itemValue: string, label: React.ReactNode) => {
    itemsRef.current.set(itemValue, label)
    forceUpdate({}) // Force re-render to show updated value
  }, [])

  const unregisterItem = useCallback((itemValue: string) => {
    itemsRef.current.delete(itemValue)
  }, [])

  return (
    <SelectContext.Provider
      value={{
        value,
        onValueChange,
        open: disabled ? false : open,
        onOpenChange: disabled ? () => {} : setOpen,
        items: itemsRef.current,
        registerItem,
        unregisterItem
      }}
    >
      <div ref={containerRef} className="relative">{children}</div>
    </SelectContext.Provider>
  )
}

interface SelectTriggerProps {
  children: React.ReactNode
  className?: string
}

export const SelectTrigger: React.FC<SelectTriggerProps> = ({ children, className }) => {
  const context = useContext(SelectContext)
  if (!context) throw new Error('SelectTrigger must be used within Select')

  return (
    <button
      className={cn(
        'flex h-10 w-full items-center justify-between rounded-md border border-gray-300 bg-white px-3 py-2 text-sm ring-offset-white placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50',
        className
      )}
      onClick={() => context.onOpenChange(!context.open)}
    >
      {children}
    </button>
  )
}

interface SelectValueProps {
  placeholder?: string
  className?: string
}

export const SelectValue: React.FC<SelectValueProps> = ({ placeholder, className }) => {
  const context = useContext(SelectContext)
  if (!context) throw new Error('SelectValue must be used within Select')

  // Get display text from items map
  let displayText: React.ReactNode = placeholder
  
  if (context.value) {
    // If we have a value and it's in the items map, show the label
    if (context.items.has(context.value)) {
      displayText = context.items.get(context.value)
    } else {
      // Value exists but not yet registered (initial render), show placeholder
      // The item will register soon via useEffect
      displayText = placeholder
    }
  }

  return (
    <span className={cn('block truncate', className)}>
      {displayText}
    </span>
  )
}

interface SelectContentProps {
  children: React.ReactNode
  className?: string
}

export const SelectContent: React.FC<SelectContentProps> = ({ children, className }) => {
  const context = useContext(SelectContext)
  if (!context) throw new Error('SelectContent must be used within Select')

  // Always render children (so SelectItems can register) but hide the dropdown
  return (
    <div
      className={cn(
        'absolute top-full left-0 z-50 w-full rounded-md border border-gray-300 bg-white shadow-lg',
        !context.open && 'hidden', // Hide with CSS instead of not rendering
        className
      )}
    >
      {children}
    </div>
  )
}

interface SelectItemProps {
  children: React.ReactNode
  value: string
  className?: string
}

export const SelectItem: React.FC<SelectItemProps> = ({ children, value, className }) => {
  const context = useContext(SelectContext)
  if (!context) throw new Error('SelectItem must be used within Select')

  // Register this item in the items map synchronously using useLayoutEffect
  useLayoutEffect(() => {
    context.registerItem(value, children)
    return () => {
      context.unregisterItem(value)
    }
  }, [value, children, context.registerItem, context.unregisterItem])

  const isSelected = context.value === value

  return (
    <div
      className={cn(
        'relative flex w-full cursor-pointer select-none items-center rounded-sm py-1.5 px-2 text-sm outline-none hover:bg-gray-100 focus:bg-gray-100',
        isSelected && 'bg-blue-100 text-blue-600',
        className
      )}
      onClick={() => {
        context.onValueChange?.(value)
        context.onOpenChange(false)
      }}
    >
      {children}
    </div>
  )
}