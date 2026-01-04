import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useAuditLogs, useExportAuditLogs } from '@/hooks/useAuditLogs'
import { useAuth } from '@/context/ManualAuthContext'
import { Download, RefreshCw, Search, Filter, AlertCircle, CheckCircle, Info, AlertTriangle } from 'lucide-react'
import { format } from 'date-fns'
import type { AuditLogFilter } from '@/services/auditLogService'

export const AuditPage: React.FC = () => {
  const { user } = useAuth()
  const [page, setPage] = useState(0)
  const [filter, setFilter] = useState<AuditLogFilter>({ page: 0, size: 20 })
  const [searchTerm, setSearchTerm] = useState('')

  const { data, isLoading, refetch } = useAuditLogs(user?.shopId, filter)
  const exportMutation = useExportAuditLogs()

  const handleSearch = () => {
    setFilter({ ...filter, search: searchTerm, page: 0 })
    setPage(0)
  }

  const handleFilterChange = (key: string, value: string) => {
    setFilter({ ...filter, [key]: value || undefined, page: 0 })
    setPage(0)
  }

  const handlePageChange = (newPage: number) => {
    setPage(newPage)
    setFilter({ ...filter, page: newPage })
  }

  const handleExport = () => {
    if (user?.shopId) {
      const { page, size, ...exportFilter } = filter
      exportMutation.mutate({ shopId: user.shopId, filter: exportFilter })
    }
  }

  const getSeverityBadge = (severity: string) => {
    const colors = {
      INFO: 'bg-blue-100 text-blue-800',
      WARNING: 'bg-yellow-100 text-yellow-800',
      ERROR: 'bg-red-100 text-red-800',
      CRITICAL: 'bg-purple-100 text-purple-800'
    }
    const icons = {
      INFO: Info,
      WARNING: AlertTriangle,
      ERROR: AlertCircle,
      CRITICAL: AlertCircle
    }
    const Icon = icons[severity as keyof typeof icons] || Info
    return (
      <Badge className={colors[severity as keyof typeof colors] || colors.INFO}>
        <Icon className="h-3 w-3 mr-1" />
        {severity}
      </Badge>
    )
  }

  const getActionBadge = (actionType: string) => {
    const colors = {
      CREATE: 'bg-green-100 text-green-800',
      UPDATE: 'bg-blue-100 text-blue-800',
      DELETE: 'bg-red-100 text-red-800',
      LOGIN: 'bg-purple-100 text-purple-800',
      LOGOUT: 'bg-gray-100 text-gray-800'
    }
    return <Badge className={colors[actionType as keyof typeof colors] || 'bg-gray-100 text-gray-800'}>{actionType}</Badge>
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Audit Logs</h1>
          <p className="text-gray-600">Security events, entity changes, and system logs</p>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" onClick={() => refetch()} disabled={isLoading}>
            <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
          </Button>
          <Button onClick={handleExport} disabled={exportMutation.isPending}>
            <Download className="h-4 w-4 mr-2" />
            Export CSV
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center">
            <Filter className="h-5 w-5 mr-2" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="flex space-x-2">
              <Input
                placeholder="Search..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              />
              <Button onClick={handleSearch} size="icon">
                <Search className="h-4 w-4" />
              </Button>
            </div>

            <Select value={filter.actionType || ''} onValueChange={(v) => handleFilterChange('actionType', v)}>
              <SelectTrigger>
                <SelectValue placeholder="Action Type" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All Actions</SelectItem>
                <SelectItem value="CREATE">Create</SelectItem>
                <SelectItem value="UPDATE">Update</SelectItem>
                <SelectItem value="DELETE">Delete</SelectItem>
                <SelectItem value="LOGIN">Login</SelectItem>
                <SelectItem value="LOGOUT">Logout</SelectItem>
                <SelectItem value="APPROVE">Approve</SelectItem>
                <SelectItem value="REJECT">Reject</SelectItem>
              </SelectContent>
            </Select>

            <Select value={filter.category || ''} onValueChange={(v) => handleFilterChange('category', v)}>
              <SelectTrigger>
                <SelectValue placeholder="Category" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All Categories</SelectItem>
                <SelectItem value="SECURITY_EVENT">Security Event</SelectItem>
                <SelectItem value="DATA_MODIFICATION">Data Modification</SelectItem>
                <SelectItem value="FINANCIAL_TRANSACTION">Financial Transaction</SelectItem>
                <SelectItem value="SYSTEM_EVENT">System Event</SelectItem>
              </SelectContent>
            </Select>

            <Select value={filter.severity || ''} onValueChange={(v) => handleFilterChange('severity', v)}>
              <SelectTrigger>
                <SelectValue placeholder="Severity" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">All Severities</SelectItem>
                <SelectItem value="INFO">Info</SelectItem>
                <SelectItem value="WARNING">Warning</SelectItem>
                <SelectItem value="ERROR">Error</SelectItem>
                <SelectItem value="CRITICAL">Critical</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Audit Log Table */}
      <Card>
        <CardHeader>
          <CardTitle>Audit Trail</CardTitle>
          <CardDescription>
            {data?.totalElements || 0} total events
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex justify-center items-center h-64">
              <RefreshCw className="h-8 w-8 animate-spin text-gray-400" />
            </div>
          ) : data?.content.length === 0 ? (
            <div className="text-center py-12 text-gray-500">
              No audit logs found
            </div>
          ) : (
            <div className="space-y-4">
              {data?.content.map((log) => (
                <div key={log.id} className="border rounded-lg p-4 hover:bg-gray-50">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center space-x-2 mb-2">
                        {getActionBadge(log.actionType)}
                        {getSeverityBadge(log.severity)}
                        <Badge variant="outline">{log.entityType}</Badge>
                        {log.success ? (
                          <CheckCircle className="h-4 w-4 text-green-500" />
                        ) : (
                          <AlertCircle className="h-4 w-4 text-red-500" />
                        )}
                      </div>
                      <p className="font-medium text-gray-900">{log.actionDescription}</p>
                      <div className="mt-2 text-sm text-gray-600 space-y-1">
                        <p>User: {log.username} ({log.userId})</p>
                        <p>Time: {format(new Date(log.actionDate), 'PPpp')}</p>
                        {log.ipAddress && <p>IP: {log.ipAddress}</p>}
                        {log.errorMessage && (
                          <p className="text-red-600">Error: {log.errorMessage}</p>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {/* Pagination */}
          {data && data.totalPages > 1 && (
            <div className="flex items-center justify-between mt-6">
              <p className="text-sm text-gray-600">
                Page {page + 1} of {data.totalPages}
              </p>
              <div className="flex space-x-2">
                <Button
                  variant="outline"
                  onClick={() => handlePageChange(page - 1)}
                  disabled={page === 0}
                >
                  Previous
                </Button>
                <Button
                  variant="outline"
                  onClick={() => handlePageChange(page + 1)}
                  disabled={page >= data.totalPages - 1}
                >
                  Next
                </Button>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
