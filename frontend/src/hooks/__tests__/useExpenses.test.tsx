import { Expense, expenseService } from "@/services/expenseService";
import { getMockExpense, getMockExpenses } from "@/testData/expenses";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import React from "react";
import { toast } from "sonner";
import {
  useApproveExpense,
  useCreateExpense,
  useDeleteExpense,
  useExpenseById,
  useExpenses,
  useRejectExpense,
  useUpdateExpense,
  useUploadReceipt,
} from "../useExpenses";

jest.mock("@/services/expenseService", () => ({
  expenseService: {
    getExpenses: jest.fn(),
    getExpenseById: jest.fn(),
    createExpense: jest.fn(),
    updateExpense: jest.fn(),
    deleteExpense: jest.fn(),
    approveExpense: jest.fn(),
    rejectExpense: jest.fn(),
    uploadReceipt: jest.fn(),
  },
}));

jest.mock("sonner", () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn(),
  },
}));

jest.mock("@/context/UnifiedAuthContext", () => ({
  useAuth: () => ({
    user: {
      id: "user1",
      username: "manager",
      email: "manager@example.com",
      roles: ["ROLE_MANAGER"],
      shopId: "shop1",
    },
    isAuthenticated: true,
    hasAnyPermission: () => true,
  }),
}));

const mockExpenseService = expenseService as jest.Mocked<typeof expenseService>;
const mockToast = toast as jest.Mocked<typeof toast>;

