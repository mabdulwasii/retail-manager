import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { LoadingSpinner } from "@/components/ui/loading-spinner";
import { NumericInput } from "@/components/ui/numeric-input";
import { Textarea } from "@/components/ui/textarea";
import { useCurrency } from "@/hooks/useCurrency";
import {
  CreateInvestmentRequest,
  InvestmentType,
  ProfitSharingModel,
  useInvestment,
} from "@/hooks/useInvestment";
import {
  CalendarIcon,
  DollarSignIcon,
  InfoIcon,
  PlusIcon,
  TrendingUpIcon,
} from "lucide-react";
import React, { useEffect, useState } from "react";

interface InvestmentFormProps {
  isOpen: boolean;
  onClose: () => void;
  shopId: string;
  onInvestmentCreated: () => void;
}

export const InvestmentForm: React.FC<InvestmentFormProps> = ({
  isOpen,
  onClose,
  shopId,
  onInvestmentCreated,
}) => {
  const { createInvestment, isLoading } = useInvestment();
  const { formatCurrency } = useCurrency();

  const [formData, setFormData] = useState<CreateInvestmentRequest>({
    shopId,
    investmentType: "SHOP_WIDE",
    amount: 0,
    profitSharingModel: "PROPORTIONAL_BY_AMOUNT",
    profitPercentage: 10,
    fixedShares: undefined,
    maturityDate: undefined,
    productIds: undefined,
    categoryFilter: undefined,
    notes: "",
  });

  const [validationErrors, setValidationErrors] = useState<
    Record<string, string>
  >({});

  useEffect(() => {
    if (!isOpen) {
      // Reset form when modal closes
      setFormData({
        shopId,
        investmentType: "SHOP_WIDE",
        amount: 0,
        profitSharingModel: "PROPORTIONAL_BY_AMOUNT",
        profitPercentage: 10,
        fixedShares: undefined,
        maturityDate: undefined,
        productIds: undefined,
        categoryFilter: undefined,
        notes: "",
      });
      setValidationErrors({});
    }
  }, [isOpen, shopId]);

  const handleInputChange = (
    field: keyof CreateInvestmentRequest,
    value: any
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear validation error when user starts typing
    if (validationErrors[field]) {
      setValidationErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  const validateForm = () => {
    const errors: Record<string, string> = {};

    if (!formData.amount || formData.amount < 100) {
      errors.amount = "Investment amount must be at least ₦100";
    }

    if (
      formData.profitSharingModel === "PROPORTIONAL_BY_AMOUNT" ||
      formData.profitSharingModel === "TIERED"
    ) {
      if (
        !formData.profitPercentage ||
        formData.profitPercentage <= 0 ||
        formData.profitPercentage > 100
      ) {
        errors.profitPercentage =
          "Profit percentage must be between 0.1% and 100%";
      }
    }

    if (formData.profitSharingModel === "FIXED_SHARES") {
      if (!formData.fixedShares || formData.fixedShares < 1) {
        errors.fixedShares = "Fixed shares must be at least 1";
      }
    }

    if (formData.maturityDate) {
      const maturityDate = new Date(formData.maturityDate);
      const today = new Date();
      if (maturityDate <= today) {
        errors.maturityDate = "Maturity date must be in the future";
      }
    }

    setValidationErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!validateForm()) {
      return;
    }

    const result = await createInvestment(formData);
    if (result) {
      onInvestmentCreated();
      onClose();
    }
  };

  const handleClose = () => {
    if (!isLoading) {
      onClose();
    }
  };

  const investmentTypeOptions: {
    value: InvestmentType;
    label: string;
    description: string;
  }[] = [
    {
      value: "SHOP_WIDE",
      label: "Shop-Wide Investment",
      description: "Invest in the entire shop performance",
    },
    {
      value: "PRODUCT_SPECIFIC",
      label: "Product-Specific",
      description: "Invest in specific products",
    },
    {
      value: "CATEGORY_SPECIFIC",
      label: "Category-Based",
      description: "Invest in product categories",
    },
  ];

  const profitSharingOptions: {
    value: ProfitSharingModel;
    label: string;
    description: string;
  }[] = [
    {
      value: "PROPORTIONAL_BY_AMOUNT",
      label: "Proportional by Amount",
      description: "Profits shared based on investment amount",
    },
    {
      value: "FIXED_SHARES",
      label: "Fixed Shares",
      description: "Fixed number of profit shares",
    },
    {
      value: "TIME_WEIGHTED",
      label: "Time-Weighted",
      description: "Profits based on investment duration",
    },
    {
      value: "TIERED",
      label: "Tiered System",
      description: "Different rates for different tiers",
    },
  ];

  const formatDateForInput = (dateString?: string): string => {
    if (!dateString) return "";
    return dateString.split("T")[0];
  };

  const getEstimatedReturn = () => {
    if (formData.amount > 0 && formData.profitPercentage) {
      const monthlyReturn =
        (formData.amount * formData.profitPercentage) / 100 / 12;
      return monthlyReturn;
    }
    return 0;
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center space-x-2">
            <TrendingUpIcon className="h-5 w-5" />
            <span>Create New Investment</span>
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Investment Type */}
          <div className="space-y-3">
            <Label>Investment Type</Label>
            <div className="grid grid-cols-1 gap-3">
              {investmentTypeOptions.map((option) => (
                <label
                  key={option.value}
                  className={`flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 ${
                    formData.investmentType === option.value
                      ? "border-blue-500 bg-blue-50"
                      : "border-gray-200"
                  }`}
                >
                  <input
                    type="radio"
                    value={option.value}
                    checked={formData.investmentType === option.value}
                    onChange={(e) =>
                      handleInputChange(
                        "investmentType",
                        e.target.value as InvestmentType
                      )
                    }
                    className="text-blue-600"
                  />
                  <div>
                    <div className="font-medium">{option.label}</div>
                    <div className="text-sm text-gray-600">
                      {option.description}
                    </div>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {/* Investment Amount */}
          <div className="space-y-2">
            <Label htmlFor="amount">Investment Amount *</Label>
            <div className="relative">
              <DollarSignIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <NumericInput
                id="amount"
                value={formData.amount || ""}
                onValueChange={(values) => {
                  handleInputChange("amount", values.floatValue || 0);
                }}
                placeholder="Enter investment amount"
                className={`pl-10 ${
                  validationErrors.amount ? "border-red-500" : ""
                }`}
                decimalScale={2}
                fixedDecimalScale={true}
                isAllowed={(values) => {
                  const { floatValue } = values;
                  return floatValue === undefined || floatValue >= 100;
                }}
              />
            </div>
            {validationErrors.amount && (
              <p className="text-sm text-red-600">{validationErrors.amount}</p>
            )}
            <p className="text-xs text-gray-500">Minimum investment: ₦100</p>
          </div>

          {/* Profit Sharing Model */}
          <div className="space-y-3">
            <Label>Profit Sharing Model</Label>
            <div className="grid grid-cols-1 gap-3">
              {profitSharingOptions.map((option) => (
                <label
                  key={option.value}
                  className={`flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 ${
                    formData.profitSharingModel === option.value
                      ? "border-blue-500 bg-blue-50"
                      : "border-gray-200"
                  }`}
                >
                  <input
                    type="radio"
                    value={option.value}
                    checked={formData.profitSharingModel === option.value}
                    onChange={(e) =>
                      handleInputChange(
                        "profitSharingModel",
                        e.target.value as ProfitSharingModel
                      )
                    }
                    className="text-blue-600"
                  />
                  <div>
                    <div className="font-medium">{option.label}</div>
                    <div className="text-sm text-gray-600">
                      {option.description}
                    </div>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {/* Conditional Fields Based on Profit Sharing Model */}
          {(formData.profitSharingModel === "PROPORTIONAL_BY_AMOUNT" ||
            formData.profitSharingModel === "TIERED") && (
            <div className="space-y-2">
              <Label htmlFor="profitPercentage">
                Expected Profit Percentage (%)
              </Label>
              <NumericInput
                id="profitPercentage"
                value={formData.profitPercentage || ""}
                onValueChange={(values) => {
                  handleInputChange("profitPercentage", values.floatValue || 0);
                }}
                placeholder="Enter expected profit percentage"
                className={
                  validationErrors.profitPercentage ? "border-red-500" : ""
                }
                suffix="%"
                prefix=""
                decimalScale={2}
                allowNegative={false}
                isAllowed={(values) => {
                  const { floatValue } = values;
                  return (
                    floatValue === undefined ||
                    (floatValue >= 0.1 && floatValue <= 100)
                  );
                }}
              />
              {validationErrors.profitPercentage && (
                <p className="text-sm text-red-600">
                  {validationErrors.profitPercentage}
                </p>
              )}
            </div>
          )}

          {formData.profitSharingModel === "FIXED_SHARES" && (
            <div className="space-y-2">
              <Label htmlFor="fixedShares">Number of Fixed Shares</Label>
              <NumericInput
                id="fixedShares"
                value={formData.fixedShares || ""}
                onValueChange={(values) => {
                  handleInputChange(
                    "fixedShares",
                    values.floatValue ? Math.floor(values.floatValue) : 0
                  );
                }}
                placeholder="Enter number of shares"
                className={validationErrors.fixedShares ? "border-red-500" : ""}
                isNumberInput={true}
                allowNegative={false}
                decimalScale={0}
                isAllowed={(values) => {
                  const { floatValue } = values;
                  return floatValue === undefined || floatValue >= 1;
                }}
              />
              {validationErrors.fixedShares && (
                <p className="text-sm text-red-600">
                  {validationErrors.fixedShares}
                </p>
              )}
            </div>
          )}

          {/* Maturity Date */}
          <div className="space-y-2">
            <Label htmlFor="maturityDate">Maturity Date (Optional)</Label>
            <div className="relative">
              <CalendarIcon className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
              <Input
                id="maturityDate"
                type="date"
                value={formatDateForInput(formData.maturityDate)}
                onChange={(e) =>
                  handleInputChange(
                    "maturityDate",
                    e.target.value ? e.target.value + "T00:00:00" : undefined
                  )
                }
                className={`pl-10 ${
                  validationErrors.maturityDate ? "border-red-500" : ""
                }`}
              />
            </div>
            {validationErrors.maturityDate && (
              <p className="text-sm text-red-600">
                {validationErrors.maturityDate}
              </p>
            )}
            <p className="text-xs text-gray-500">
              Leave empty for indefinite investment
            </p>
          </div>

          {/* Investment Notes */}
          <div className="space-y-2">
            <Label htmlFor="notes">Notes (Optional)</Label>
            <Textarea
              id="notes"
              value={formData.notes || ""}
              onChange={(e) => handleInputChange("notes", e.target.value)}
              placeholder="Add any additional notes about your investment..."
              rows={3}
              maxLength={1000}
            />
            <p className="text-xs text-gray-500">
              {formData.notes?.length || 0}/1000 characters
            </p>
          </div>

          {/* Estimated Returns */}
          {formData.amount > 0 && formData.profitPercentage && (
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <div className="flex items-start space-x-2">
                <InfoIcon className="h-5 w-5 text-blue-600 mt-0.5" />
                <div>
                  <h4 className="font-medium text-blue-900">
                    Estimated Returns
                  </h4>
                  <div className="text-sm text-blue-800 mt-1">
                    <p>
                      Investment Amount:{" "}
                      <strong>{formatCurrency(formData.amount)}</strong>
                    </p>
                    <p>
                      Expected Monthly Return:{" "}
                      <strong>{formatCurrency(getEstimatedReturn())}</strong>
                    </p>
                    <p>
                      Annual Return:{" "}
                      <strong>
                        {formatCurrency(getEstimatedReturn() * 12)}
                      </strong>
                    </p>
                  </div>
                  <p className="text-xs text-blue-600 mt-2">
                    * These are estimates based on your profit percentage.
                    Actual returns may vary.
                  </p>
                </div>
              </div>
            </div>
          )}

          <DialogFooter className="space-x-2">
            <Button
              type="button"
              variant="outline"
              onClick={handleClose}
              disabled={isLoading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={isLoading || !formData.amount}
              className="min-w-32"
            >
              {isLoading ? (
                <>
                  <LoadingSpinner size="sm" />
                  <span className="ml-2">Creating...</span>
                </>
              ) : (
                <>
                  <PlusIcon className="h-4 w-4 mr-2" />
                  Create Investment
                </>
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};
