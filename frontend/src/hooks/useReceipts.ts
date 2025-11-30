import { useAuth } from "@/context/ManualAuthContext";
import {
  Receipt,
  ReceiptFilter,
  receiptService,
} from "@/services/receiptService";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

// Re-export types
export type { Receipt, ReceiptFilter };

/**
 * Hook to fetch paginated receipts with filters
 */
export const useReceipts = (filter?: ReceiptFilter) => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: ["receipts", filter],
    queryFn: () => receiptService.getReceipts(filter),
    enabled: !!isAuthenticated,
    staleTime: 1 * 60 * 1000, // 1 minute
    gcTime: 5 * 60 * 1000,
    refetchOnWindowFocus: false,
    retry: 2,
  });
};

/**
 * Hook to fetch receipt by ID
 */
export const useReceiptById = (receiptId: string | undefined) => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: ["receipts", receiptId],
    queryFn: () => receiptService.getReceiptById(receiptId!),
    enabled: !!(isAuthenticated && receiptId),
    staleTime: 2 * 60 * 1000, // 2 minutes
    gcTime: 5 * 60 * 1000,
    refetchOnWindowFocus: false,
    retry: 2,
  });
};

/**
 * Hook to fetch receipt by number
 */
export const useReceiptByNumber = (receiptNumber: string | undefined) => {
  const { isAuthenticated } = useAuth();

  return useQuery({
    queryKey: ["receipts", "by-number", receiptNumber],
    queryFn: () => receiptService.getReceiptByNumber(receiptNumber!),
    enabled: !!(isAuthenticated && receiptNumber),
    staleTime: 2 * 60 * 1000,
    gcTime: 5 * 60 * 1000,
    refetchOnWindowFocus: false,
    retry: 2,
  });
};

/**
 * Mutation to generate receipt
 */
export const useGenerateReceipt = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (transactionId: string) =>
      receiptService.generateReceipt(transactionId),
    onSuccess: (newReceipt) => {
      queryClient.invalidateQueries({ queryKey: ["receipts"] });
      toast.success("Receipt generated successfully", {
        description: `Receipt ${newReceipt.receiptNumber} has been generated.`,
      });
    },
    onError: (error: any) => {
      toast.error("Failed to generate receipt", {
        description:
          error.response?.data?.message || error.message || "An error occurred",
      });
    },
  });
};

/**
 * Mutation to regenerate receipt
 */
export const useRegenerateReceipt = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (transactionId: string) =>
      receiptService.regenerateReceipt(transactionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["receipts"] });
      toast.success("Receipt regenerated successfully");
    },
    onError: (error: any) => {
      toast.error("Failed to regenerate receipt", {
        description:
          error.response?.data?.message || error.message || "An error occurred",
      });
    },
  });
};

/**
 * Mutation to mark receipt as printed
 */
export const useMarkAsPrinted = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      receiptId,
      printedBy,
    }: {
      receiptId: string;
      printedBy: string;
    }) => receiptService.markAsPrinted(receiptId, printedBy),
    onSuccess: (updatedReceipt) => {
      queryClient.invalidateQueries({ queryKey: ["receipts"] });
      queryClient.invalidateQueries({
        queryKey: ["receipts", updatedReceipt.id],
      });
      toast.success("Receipt marked as printed");
    },
    onError: (error: any) => {
      toast.error("Failed to mark receipt as printed", {
        description:
          error.response?.data?.message || error.message || "An error occurred",
      });
    },
  });
};

/**
 * Mutation to mark receipt as emailed
 */
export const useMarkAsEmailed = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      receiptId,
      emailAddress,
    }: {
      receiptId: string;
      emailAddress: string;
    }) => receiptService.markAsEmailed(receiptId, emailAddress),
    onSuccess: (updatedReceipt, variables) => {
      queryClient.invalidateQueries({ queryKey: ["receipts"] });
      queryClient.invalidateQueries({
        queryKey: ["receipts", updatedReceipt.id],
      });
      toast.success(`Receipt sent to ${variables.emailAddress}`);
    },
    onError: (error: any) => {
      toast.error("Failed to send receipt", {
        description:
          error.response?.data?.message || error.message || "An error occurred",
      });
    },
  });
};

/**
 * Mutation to download receipt PDF
 */
export const useDownloadReceipt = () => {
  return useMutation({
    mutationFn: ({
      transactionId,
      receiptNumber,
    }: {
      transactionId: string;
      receiptNumber: string;
    }) => receiptService.downloadReceiptPDF(transactionId, receiptNumber),
    onSuccess: () => {
      toast.success("Receipt downloaded");
    },
    onError: (error: any) => {
      toast.error("Failed to download receipt", {
        description:
          error.response?.data?.message || error.message || "An error occurred",
      });
    },
  });
};

/**
 * Mutation to print receipt
 */
export const usePrintReceipt = () => {
  return useMutation({
    mutationFn: (transactionId: string) =>
      receiptService.printReceipt(transactionId),
    onError: (error: any) => {
      toast.error("Failed to print receipt", {
        description:
          error.response?.data?.message || error.message || "An error occurred",
      });
    },
  });
};
