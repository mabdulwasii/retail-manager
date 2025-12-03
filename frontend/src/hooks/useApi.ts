import { useState, useEffect, useCallback } from 'react'
import { AxiosError } from 'axios'

interface ApiState<T> {
  data: T | null
  loading: boolean
  error: string | null
}

interface UseApiOptions {
  immediate?: boolean
  deps?: React.DependencyList
}

export function useApi<T>(
  apiCall: () => Promise<T>,
  options: UseApiOptions = { immediate: true }
): [ApiState<T>, () => Promise<void>] {
  const [state, setState] = useState<ApiState<T>>({
    data: null,
    loading: false,
    error: null,
  })

  const execute = useCallback(async () => {
    setState(prev => ({ ...prev, loading: true, error: null }))

    try {
      const result = await apiCall()
      setState({ data: result, loading: false, error: null })
    } catch (error) {
      const isAxiosError = (error as any)?.isAxiosError === true
      const errorMessage = isAxiosError
        ? (error as AxiosError).response?.data?.message || (error as AxiosError).message
        : 'An unexpected error occurred'

      setState(prev => ({ ...prev, loading: false, error: errorMessage }))
    }
  }, [apiCall])

  useEffect(() => {
    if (options.immediate) {
      execute()
    }
  }, options.deps || [execute])

  return [state, execute]
}

export function useMutation<T, P = unknown>(
  mutationFn: (params: P) => Promise<T>
): {
  mutate: (params: P) => Promise<T | null>
  loading: boolean
  error: string | null
  reset: () => void
} {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const mutate = useCallback(async (params: P): Promise<T | null> => {
    setLoading(true)
    setError(null)

    try {
      const result = await mutationFn(params)
      setLoading(false)
      return result
    } catch (error) {
      const isAxiosError = (error as any)?.isAxiosError === true
      const errorMessage = isAxiosError
        ? (error as AxiosError).response?.data?.message || (error as AxiosError).message
        : 'An unexpected error occurred'

      setError(errorMessage)
      setLoading(false)
      return null
    }
  }, [mutationFn])

  const reset = useCallback(() => {
    setLoading(false)
    setError(null)
  }, [])

  return { mutate, loading, error, reset }
}