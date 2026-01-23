/**
 * Chart utility functions shared across all chart components
 */

/**
 * Normalizes a value that can be a number, string, or array to a number
 * @param value - The value to normalize
 * @returns The normalized number value
 */
export const normalizeValue = (value: string | number | (string | number)[]): number => {
  if (typeof value === 'number') {
    return value
  }
  if (Array.isArray(value)) {
    return Number(value[0])
  }
  return Number.parseFloat(String(value))
}
