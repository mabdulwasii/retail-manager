import { api } from "@/services/api";
import {
  getMockFraudAlert,
  getMockFraudAlerts,
  getMockFraudRules,
  getMockRiskAssessments,
} from "@/testData/fraud";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { renderHook, waitFor } from "@testing-library/react";
import React from "react";
import { toast } from "sonner";
import {
  useAcknowledgeFraudAlert,
  useCreateFraudRule,
  useDeleteFraudRule,
  useFraudAlertById,
  useFraudAlerts,
  useFraudRules,
  useResolveFraudAlert,
  useRiskAssessments,
  useUpdateFraudRule,
} from "../useFraudDetection";

// Mock API service
jest.mock("@/services/api", () => ({
  api: {
    get: jest.fn(),
    post: jest.fn(),
    patch: jest.fn(),
    delete: jest.fn(),
  },
}));

const mockApi = api as jest.Mocked<typeof api>;
jest.mock("sonner", () => ({
  toast: {
    success: jest.fn(),
    error: jest.fn(),
  },
}));

jest.mock("@/context/ManualAuthContext", () => ({
  useAuth: () => ({
    user: {
      id: "user1",
      username: "admin",
      email: "admin@example.com",
      roles: ["ROLE_ADMIN"],
      shopId: "shop1",
    },
    isAuthenticated: true,
    hasAnyPermission: () => true,
    hasPermission: () => true,
  }),
}));

const mockToast = toast as jest.Mocked<typeof toast>;

describe("useFraudDetection", () => {
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

  describe("useFraudAlerts", () => {
    it("should fetch fraud alerts successfully", async () => {
      // Using test data from @/testData/fraud
      const mockData = getMockFraudAlerts();
      mockApi.get.mockResolvedValueOnce(mockData);

      const { result } = renderHook(() => useFraudAlerts(), { wrapper });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.content).toHaveLength(3);
      expect(result.current.data?.content[0].alertNumber).toBe("FRD-2024-001");
      expect(result.current.data?.content[0].severity).toBe("HIGH");
    });

    it("should fetch alerts with filters", async () => {
      const mockData = getMockFraudAlerts();
      mockApi.get.mockResolvedValueOnce(mockData);

      const filter = { severity: "HIGH" as const };
      const { result } = renderHook(() => useFraudAlerts(filter), { wrapper });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.content).toHaveLength(3);
    });

    it("should handle fetch error", async () => {
      mockApi.get.mockRejectedValueOnce(new Error("Unauthorized"));

      const { result } = renderHook(() => useFraudAlerts(), { wrapper });

      await waitFor(
        () => {
          expect(result.current.isError).toBe(true);
        },
        { timeout: 3000 }
      );
    });
  });

  describe("useFraudAlertById", () => {
    it("should fetch single alert successfully", async () => {
      const mockAlert = getMockFraudAlert();
      mockApi.get.mockResolvedValueOnce(mockAlert);

      const { result } = renderHook(() => useFraudAlertById("alert1"), {
        wrapper,
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.id).toBe("alert1");
      expect(result.current.data?.alertNumber).toBe("FRD-2024-001");
    });

    it("should not fetch if alertId is undefined", () => {
      const { result } = renderHook(() => useFraudAlertById(undefined), {
        wrapper,
      });

      expect(result.current.isLoading).toBe(false);
      expect(result.current.fetchStatus).toBe("idle");
    });
  });

  describe("useRiskAssessments", () => {
    it("should fetch risk assessments successfully", async () => {
      const mockData = getMockRiskAssessments();
      mockApi.get.mockResolvedValueOnce(mockData);

      const { result } = renderHook(() => useRiskAssessments(), { wrapper });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data?.content).toHaveLength(2);
      expect(result.current.data?.content[0].assessmentType).toBe(
        "TRANSACTION_RISK"
      );
    });
  });

  describe("useFraudRules", () => {
    it("should fetch fraud rules successfully", async () => {
      const mockRules = getMockFraudRules();
      mockApi.get.mockResolvedValueOnce(mockRules);

      const { result } = renderHook(() => useFraudRules(), { wrapper });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });

      expect(result.current.data).toHaveLength(3);
      expect(result.current.data?.[0].ruleName).toBe("Large Transaction Rule");
    });
  });

  describe("useAcknowledgeFraudAlert", () => {
    it("should acknowledge alert successfully", async () => {
      mockApi.post.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useAcknowledgeFraudAlert(), {
        wrapper,
      });

      await result.current.mutateAsync({
        alertId: "alert1",
        notes: "Acknowledged",
      });

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith("Alert acknowledged");
      });
    });

    it("should handle acknowledge error", async () => {
      const error = {
        response: { data: { message: "Alert already acknowledged" } },
      };
      mockApi.post.mockRejectedValueOnce(error);

      const { result } = renderHook(() => useAcknowledgeFraudAlert(), {
        wrapper,
      });

      try {
        await result.current.mutateAsync({ alertId: "alert1" });
      } catch (e) {
        // Expected
      }

      await waitFor(() => {
        expect(mockToast.error).toHaveBeenCalled();
      });
    });
  });

  describe("useResolveFraudAlert", () => {
    it("should resolve alert successfully", async () => {
      mockApi.post.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useResolveFraudAlert(), { wrapper });

      await result.current.mutateAsync({
        alertId: "alert1",
        notes: "Investigated - not fraud",
      });

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith("Alert resolved");
      });
    });
  });

  describe("useCreateFraudRule", () => {
    it("should create fraud rule successfully", async () => {
      mockApi.post.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useCreateFraudRule(), { wrapper });

      await result.current.mutateAsync({
        ruleName: "New Rule",
        ruleType: "CUSTOM",
        enabled: true,
        riskScoreWeight: 0.5,
        severity: "MEDIUM" as const,
        autoBlock: false,
        requiresManualReview: true,
      });

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          "Fraud rule created successfully"
        );
      });
    });
  });

  describe("useUpdateFraudRule", () => {
    it("should update fraud rule successfully", async () => {
      mockApi.patch.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useUpdateFraudRule(), { wrapper });

      await result.current.mutateAsync({
        ruleId: "rule1",
        data: { enabled: false },
      });

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          "Fraud rule updated successfully"
        );
      });
    });
  });

  describe("useDeleteFraudRule", () => {
    it("should delete fraud rule successfully", async () => {
      mockApi.delete.mockResolvedValueOnce(undefined);

      const { result } = renderHook(() => useDeleteFraudRule(), { wrapper });

      await result.current.mutateAsync("rule1");

      await waitFor(() => {
        expect(mockToast.success).toHaveBeenCalledWith(
          "Fraud rule deleted successfully"
        );
      });
    });
  });
});
