import * as React from "react"
import { cn } from "@/lib/utils"

interface RadioGroupProps extends React.HTMLAttributes<HTMLDivElement> {
  value?: string
  onValueChange?: (value: string) => void
  defaultValue?: string
  name?: string
}

const RadioGroup = React.forwardRef<HTMLDivElement, RadioGroupProps>(
  ({ className, value, onValueChange, defaultValue, children, name, ...props }, ref) => {
    const [internalValue, setInternalValue] = React.useState(defaultValue || "")
    const currentValue = value !== undefined ? value : internalValue

    const handleChange = (newValue: string) => {
      if (value === undefined) {
        setInternalValue(newValue)
      }
      onValueChange?.(newValue)
    }

    return (
      <div
        ref={ref}
        className={cn("grid gap-2", className)}
        role="radiogroup"
        {...props}
      >
        {React.Children.map(children, (child) => {
          if (React.isValidElement(child)) {
            return React.cloneElement(child as React.ReactElement<any>, {
              name,
              checked: child.props.value === currentValue,
              onCheckedChange: () => handleChange(child.props.value),
            })
          }
          return child
        })}
      </div>
    )
  }
)
RadioGroup.displayName = "RadioGroup"

interface RadioGroupItemProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'type'> {
  value: string
  checked?: boolean
  onCheckedChange?: () => void
}

const RadioGroupItem = React.forwardRef<HTMLInputElement, RadioGroupItemProps>(
  ({ className, value, checked, onCheckedChange, ...props }, ref) => {
    return (
      <div className="relative flex items-center">
        <input
          type="radio"
          ref={ref}
          value={value}
          checked={checked}
          onChange={onCheckedChange}
          className={cn(
            "h-4 w-4 rounded-full border border-primary text-primary",
            "focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
            "disabled:cursor-not-allowed disabled:opacity-50",
            "cursor-pointer",
            className
          )}
          {...props}
        />
        {checked && (
          <div className="absolute left-0 top-0 h-4 w-4 flex items-center justify-center pointer-events-none">
            <div className="h-2 w-2 rounded-full bg-primary" />
          </div>
        )}
      </div>
    )
  }
)
RadioGroupItem.displayName = "RadioGroupItem"

export { RadioGroup, RadioGroupItem }
