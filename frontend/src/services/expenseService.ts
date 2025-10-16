import api from "@/lib/axios";

export interface ExpenseSummary {
  totalExpenses: number;
  pendingApproval: number;
  approvedExpenses: number;
  totalAmount: number;
  monthlyTotal: number;
  categoryBreakdown: Array<{
    category: string;
    itemCount: number;
    totalValue: number;
  }>;
}

export interface Expense {
  id: string;
  title: string;
  description?: string;
  category: any;
  amount: number;
  date: string;
  shopId: string;
  requestedBy: string;
  requestedByName: string;
  approvedBy?: string;
  approvedByName?: string;
  status: string;
  receiptUrl?: string;
  tags: string[];
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateExpenseRequest {
  title: string;
  description?: string;
  categoryId: string;
  amount: number;
  date: string;
  receiptFile?: File;
  tags?: string[];
  notes?: string;
}

export interface UpdateExpenseRequest extends Partial<CreateExpenseRequest> {
  status?: string;
}

export const expenseService = {
  async getExpenseSummary(
    shopId: string,
    startDate?: string,
    endDate?: string
  ): Promise<ExpenseSummary> {
    const params: Record<string, string> = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;

    const { data } = await api.get(`/shops/${shopId}/expenses/summary`, {
      params,
    });
    return data;
  },

  async getExpenses(
    shopId: string,
    params?: Record<string, any>
  ): Promise<Expense[]> {
    const { data } = await api.get(`/shops/${shopId}/expenses`, { params });
    return data;
  },

  async getExpenseById(expenseId: string): Promise<Expense> {
    const { data } = await api.get(`/expenses/${expenseId}`);
    return data;
  },

  async createExpense(
    shopId: string,
    request: CreateExpenseRequest
  ): Promise<Expense> {
    const formData = new FormData();
    formData.append("title", request.title);
    if (request.description)
      formData.append("description", request.description);
    formData.append("categoryId", request.categoryId);
    formData.append("amount", request.amount.toString());
    formData.append("date", request.date);
    if (request.receiptFile) formData.append("receipt", request.receiptFile);
    if (request.tags?.length)
      formData.append("tags", JSON.stringify(request.tags));
    if (request.notes) formData.append("notes", request.notes);

    const { data } = await api.post(`/shops/${shopId}/expenses`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return data;
  },

  async updateExpense(
    expenseId: string,
    updates: UpdateExpenseRequest
  ): Promise<Expense> {
    const formData = new FormData();
    if (updates.title) formData.append("title", updates.title);
    if (updates.description !== undefined)
      formData.append("description", updates.description);
    if (updates.categoryId) formData.append("categoryId", updates.categoryId);
    if (updates.amount) formData.append("amount", updates.amount.toString());
    if (updates.date) formData.append("date", updates.date);
    if (updates.receiptFile) formData.append("receipt", updates.receiptFile);
    if (updates.tags) formData.append("tags", JSON.stringify(updates.tags));
    if (updates.notes !== undefined) formData.append("notes", updates.notes);
    if (updates.status) formData.append("status", updates.status);

    const { data } = await api.put(`/expenses/${expenseId}`, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
    return data;
  },

  async deleteExpense(expenseId: string): Promise<void> {
    await api.delete(`/expenses/${expenseId}`);
  },

  async approveExpense(expenseId: string, notes?: string): Promise<Expense> {
    const { data } = await api.post(`/expenses/${expenseId}/approve`, {
      notes,
    });
    return data;
  },

  async rejectExpense(expenseId: string, notes?: string): Promise<Expense> {
    const { data } = await api.post(`/expenses/${expenseId}/reject`, {
      notes,
    });
    return data;
  },

  async uploadReceipt(
    expenseId: string,
    file: File
  ): Promise<{ receiptUrl: string }> {
    const formData = new FormData();
    formData.append("receipt", file);

    const { data } = await api.post(
      `/expenses/${expenseId}/receipt`,
      formData,
      {
        headers: { "Content-Type": "multipart/form-data" },
      }
    );
    return data;
  },
};
