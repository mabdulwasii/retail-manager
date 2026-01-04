import { useQuery, useMutation } from '@tanstack/react-query'
import { useAuth } from '@/context/ManualAuthContext'
import { Permission } from '@/types/permissions'
import { auditLogService, type AuditLogFilter } from '@/services/auditLogService'
import { toast } from 'sonner'

// Query hook for fetching audit logs with pagination
export const useAuditLogs = (shopId?: string, filter?: AuditLogFilter) => {
  const { isAuthenticated, hasAnyPermission, user } = useAuth()
  const targetShopId = shopId || user?.shopId

  return useQuery({
    queryKey: ['auditLogs', targetShopId, filter],
    queryFn: () => auditLogService.getAuditLogs(targetShopId!, filter),
    enabled: !!(isAuthenticated && targetShopId &&
      hasAnyPermission([Permission.AUDIT_LOG_LIST])),
    staleTime: 30 * 1000, // 30 seconds
    retry: 1
  })
}

// Mutation hook for exporting audit logs to CSV
export const useExportAuditLogs = () => {
  return useMutation({
    mutationFn: ({ shopId, filter }: { shopId: string; filter?: Omit<AuditLogFilter, 'page' | 'size'> }) =>
      auditLogService.exportAuditLogs(shopId, filter),
    onSuccess: (blob) => {
      // Create download link
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `audit-logs-${new Date().toISOString().split('T')[0]}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)

      toast.success('Audit logs exported successfully')
    },
    onError: (error: any) => {
      toast.error('Failed to export audit logs', {
        description: error.response?.data?.message || error.message
      })
    }
  })
}