describe("useExpenses", () => {
  let queryClient: QueryClient;
  let wrapper: React.FC<{ children: React.ReactNode }>;

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
        mutations: { retry: false },
      },
    });
    wrapper = ({ children }) =>
      React.createElement(
        QueryClientProvider,
        { client: queryClient },
        children
      );
    jest.clearAllMocks();
  });

  afterEach(() => {
    queryClient.clear();
  });

  describe("useExpenses", () => {
    it("should fetch expenses successfully", async () => {
      // Using test data from @/testData/expenses
      const mockData = getMockExpenses() as unknown as Expense[];
      mockExpenseService.getExpenses.mockResolvedValueOnce(mockData);

      const { result } = renderHook(() => useExpenses(), { wrapper });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.content).toHaveLength(3);
      expect(result.current.data?.content[0].title).toBe(
        "Office Supplies Purchase"
      );
      expect(mockExpenseService.getExpenses).toHaveBeenCalledWith(
        "shop1",
        undefined
      );
    });

    it("should fetch expenses with filters", async () => {
      const mockData = getMockExpenses() as unknown as Expense[];
      mockExpenseService.getExpenses.mockResolvedValueOnce(mockData);

      const filter = {
        startDate: "2024-01-01",
        endDate: "2024-01-31",
        status: "APPROVED" as const,
        minAmount: 100,
        maxAmount: 1000,
      };

      const { result } = renderHook(() => useExpenses(undefined, filter), {
        wrapper,
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(mockExpenseService.getExpenses).toHaveBeenCalledWith(
        "shop1",
        filter
      );
    });

    it("should use provided shopId over user.shopId", async () => {
      const mockData = getMockExpenses();
      mockExpenseService.getExpenses.mockResolvedValueOnce(mockData as unknown as Expense[]);

      const { result } = renderHook(() => useExpenses("shop2"), { wrapper });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(mockExpenseService.getExpenses).toHaveBeenCalledWith(
        "shop2",
        undefined
      );
    });

    it("should handle fetch error", async () => {
      mockExpenseService.getExpenses.mockRejectedValueOnce(
        new Error("Failed to fetch")
      );

      const { result } = renderHook(() => useExpenses(), { wrapper });

      await waitFor(
        () => {
          expect(result.current.isError).toBe(true);
        },
        { timeout: 3000 }
      );
    });
  });

  describe("useExpenseById", () => {
    it("should fetch single expense successfully", async () => {
      // Using test data factory
      const mockExpense = getMockExpense();
      mockExpenseService.getExpenseById.mockResolvedValueOnce(mockExpense);

      const { result } = renderHook(() => useExpenseById("exp1"), { wrapper });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.id).toBe("exp1");
      expect(result.current.data?.title).toBe("Office Supplies Purchase");
      expect(mockExpenseService.getExpenseById).toHaveBeenCalledWith("exp1");
    });

    it("should not fetch if expenseId is undefined", () => {
      const { result } = renderHook(() => useExpenseById(undefined), {
        wrapper,
      });

      expect(result.current.isLoading).toBe(false);
      expect(mockExpenseService.getExpenseById).not.toHaveBeenCalled();
    });

    it("should handle fetch error", async () => {
      mockExpenseService.getExpenseById.mockRejectedValueOnce(
        new Error("Expense not found")
      );

      const { result } = renderHook(() => useExpenseById("invalid"), {
        wrapper,
      });

      await waitFor(
        () => {
          expect(result.current.isError).toBe(true);
        },
        { timeout: 3000 }
      );
    });
  });

  describe("useCreateExpense", () => {
    it("should create expense successfully", async () => {
      // Using test data factory
      const newExpense = getMockExpense({
        id: "exp-new",
        title: "New Expense",
      });
      mockExpenseService.createExpense.mockResolvedValueOnce(newExpense);

      const { result } = renderHook(() => useCreateExpense(), { wrapper });

      await result.current.mutateAsync({
        shopId: "shop1",
        data: {
          title: "New Expense",
          description: "Test expense",
          categoryId: "cat1",
          amount: 200,
          date: new Date().toISOString(),
        },
      });

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          "Expense created successfully"
        );
      });

      expect(mockExpenseService.createExpense).toHaveBeenCalled();
    });

    it("should handle create error", async () => {
      const error = { response: { data: { message: "Amount exceeds limit" } } };
      mockExpenseService.createExpense.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useCreateExpense(), { wrapper });

      try {
        await result.current.mutateAsync({
          shopId: "shop1",
          data: {
            title: "Test",
            categoryId: "cat1",
            amount: 10000,
            date: new Date().toISOString(),
          },
        });
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          "Failed to create expense",
          { description: "Amount exceeds limit" }
        );
      });
    });
  });

  describe("useUpdateExpense", () => {
    it("should update expense successfully", async () => {
      // Using test data factory with updated properties
      const updatedExpense = getMockExpense({ title: "Updated Expense" });
      mockExpenseService.updateExpense.mockResolvedValueOnce(updatedExpense);

      const { result } = renderHook(() => useUpdateExpense(), { wrapper });

      await result.current.mutateAsync({
        expenseId: "exp1",
        updates: { title: "Updated Expense" },
      });

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          "Expense updated successfully"
        );
      });

      expect(mockExpenseService.updateExpense).toHaveBeenCalledWith("exp1", {
        title: "Updated Expense",
      });
    });

    it("should handle update error", async () => {
      const error = { message: "Cannot update approved expense" };
      mockExpenseService.updateExpense.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useUpdateExpense(), { wrapper });

      try {
        await result.current.mutateAsync({
          expenseId: "exp1",
          updates: { amount: 500 },
        });
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalled();
      });
    });
  });

  describe("useDeleteExpense", () => {
    it("should delete expense successfully", async () => {
      mockExpenseService.deleteExpense.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useDeleteExpense(), { wrapper });

      await result.current.mutateAsync("exp1");

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          "Expense deleted successfully"
        );
      });

      expect(mockExpenseService.deleteExpense).toHaveBeenCalledWith("exp1");
    });

    it("should handle delete error", async () => {
      const error = {
        response: { data: { message: "Cannot delete paid expense" } },
      };
      mockExpenseService.deleteExpense.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useDeleteExpense(), { wrapper });

      try {
        await result.current.mutateAsync("exp1");
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          "Failed to delete expense",
          { description: "Cannot delete paid expense" }
        );
      });
    });
  });

  describe("useApproveExpense", () => {
    it("should approve expense successfully", async () => {
      mockExpenseService.approveExpense.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useApproveExpense(), { wrapper });

      await result.current.mutateAsync({
        expenseId: "exp1",
        notes: "Approved for office supplies",
      });

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          "Expense approved successfully"
        );
      });

      expect(mockExpenseService.approveExpense).toHaveBeenCalledWith(
        "exp1",
        "Approved for office supplies"
      );
    });

    it("should handle approve error", async () => {
      const error = { message: "Insufficient permissions" };
      mockExpenseService.approveExpense.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useApproveExpense(), { wrapper });

      try {
        await result.current.mutateAsync({ expenseId: "exp1" });
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalled();
      });
    });
  });

  describe("useRejectExpense", () => {
    it("should reject expense successfully", async () => {
      mockExpenseService.rejectExpense.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useRejectExpense(), { wrapper });

      await result.current.mutateAsync({
        expenseId: "exp1",
        notes: "Not justified",
      });

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith("Expense rejected");
      });

      expect(mockExpenseService.rejectExpense).toHaveBeenCalledWith(
        "exp1",
        "Not justified"
      );
    });

    it("should handle reject error", async () => {
      const error = { response: { data: { message: "Already approved" } } };
      mockExpenseService.rejectExpense.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useRejectExpense(), { wrapper });

      try {
        await result.current.mutateAsync({ expenseId: "exp1" });
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          "Failed to reject expense",
          { description: "Already approved" }
        );
      });
    });
  });

  describe("useUploadReceipt", () => {
    it("should upload receipt successfully", async () => {
      mockExpenseService.uploadReceipt.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useUploadReceipt(), { wrapper });

      const mockFile = new File(["receipt"], "receipt.pdf", {
        type: "application/pdf",
      });

      await result.current.mutateAsync({
        expenseId: "exp1",
        file: mockFile,
      });

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          "Receipt uploaded successfully"
        );
      });

      expect(mockExpenseService.uploadReceipt).toHaveBeenCalledWith(
        "exp1",
        mockFile
      );
    });

    it("should handle upload error", async () => {
      const error = { message: "File too large" };
      mockExpenseService.uploadReceipt.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useUploadReceipt(), { wrapper });

      const mockFile = new File(["large receipt"], "large.pdf", {
        type: "application/pdf",
      });

      try {
        await result.current.mutateAsync({
          expenseId: "exp1",
          file: mockFile,
        });
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalledWith(
          "Failed to upload receipt",
          { description: "File too large" }
        );
      });
    });
  });
});
