import api from "@/lib/axios";

export interface AuditLog {
  id: string;
  shopId: string;
  shopName: string;
  userId: string;
  username: string;
  category: AuditCategory;
  actionType: ActionType;
  entityType: string;
  entityId?: string;
  actionDescription: string;
  actionDate: string;
  ipAddress?: string;
  userAgent?: string;
  severity: Severity;
  success: boolean;
  errorMessage?: string;
  oldValues?: string;
  newValues?: string;
  details?: Record<string, any>;
}

export type AuditCategory =
  | "SECURITY_EVENT"
  | "DATA_MODIFICATION"
  | "FINANCIAL_TRANSACTION"
  | "SYSTEM_EVENT";

export type ActionType =
  | "CREATE"
  | "READ"
  | "UPDATE"
  | "DELETE"
  | "LOGIN"
  | "LOGOUT"
  | "APPROVE"
  | "REJECT"
  | "EXPORT"
  | "BACKUP"
  | "RESTORE";

export type Severity = "INFO" | "WARNING" | "ERROR" | "CRITICAL";

export interface AuditLogFilter {
  search?: string;
  actionType?: string;
  entityType?: string;
  category?: string;
  userId?: string;
  dateFrom?: string;
  dateTo?: string;
  severity?: string;
  success?: boolean;
  page?: number;
  size?: number;
}

export interface AuditLogPage {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export const auditLogService = {
  async getAuditLogs(
    shopId: string,
    filter?: AuditLogFilter
  ): Promise<AuditLogPage> {
    const { data } = await api.get(`/shops/${shopId}/audit-logs`, {
      params: filter,
    });
    return data;
  },

  async exportAuditLogs(
    shopId: string,
    filter?: Omit<AuditLogFilter, "page" | "size">
  ): Promise<Blob> {
    const { data } = await api.get(`/shops/${shopId}/audit-logs/export`, {
      params: filter,
      responseType: "blob",
    });
    return data;
  },
};
