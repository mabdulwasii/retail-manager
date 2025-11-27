import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { NumericInput } from "@/components/ui/numeric-input";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useProcessWithdrawal } from "@/hooks/investment/useInvestmentMutations";
import { useCurrency } from "@/hooks/useCurrency";
import type { Investment } from "@/types/investment";
import { yupResolver } from "@hookform/resolvers/yup";
import { differenceInMonths } from "date-fns";
import { AlertCircle } from "lucide-react";
import React, { useMemo } from "react";
import { Controller, useForm } from "react-hook-form";
import * as yup from "yup";

const withdrawalSchema = yup.object().shape({
  withdrawalType: yup.string().oneOf(["partial", "full"]).required(),
  amount: yup.number().positive("Amount must be greater than 0").required(),
  reason: yup.string().required("Reason is required"),
  paymentMethod: yup.string().optional(),
  bankAccount: yup.string().optional(),
  notes: yup.string().optional(),
});

type WithdrawalFormValues = yup.InferType<typeof withdrawalSchema>;

interface WithdrawalRequestModalProps {
  open: boolean;
  onClose: () => void;
  investment: Investment;
}

export function WithdrawalRequestModal({
  open,
  onClose,
  investment,
}: WithdrawalRequestModalProps) {
  const { formatCurrency } = useCurrency();
  const withdrawalMutation = useProcessWithdrawal();

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    control,
    formState: { errors },
  } = useForm<WithdrawalFormValues>({
    resolver: yupResolver(withdrawalSchema),
    defaultValues: {
      withdrawalType: "partial",
      amount: 0,
      reason: "",
      paymentMethod: "",
      bankAccount: "",
      notes: "",
    },
  });

  const withdrawalType = watch("withdrawalType");
  const amount = watch("amount");

  // Calculate early withdrawal penalty
  const penalty = useMemo(() => {
    if (!investment.maturityDate) return 0;

    const maturityDate = new Date(investment.maturityDate);
    const now = new Date();
    const monthsRemaining = differenceInMonths(maturityDate, now);

    if (monthsRemaining > 0) {
      // 2% penalty per month remaining
      const withdrawalAmount =
        withdrawalType === "full" ? investment.availableBalance : amount;
      return withdrawalAmount * (monthsRemaining * 0.02);
    }

    return 0;
  }, [
    investment.maturityDate,
    investment.availableBalance,
    withdrawalType,
    amount,
  ]);

  const netAmount = useMemo(() => {
    const withdrawalAmount =
      withdrawalType === "full" ? investment.availableBalance : amount;
    return withdrawalAmount - penalty;
  }, [withdrawalType, amount, investment.availableBalance, penalty]);

  const onSubmit = async (data: WithdrawalFormValues) => {
    try {
      const withdrawalAmount =
        data.withdrawalType === "full"
          ? investment.availableBalance
          : data.amount;

      await withdrawalMutation.mutateAsync({
        investmentId: investment.id,
        request: {
          amount: withdrawalAmount,
          reason: data.reason,
          paymentMethod: data.paymentMethod,
          bankAccount: data.bankAccount,
          notes: data.notes,
        },
      });

      reset();
      onClose();
    } catch (error) {
      console.error("Failed to process withdrawal:", error);
    }
  };

  const handleClose = () => {
    reset();
    onClose();
  };

  // Set amount to full balance when selecting full withdrawal
  React.useEffect(() => {
    if (withdrawalType === "full") {
      setValue("amount", investment.availableBalance);
    }
  }, [withdrawalType, investment.availableBalance, setValue]);

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[600px]">
        <DialogHeader>
          <DialogTitle>Request Withdrawal</DialogTitle>
          <DialogDescription>
            Withdraw funds from your investment
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-6 py-4">
            {/* Current Balance */}
            <div className="rounded-lg bg-blue-50 border border-blue-200 p-4">
              <div className="flex justify-between items-center">
                <div>
                  <p className="text-sm text-blue-700 font-medium">
                    Available Balance
                  </p>
                  <p className="text-2xl font-bold text-blue-900">
                    {formatCurrency(investment.availableBalance)}
                  </p>
                </div>
                <div className="text-right">
                  <p className="text-sm text-blue-700">Investment Status</p>
                  <p className="font-semibold text-blue-900">
                    {investment.status}
                  </p>
                </div>
              </div>
            </div>

            {/* Withdrawal Type */}
            <div className="space-y-3">
              <Label>Withdrawal Type</Label>
              <RadioGroup
                value={withdrawalType}
                onValueChange={(value) =>
                  setValue("withdrawalType", value as "partial" | "full")
                }
              >
                <div className="flex items-center space-x-2 border rounded-lg p-4">
                  <RadioGroupItem value="partial" id="partial" />
                  <Label htmlFor="partial" className="cursor-pointer flex-1">
                    <div className="font-medium">Partial Withdrawal</div>
                    <div className="text-sm text-muted-foreground">
                      Withdraw a specific amount
                    </div>
                  </Label>
                </div>
                <div className="flex items-center space-x-2 border rounded-lg p-4">
                  <RadioGroupItem value="full" id="full" />
                  <Label htmlFor="full" className="cursor-pointer flex-1">
                    <div className="font-medium">Full Withdrawal</div>
                    <div className="text-sm text-muted-foreground">
                      Withdraw entire balance and close investment
                    </div>
                  </Label>
                </div>
              </RadioGroup>
            </div>

            {/* Amount */}
            {withdrawalType === "partial" && (
              <div className="space-y-2">
                <Label htmlFor="amount">Withdrawal Amount *</Label>
                <Controller
                  name="amount"
                  control={control}
                  render={({ field }) => (
                    <NumericInput
                      id="amount"
                      value={field.value ?? ""}
                      onValueChange={(values) => {
                        field.onChange(values.floatValue ?? 0);
                      }}
                      placeholder="10000"
                      decimalScale={2}
                      fixedDecimalScale={false}
                      allowNegative={false}
                      isAllowed={(values) => {
                        const { floatValue } = values;
                        return (
                          floatValue === undefined ||
                          floatValue <= investment.availableBalance
                        );
                      }}
                    />
                  )}
                />
                <p className="text-sm text-muted-foreground">
                  Maximum: {formatCurrency(investment.availableBalance)}
                </p>
                {errors.amount && (
                  <p className="text-sm text-red-600">
                    {errors.amount.message}
                  </p>
                )}
              </div>
            )}

            {/* Penalty Warning */}
            {penalty > 0 && (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  <strong>Early Withdrawal Penalty:</strong>{" "}
                  {formatCurrency(penalty)}
                  <br />
                  <span className="text-sm">
                    2% penalty per month remaining until maturity
                  </span>
                </AlertDescription>
              </Alert>
            )}

            {/* Calculation Summary */}
            <div className="rounded-lg border p-4 space-y-2 bg-muted">
              <h4 className="font-semibold text-sm mb-3">Withdrawal Summary</h4>
              <div className="flex justify-between text-sm">
                <span className="text-muted-foreground">
                  Withdrawal Amount:
                </span>
                <span className="font-medium">
                  {formatCurrency(
                    withdrawalType === "full"
                      ? investment.availableBalance
                      : amount
                  )}
                </span>
              </div>
              {penalty > 0 && (
                <div className="flex justify-between text-sm text-red-600">
                  <span>Early Withdrawal Penalty:</span>
                  <span className="font-medium">
                    -{formatCurrency(penalty)}
                  </span>
                </div>
              )}
              <div className="flex justify-between text-base font-semibold pt-2 border-t">
                <span>Net Amount:</span>
                <span className="text-green-600">
                  {formatCurrency(netAmount)}
                </span>
              </div>
            </div>

            {/* Reason */}
            <div className="space-y-2">
              <Label htmlFor="reason">Reason for Withdrawal *</Label>
              <Select
                value={watch("reason")}
                onValueChange={(value) => setValue("reason", value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select reason" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="emergency_funds">
                    Emergency Funds Needed
                  </SelectItem>
                  <SelectItem value="better_opportunity">
                    Better Investment Opportunity
                  </SelectItem>
                  <SelectItem value="maturity">Investment Matured</SelectItem>
                  <SelectItem value="business_needs">Business Needs</SelectItem>
                  <SelectItem value="personal_reasons">
                    Personal Reasons
                  </SelectItem>
                  <SelectItem value="other">Other</SelectItem>
                </SelectContent>
              </Select>
              {errors.reason && (
                <p className="text-sm text-red-600">{errors.reason.message}</p>
              )}
            </div>

            {/* Payment Method */}
            <div className="space-y-2">
              <Label htmlFor="paymentMethod">Preferred Payment Method</Label>
              <Select
                value={watch("paymentMethod")}
                onValueChange={(value) => setValue("paymentMethod", value)}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select payment method" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="BANK_TRANSFER">Bank Transfer</SelectItem>
                  <SelectItem value="CHECK">Check</SelectItem>
                  <SelectItem value="MOBILE_MONEY">Mobile Money</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Bank Account */}
            <div className="space-y-2">
              <Label htmlFor="bankAccount">Bank Account (Optional)</Label>
              <Input
                id="bankAccount"
                placeholder="****1234"
                {...register("bankAccount")}
              />
              <p className="text-xs text-muted-foreground">
                Confirm your bank account for the transfer
              </p>
            </div>

            {/* Notes */}
            <div className="space-y-2">
              <Label htmlFor="notes">Additional Notes (Optional)</Label>
              <Textarea
                id="notes"
                placeholder="Any additional information..."
                rows={3}
                {...register("notes")}
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={withdrawalMutation.isPending}
            >
              Cancel
            </Button>
            <Button type="submit" disabled={withdrawalMutation.isPending}>
              {withdrawalMutation.isPending
                ? "Processing..."
                : "Submit Request"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
