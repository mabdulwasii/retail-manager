import { renderHook, waitFor } from '@testing-library/react'
import { AxiosError } from 'axios'
import { useApi, useMutation } from '../useApi'

// Mock axios error
const createAxiosError = (message: string, status = 500): AxiosError => {
  const error = new Error(message) as AxiosError
  error.response = {
    data: { message },
    status,
    statusText: 'Internal Server Error',
    headers: {},
    config: {} as any,
  }
  error.isAxiosError = true
  return error
}

describe('useApi', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should initialize with correct default state', () => {
    const mockApiCall = jest.fn()
    const { result } = renderHook(() => useApi(mockApiCall, { immediate: false }))

    expect(result.current[0].data).toBeNull()
    expect(result.current[0].loading).toBe(false)
    expect(result.current[0].error).toBeNull()
  })

  it('should execute API call immediately by default', async () => {
    const mockData = { id: 1, name: 'Test' }
    const mockApiCall = jest.fn().mockResolvedValue(mockData)

    const { result } = renderHook(() => useApi(mockApiCall))

    expect(result.current[0].loading).toBe(true)
    expect(mockApiCall).toHaveBeenCalledTimes(1)

    await waitFor(() => {
      expect(result.current[0].loading).toBe(false)
      expect(result.current[0].data).toEqual(mockData)
      expect(result.current[0].error).toBeNull()
    })
  })

  it('should not execute immediately when immediate is false', () => {
    const mockApiCall = jest.fn()
    renderHook(() => useApi(mockApiCall, { immediate: false }))

    expect(mockApiCall).not.toHaveBeenCalled()
  })

  it('should handle API call success', async () => {
    const mockData = { users: [{ id: 1, name: 'John' }] }
    const mockApiCall = jest.fn().mockResolvedValue(mockData)

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: false }))

    const [, execute] = result.current
    execute()

    expect(result.current[0].loading).toBe(true)

    await waitFor(() => {
      expect(result.current[0].loading).toBe(false)
      expect(result.current[0].data).toEqual(mockData)
      expect(result.current[0].error).toBeNull()
    })
  })

  it('should handle API call error with AxiosError', async () => {
    const errorMessage = 'Network Error'
    const axiosError = createAxiosError(errorMessage, 404)
    const mockApiCall = jest.fn().mockRejectedValue(axiosError)

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: false }))

    const [, execute] = result.current
    execute()

    await waitFor(() => {
      expect(result.current[0].loading).toBe(false)
      expect(result.current[0].data).toBeNull()
      expect(result.current[0].error).toBe(errorMessage)
    })
  })

  it('should handle API call error with generic Error', async () => {
    const errorMessage = 'Generic error'
    const genericError = new Error(errorMessage)
    const mockApiCall = jest.fn().mockRejectedValue(genericError)

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: false }))

    const [, execute] = result.current
    execute()

    await waitFor(() => {
      expect(result.current[0].loading).toBe(false)
      expect(result.current[0].data).toBeNull()
      expect(result.current[0].error).toBe('An unexpected error occurred')
    })
  })

  it('should handle non-Error objects', async () => {
    const mockApiCall = jest.fn().mockRejectedValue('String error')

    const { result } = renderHook(() => useApi(mockApiCall, { immediate: false }))

    const [, execute] = result.current
    execute()

    await waitFor(() => {
      expect(result.current[0].loading).toBe(false)
      expect(result.current[0].error).toBe('An unexpected error occurred')
    })
  })

  it('should re-execute when dependencies change', async () => {
    const mockApiCall = jest.fn().mockResolvedValue({ data: 'test' })
    let dependency = 'initial'

    const { rerender } = renderHook(
      ({ dep }) => useApi(mockApiCall, { deps: [dep] }),
      { initialProps: { dep: dependency } }
    )

    expect(mockApiCall).toHaveBeenCalledTimes(1)

    dependency = 'changed'
    rerender({ dep: dependency })

    await waitFor(() => {
      expect(mockApiCall).toHaveBeenCalledTimes(2)
    })
  })
})

describe('useMutation', () => {
  beforeEach(() => {
    jest.clearAllMocks()
  })

  it('should initialize with correct default state', () => {
    const mockMutation = jest.fn()
    const { result } = renderHook(() => useMutation(mockMutation))

    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
    expect(typeof result.current.mutate).toBe('function')
    expect(typeof result.current.reset).toBe('function')
  })

  it('should handle successful mutation', async () => {
    const mockResult = { id: 1, created: true }
    const mockMutation = jest.fn().mockResolvedValue(mockResult)
    const params = { name: 'Test User' }

    const { result } = renderHook(() => useMutation(mockMutation))

    const mutationResult = result.current.mutate(params)

    expect(result.current.loading).toBe(true)
    expect(mockMutation).toHaveBeenCalledWith(params)

    const resolvedResult = await mutationResult

    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
    expect(resolvedResult).toEqual(mockResult)
  })

  it('should handle mutation error with AxiosError', async () => {
    const errorMessage = 'Validation failed'
    const axiosError = createAxiosError(errorMessage, 400)
    const mockMutation = jest.fn().mockRejectedValue(axiosError)

    const { result } = renderHook(() => useMutation(mockMutation))

    const mutationResult = result.current.mutate({ invalid: 'data' })
    const resolvedResult = await mutationResult

    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBe(errorMessage)
    expect(resolvedResult).toBeNull()
  })

  it('should handle mutation error with generic Error', async () => {
    const errorMessage = 'Database connection failed'
    const genericError = new Error(errorMessage)
    const mockMutation = jest.fn().mockRejectedValue(genericError)

    const { result } = renderHook(() => useMutation(mockMutation))

    const mutationResult = result.current.mutate({})
    await mutationResult

    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBe('An unexpected error occurred')
  })

  it('should reset state correctly', async () => {
    const axiosError = createAxiosError('Error message')
    const mockMutation = jest.fn().mockRejectedValue(axiosError)

    const { result } = renderHook(() => useMutation(mockMutation))

    // Trigger an error
    await result.current.mutate({})
    expect(result.current.error).toBeTruthy()

    // Reset state
    result.current.reset()

    expect(result.current.loading).toBe(false)
    expect(result.current.error).toBeNull()
  })

  it('should handle multiple concurrent mutations', async () => {
    const mockMutation = jest.fn()
      .mockResolvedValueOnce({ id: 1 })
      .mockResolvedValueOnce({ id: 2 })

    const { result } = renderHook(() => useMutation(mockMutation))

    const promise1 = result.current.mutate({ name: 'User 1' })
    const promise2 = result.current.mutate({ name: 'User 2' })

    const [result1, result2] = await Promise.all([promise1, promise2])

    expect(result1).toEqual({ id: 1 })
    expect(result2).toEqual({ id: 2 })
    expect(mockMutation).toHaveBeenCalledTimes(2)
  })
})