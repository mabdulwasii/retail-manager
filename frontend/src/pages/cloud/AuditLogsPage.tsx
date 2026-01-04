import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  FileText,
  Search,
  Download,
  Eye,
  Filter,
  Calendar,
  User,
  Activity,
  Shield,
  Database,
  Settings,
  Loader,
  Building2,
  Store,
  CreditCard,
} from 'lucide-react';
import { format } from 'date-fns';

/**
 * Audit Logs Page
 * View system audit logs with filtering and search
 *
 * Features:
 * - List all audit logs with pagination
 * - Filter by date range, user, action type, entity
 * - Search functionality
 * - Export audit logs to CSV
 * - Action type badges with color coding
 * - Detailed log information
 */

enum AuditAction {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  LOGIN = 'LOGIN',
  LOGOUT = 'LOGOUT',
  ACCESS = 'ACCESS',
  SYNC = 'SYNC',
  EXPORT = 'EXPORT',
}

enum EntityType {
  TENANT = 'TENANT',
  SHOP = 'SHOP',
  USER = 'USER',
  API_KEY = 'API_KEY',
  SUBSCRIPTION = 'SUBSCRIPTION',
  INVOICE = 'INVOICE',
  SYNC_LOG = 'SYNC_LOG',
}

interface AuditLog {
  id: string;
  timestamp: string;
  action: AuditAction;
  entityType: EntityType;
  entityId: string;
  entityName?: string;
  userId: string;
  userName: string;
  userEmail: string;
  ipAddress: string;
  details?: string;
  metadata?: Record<string, any>;
}

