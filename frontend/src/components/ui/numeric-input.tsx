import * as React from "react"
import { NumericFormat, NumberFormatValues, OnValueChange } from 'react-number-format'
import { cn } from "@/lib/utils"

export interface NumericInputProps extends Omit<React.InputHTMLAttributes<HTMLInputElement>, 'onChange' | 'value'> {
  value?: number | string | null
  onValueChange?: OnValueChange
  isAllowed?: (values: NumberFormatValues) => boolean
  thousandSeparator?: boolean | string
  decimalSeparator?: string
  allowedDecimalSeparators?: Array<string>
  thousandsGroupStyle?: 'thousand' | 'lakh' | 'wan' | 'none'
  decimalScale?: number
  fixedDecimalScale?: boolean
  allowNegative?: boolean
  allowLeadingZeros?: boolean
  suffix?: string
  prefix?: string
  isNumberInput?: boolean
  type?: 'text' | 'tel' | 'password';
}

const NumericInput = React.forwardRef<HTMLInputElement, NumericInputProps>(
  ({ 
    className, 
    type = 'text',
    value,
    prefix = '₦',
    thousandsGroupStyle = 'thousand',
    thousandSeparator = ',',
    allowNegative = false,
    allowLeadingZeros = false,
    decimalScale,
    fixedDecimalScale,
    isNumberInput = false,
    onValueChange,
    isAllowed,
    disabled,
    placeholder,
    decimalSeparator,
    allowedDecimalSeparators,
    suffix,
    ...props 
  }, ref) => {
    // Filter out props that are incompatible with NumericFormat
    const { 
      defaultValue,
      ...compatibleProps 
    } = props

    return (
      <NumericFormat
        getInputRef={ref}
        type={type}
        prefix={isNumberInput ? undefined : prefix}
        suffix={suffix}
        thousandsGroupStyle={isNumberInput ? undefined : thousandsGroupStyle}
        thousandSeparator={isNumberInput ? undefined : thousandSeparator}
        decimalSeparator={decimalSeparator}
        allowedDecimalSeparators={allowedDecimalSeparators}
        value={value ?? ''}
        className={cn(
          "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50",
          className
        )}
        placeholder={placeholder}
        allowNegative={allowNegative}
        {...(onValueChange && { onValueChange })}
        {...(isAllowed && { isAllowed })}
        allowLeadingZeros={allowLeadingZeros}
        disabled={disabled}
        decimalScale={decimalScale}
        fixedDecimalScale={fixedDecimalScale}
        {...compatibleProps}
      />
    )
  }
)

NumericInput.displayName = "NumericInput"

export { NumericInput }