export const AuditLogsPage: React.FC = () => {
  // TODO: Get actual tenant ID from auth context
  const tenantId = 'demo-tenant-id';

  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [filteredLogs, setFilteredLogs] = useState<AuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isExporting, setIsExporting] = useState(false);

  // Filters
  const [searchQuery, setSearchQuery] = useState('');
  const [actionFilter, setActionFilter] = useState<string>('ALL');
  const [entityTypeFilter, setEntityTypeFilter] = useState<string>('ALL');
  const [dateRange, setDateRange] = useState<string>('7_DAYS');

  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const logsPerPage = 20;

  useEffect(() => {
    loadAuditLogs();
  }, [tenantId, dateRange]);

  useEffect(() => {
    filterLogs();
  }, [logs, searchQuery, actionFilter, entityTypeFilter]);

  const loadAuditLogs = async () => {
    try {
      setIsLoading(true);

      // TODO: Replace with actual API call
      // const logsData = await auditService.getLogs(tenantId, { dateRange });

      // Mock data for now
      const mockLogs: AuditLog[] = [
        {
          id: '1',
          timestamp: '2026-01-04T10:30:00Z',
          action: AuditAction.CREATE,
          entityType: EntityType.SHOP,
          entityId: 'shop-123',
          entityName: 'Downtown Store',
          userId: 'user-1',
          userName: 'John Admin',
          userEmail: 'john@demoretail.com',
          ipAddress: '192.168.1.100',
          details: 'Created new shop location',
        },
        {
          id: '2',
          timestamp: '2026-01-04T09:15:00Z',
          action: AuditAction.UPDATE,
          entityType: EntityType.TENANT,
          entityId: tenantId,
          entityName: 'Demo Retail Company',
          userId: 'user-1',
          userName: 'John Admin',
          userEmail: 'john@demoretail.com',
          ipAddress: '192.168.1.100',
          details: 'Updated tenant settings',
        },
        {
          id: '3',
          timestamp: '2026-01-03T16:45:00Z',
          action: AuditAction.CREATE,
          entityType: EntityType.API_KEY,
          entityId: 'key-456',
          entityName: 'Production API Key',
          userId: 'user-2',
          userName: 'Sarah Manager',
          userEmail: 'sarah@demoretail.com',
          ipAddress: '192.168.1.101',
          details: 'Generated new API key with READ, WRITE permissions',
        },
        {
          id: '4',
          timestamp: '2026-01-03T14:20:00Z',
          action: AuditAction.LOGIN,
          entityType: EntityType.USER,
          entityId: 'user-1',
          entityName: 'John Admin',
          userId: 'user-1',
          userName: 'John Admin',
          userEmail: 'john@demoretail.com',
          ipAddress: '192.168.1.100',
          details: 'Successful login',
        },
        {
          id: '5',
          timestamp: '2026-01-02T11:30:00Z',
          action: AuditAction.UPDATE,
          entityType: EntityType.SUBSCRIPTION,
          entityId: 'sub-789',
          entityName: 'PREMIUM Subscription',
          userId: 'user-1',
          userName: 'John Admin',
          userEmail: 'john@demoretail.com',
          ipAddress: '192.168.1.100',
          details: 'Upgraded subscription tier from BASIC to PREMIUM',
        },
        {
          id: '6',
          timestamp: '2026-01-02T09:00:00Z',
          action: AuditAction.SYNC,
          entityType: EntityType.SHOP,
          entityId: 'shop-123',
          entityName: 'Downtown Store',
          userId: 'system',
          userName: 'System',
          userEmail: 'system@internal',
          ipAddress: '10.0.0.1',
          details: 'Synchronized shop data',
        },
        {
          id: '7',
          timestamp: '2026-01-01T15:10:00Z',
          action: AuditAction.DELETE,
          entityType: EntityType.API_KEY,
          entityId: 'key-old',
          entityName: 'Old Development Key',
          userId: 'user-2',
          userName: 'Sarah Manager',
          userEmail: 'sarah@demoretail.com',
          ipAddress: '192.168.1.101',
          details: 'Revoked expired API key',
        },
        {
          id: '8',
          timestamp: '2025-12-31T13:40:00Z',
          action: AuditAction.EXPORT,
          entityType: EntityType.INVOICE,
          entityId: 'inv-202512',
          entityName: 'December Invoice',
          userId: 'user-1',
          userName: 'John Admin',
          userEmail: 'john@demoretail.com',
          ipAddress: '192.168.1.100',
          details: 'Exported billing invoice',
        },
      ];

      setLogs(mockLogs);
    } catch (err) {
      console.error('Failed to load audit logs:', err);
    } finally {
      setIsLoading(false);
    }
  };

  const filterLogs = () => {
    let filtered = [...logs];

    // Search filter
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      filtered = filtered.filter(
        (log) =>
          log.userName.toLowerCase().includes(query) ||
          log.userEmail.toLowerCase().includes(query) ||
          log.entityName?.toLowerCase().includes(query) ||
          log.details?.toLowerCase().includes(query) ||
          log.ipAddress.includes(query)
      );
    }

    // Action filter
    if (actionFilter !== 'ALL') {
      filtered = filtered.filter((log) => log.action === actionFilter);
    }

    // Entity type filter
    if (entityTypeFilter !== 'ALL') {
      filtered = filtered.filter((log) => log.entityType === entityTypeFilter);
    }

    setFilteredLogs(filtered);
    setCurrentPage(1); // Reset to first page when filters change
  };

  const handleExport = async () => {
    setIsExporting(true);
    try {
      // TODO: Replace with actual export API call
      // const csvData = await auditService.exportLogs(tenantId, filters);

      // Simulate export
      await new Promise((resolve) => setTimeout(resolve, 1000));

      // Create CSV mock
      const headers = ['Timestamp', 'Action', 'Entity Type', 'Entity Name', 'User', 'IP Address', 'Details'];
      const rows = filteredLogs.map((log) => [
        format(new Date(log.timestamp), 'yyyy-MM-dd HH:mm:ss'),
        log.action,
        log.entityType,
        log.entityName || '',
        log.userName,
        log.ipAddress,
        log.details || '',
      ]);

      const csvContent = [headers, ...rows].map((row) => row.join(',')).join('\n');
      const blob = new Blob([csvContent], { type: 'text/csv' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `audit-logs-${format(new Date(), 'yyyy-MM-dd')}.csv`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Export failed:', error);
    } finally {
      setIsExporting(false);
    }
  };

  const getActionBadge = (action: AuditAction) => {
    const config: Record<AuditAction, { variant: any; icon: any; label: string }> = {
      [AuditAction.CREATE]: { variant: 'default', icon: Database, label: 'Create' },
      [AuditAction.UPDATE]: { variant: 'default', icon: Settings, label: 'Update' },
      [AuditAction.DELETE]: { variant: 'destructive', icon: Database, label: 'Delete' },
      [AuditAction.LOGIN]: { variant: 'default', icon: Shield, label: 'Login' },
      [AuditAction.LOGOUT]: { variant: 'secondary', icon: Shield, label: 'Logout' },
      [AuditAction.ACCESS]: { variant: 'secondary', icon: Eye, label: 'Access' },
      [AuditAction.SYNC]: { variant: 'default', icon: Activity, label: 'Sync' },
      [AuditAction.EXPORT]: { variant: 'secondary', icon: Download, label: 'Export' },
    };

    const { variant, icon: Icon, label } = config[action];
    return (
      <Badge variant={variant} className="flex items-center gap-1">
        <Icon className="h-3 w-3" />
        {label}
      </Badge>
    );
  };

  const getEntityIcon = (entityType: EntityType) => {
    const icons: Record<EntityType, any> = {
      [EntityType.TENANT]: Building2,
      [EntityType.SHOP]: Store,
      [EntityType.USER]: User,
      [EntityType.API_KEY]: Shield,
      [EntityType.SUBSCRIPTION]: CreditCard,
      [EntityType.INVOICE]: FileText,
      [EntityType.SYNC_LOG]: Activity,
    };

    const Icon = icons[entityType] || Database;
    return <Icon className="h-4 w-4 text-muted-foreground" />;
  };

  // Pagination
  const indexOfLastLog = currentPage * logsPerPage;
  const indexOfFirstLog = indexOfLastLog - logsPerPage;
  const currentLogs = filteredLogs.slice(indexOfFirstLog, indexOfLastLog);
  const totalPages = Math.ceil(filteredLogs.length / logsPerPage);

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <FileText className="h-8 w-8" />
            Audit Logs
          </h1>
          <p className="text-muted-foreground mt-2">View and search system audit logs</p>
        </div>
        <Card>
          <CardContent className="py-12">
            <div className="flex items-center justify-center">
              <div className="text-center">
                <Loader className="inline-block animate-spin h-12 w-12 text-primary" />
                <p className="mt-4 text-muted-foreground">Loading audit logs...</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <FileText className="h-8 w-8" />
            Audit Logs
          </h1>
          <p className="text-muted-foreground mt-2">View and search system audit logs</p>
        </div>
        <Button onClick={handleExport} disabled={isExporting || filteredLogs.length === 0}>
          {isExporting ? (
            <>
              <Loader className="inline-block animate-spin h-4 w-4 mr-2" />
              Exporting...
            </>
          ) : (
            <>
              <Download className="h-4 w-4 mr-2" />
              Export CSV
            </>
          )}
        </Button>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="h-5 w-5" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="space-y-2">
              <Label htmlFor="search">Search</Label>
              <div className="relative">
                <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="search"
                  className="pl-9"
                  placeholder="Search logs..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="action">Action</Label>
              <Select value={actionFilter} onValueChange={setActionFilter}>
                <SelectTrigger id="action">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Actions</SelectItem>
                  {Object.values(AuditAction).map((action) => (
                    <SelectItem key={action} value={action}>
                      {action}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="entityType">Entity Type</Label>
              <Select value={entityTypeFilter} onValueChange={setEntityTypeFilter}>
                <SelectTrigger id="entityType">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">All Entities</SelectItem>
                  {Object.values(EntityType).map((type) => (
                    <SelectItem key={type} value={type}>
                      {type}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="dateRange">
                <Calendar className="inline h-4 w-4 mr-2" />
                Date Range
              </Label>
              <Select value={dateRange} onValueChange={setDateRange}>
                <SelectTrigger id="dateRange">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1_DAY">Last 24 Hours</SelectItem>
                  <SelectItem value="7_DAYS">Last 7 Days</SelectItem>
                  <SelectItem value="30_DAYS">Last 30 Days</SelectItem>
                  <SelectItem value="90_DAYS">Last 90 Days</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Audit Logs Table */}
      <Card>
        <CardHeader>
          <CardTitle>
            Activity Log ({filteredLogs.length} {filteredLogs.length === 1 ? 'entry' : 'entries'})
          </CardTitle>
          <CardDescription>Detailed audit trail of all system activities</CardDescription>
        </CardHeader>
        <CardContent>
          {currentLogs.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <FileText className="h-16 w-16 text-muted-foreground mb-4" />
              <h3 className="text-lg font-semibold mb-2">No audit logs found</h3>
              <p className="text-muted-foreground max-w-md">
                {searchQuery || actionFilter !== 'ALL' || entityTypeFilter !== 'ALL'
                  ? 'No logs match your search criteria. Try adjusting your filters.'
                  : 'No audit logs available for the selected date range.'}
              </p>
            </div>
          ) : (
            <>
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Timestamp</TableHead>
                      <TableHead>Action</TableHead>
                      <TableHead>Entity</TableHead>
                      <TableHead>User</TableHead>
                      <TableHead>IP Address</TableHead>
                      <TableHead>Details</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {currentLogs.map((log) => (
                      <TableRow key={log.id}>
                        <TableCell className="text-sm">
                          {format(new Date(log.timestamp), 'MMM d, yyyy HH:mm:ss')}
                        </TableCell>
                        <TableCell>{getActionBadge(log.action)}</TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            {getEntityIcon(log.entityType)}
                            <div>
                              <div className="font-medium text-sm">{log.entityName || 'N/A'}</div>
                              <div className="text-xs text-muted-foreground">{log.entityType}</div>
                            </div>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div>
                            <div className="font-medium text-sm">{log.userName}</div>
                            <div className="text-xs text-muted-foreground">{log.userEmail}</div>
                          </div>
                        </TableCell>
                        <TableCell className="text-sm font-mono">{log.ipAddress}</TableCell>
                        <TableCell className="text-sm text-muted-foreground max-w-md truncate">
                          {log.details}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-between mt-4">
                  <div className="text-sm text-muted-foreground">
                    Showing {indexOfFirstLog + 1} to {Math.min(indexOfLastLog, filteredLogs.length)} of{' '}
                    {filteredLogs.length} logs
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage((prev) => Math.max(1, prev - 1))}
                      disabled={currentPage === 1}
                    >
                      Previous
                    </Button>
                    <div className="text-sm">
                      Page {currentPage} of {totalPages}
                    </div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setCurrentPage((prev) => Math.min(totalPages, prev + 1))}
                      disabled={currentPage === totalPages}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              )}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default AuditLogsPage;
