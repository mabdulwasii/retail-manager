import React, { useState, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { ArrowLeft, ArrowRight, Check } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { RadioGroup, RadioGroupItem } from '@/components/ui/radio-group'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Checkbox } from '@/components/ui/checkbox'
import { useCreateInvestment } from '@/hooks/investment/useInvestmentMutations'
import { InvestmentType, ProfitSharingModel, InvestmentCreateRequest } from '@/types/investment'
import { useCurrency } from '@/hooks/useCurrency'
import { useActiveShops } from '@/hooks/useShops'
import { addMonths, format } from 'date-fns'

// Validation schema
const investmentSchema = yup.object().shape({
  shopId: yup.string().required('Shop is required'),
  investmentType: yup.string().oneOf(Object.values(InvestmentType)).required(),
  amount: yup.number()
    .min(1000, 'Minimum investment is $1,000')
    .max(1000000, 'Maximum investment is $1,000,000')
    .required(),
  profitSharingModel: yup.string().oneOf(Object.values(ProfitSharingModel)).required(),
  profitPercentage: yup.number()
    .min(1, 'Minimum share is 1%')
    .max(50, 'Maximum share is 50%')
    .optional(),
  fixedShares: yup.number().positive().optional(),
  duration: yup.number()
    .min(3, 'Minimum duration is 3 months')
    .max(60, 'Maximum duration is 60 months')
    .required(),
  productIds: yup.array().of(yup.string()).optional(),
  categoryFilter: yup.string().optional(),
  notes: yup.string().max(1000).optional(),
  agreedToTerms: yup.boolean()
    .oneOf([true], 'You must agree to the terms')
    .required('You must agree to the terms'),
})

type InvestmentFormValues = yup.InferType<typeof investmentSchema>

const STEPS = [
  { id: 1, title: 'Basic Information', description: 'Investment details' },
  { id: 2, title: 'Terms & Conditions', description: 'Profit sharing terms' },
  { id: 3, title: 'Product Selection', description: 'Optional filters' },
  { id: 4, title: 'Review & Submit', description: 'Confirm investment' },
]

export const CreateInvestmentPage: React.FC = () => {
  const navigate = useNavigate()
  const { formatCurrency } = useCurrency()
  const [currentStep, setCurrentStep] = useState(1)
  const createInvestment = useCreateInvestment()
  
  // Fetch active shops
  const { data: shops = [], isLoading: shopsLoading } = useActiveShops()

  const form = useForm<InvestmentFormValues>({
    resolver: yupResolver(investmentSchema),
    defaultValues: {
      shopId: '',
      investmentType: InvestmentType.SHOP_WIDE,
      amount: 0,
      profitSharingModel: ProfitSharingModel.PROPORTIONAL_BY_AMOUNT,
      profitPercentage: 15,
      duration: 24,
      productIds: [],
      notes: '',
      agreedToTerms: false,
    },
  })

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = form

  // Watch form values for preview
  const amount = watch('amount')
  const profitPercentage = watch('profitPercentage') || 0
  const duration = watch('duration') || 0
  const investmentType = watch('investmentType')
  const shopId = watch('shopId')
  
  // Get selected shop name
  const selectedShop = shops.find(shop => shop.id === shopId)

  // Calculate investment preview
  const investmentPreview = useMemo(() => {
    const monthlyExpectedReturn = amount * (profitPercentage / 100) * 0.1 // Estimate
    const totalExpectedReturn = monthlyExpectedReturn * duration
    const expectedROI = amount > 0 ? (totalExpectedReturn / amount) * 100 : 0

    return {
      monthlyExpectedReturn: {
        min: monthlyExpectedReturn * 0.8,
        max: monthlyExpectedReturn * 1.2,
        average: monthlyExpectedReturn,
      },
      totalExpectedReturn,
      expectedROI: {
        min: expectedROI * 0.8,
        max: expectedROI * 1.2,
        average: expectedROI,
      },
      maturityDate: duration > 0 ? addMonths(new Date(), duration) : null,
    }
  }, [amount, profitPercentage, duration])

  const onSubmit = async (data: InvestmentFormValues) => {
    try {
      const maturityDate = investmentPreview.maturityDate
        ? investmentPreview.maturityDate.toISOString()
        : undefined

      // Build payload with only defined optional fields (exactOptionalPropertyTypes compliance)
      const payload: InvestmentCreateRequest = {
        shopId: data.shopId,
        investmentType: data.investmentType,
        amount: data.amount,
        profitSharingModel: data.profitSharingModel,
      }

      // Conditionally add optional fields only if they have values
      if (data.profitPercentage !== undefined) {
        payload.profitPercentage = data.profitPercentage
      }
      if (data.fixedShares !== undefined) {
        payload.fixedShares = data.fixedShares
      }
      if (maturityDate !== undefined) {
        payload.maturityDate = maturityDate
      }
      if (data.productIds && data.productIds.length > 0) {
        const filteredIds = data.productIds.filter((id): id is string => !!id)
        if (filteredIds.length > 0) {
          payload.productIds = filteredIds
        }
      }
      if (data.categoryFilter) {
        payload.categoryFilter = data.categoryFilter
      }
      if (data.notes) {
        payload.notes = data.notes
      }

      await createInvestment.mutateAsync(payload)

      navigate('/investments')
    } catch (error) {
      console.error('Failed to create investment:', error)
    }
  }

  const handleNext = () => {
    if (currentStep < STEPS.length) {
      setCurrentStep(currentStep + 1)
    }
  }

  const handleBack = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1)
    }
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" onClick={() => navigate('/investments')}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Create New Investment</h1>
          <p className="text-muted-foreground mt-1">
            Follow the steps to create your investment
          </p>
        </div>
      </div>

      {/* Progress Steps */}
      <div className="flex items-center justify-between">
        {STEPS.map((step, index) => (
          <React.Fragment key={step.id}>
            <div className="flex flex-col items-center flex-1">
              <div
                className={`
                  w-10 h-10 rounded-full flex items-center justify-center font-semibold
                  ${
                    currentStep > step.id
                      ? 'bg-green-500 text-white'
                      : currentStep === step.id
                      ? 'bg-primary text-white'
                      : 'bg-gray-200 text-gray-600'
                  }
                `}
              >
                {currentStep > step.id ? <Check className="h-5 w-5" /> : step.id}
              </div>
              <div className="text-center mt-2">
                <p className="text-sm font-medium">{step.title}</p>
                <p className="text-xs text-muted-foreground">{step.description}</p>
              </div>
            </div>
            {index < STEPS.length - 1 && (
              <div className="flex-1 h-0.5 bg-gray-200 mb-8" />
            )}
          </React.Fragment>
        ))}
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit(onSubmit)}>
        {/* Step 1: Basic Information */}
        {currentStep === 1 && (
          <Card>
            <CardHeader>
              <CardTitle>Basic Information</CardTitle>
              <CardDescription>Enter the fundamental investment details</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Shop Selection */}
              <div className="space-y-2">
                <Label htmlFor="shopId">Shop *</Label>
                <Select
                  value={watch('shopId')}
                  onValueChange={(value) => setValue('shopId', value)}
                  disabled={shopsLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder={shopsLoading ? "Loading shops..." : "Select a shop"} />
                  </SelectTrigger>
                  <SelectContent>
                    {shops.length === 0 && !shopsLoading ? (
                      <div className="px-2 py-4 text-sm text-muted-foreground text-center">
                        No active shops available
                      </div>
                    ) : (
                      shops.map((shop) => (
                        <SelectItem key={shop.id} value={shop.id}>
                          {shop.name}
                        </SelectItem>
                      ))
                    )}
                  </SelectContent>
                </Select>
                {errors.shopId && (
                  <p className="text-sm text-red-600">{errors.shopId.message}</p>
                )}
              </div>

              {/* Investment Type */}
              <div className="space-y-3">
                <Label>Investment Type *</Label>
                <RadioGroup
                  value={investmentType}
                  onValueChange={(value) => setValue('investmentType', value as InvestmentType)}
                >
                  <div className="flex items-center space-x-2 border rounded-lg p-4">
                    <RadioGroupItem value={InvestmentType.SHOP_WIDE} id="shop_wide" />
                    <Label htmlFor="shop_wide" className="cursor-pointer flex-1">
                      <div className="font-medium">Shop-Wide Investment</div>
                      <div className="text-sm text-muted-foreground">
                        Investment applies to all products and sales in the shop
                      </div>
                    </Label>
                  </div>
                  <div className="flex items-center space-x-2 border rounded-lg p-4">
                    <RadioGroupItem value={InvestmentType.PRODUCT_SPECIFIC} id="product_specific" />
                    <Label htmlFor="product_specific" className="cursor-pointer flex-1">
                      <div className="font-medium">Product-Specific</div>
                      <div className="text-sm text-muted-foreground">
                        Investment limited to specific products
                      </div>
                    </Label>
                  </div>
                  <div className="flex items-center space-x-2 border rounded-lg p-4">
                    <RadioGroupItem value={InvestmentType.CATEGORY_BASED} id="category_based" />
                    <Label htmlFor="category_based" className="cursor-pointer flex-1">
                      <div className="font-medium">Category-Based</div>
                      <div className="text-sm text-muted-foreground">
                        Investment applies to a specific product category
                      </div>
                    </Label>
                  </div>
                </RadioGroup>
              </div>

              {/* Investment Amount */}
              <div className="space-y-2">
                <Label htmlFor="amount">Investment Amount *</Label>
                <div className="relative">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                    $
                  </span>
                  <Input
                    id="amount"
                    type="number"
                    placeholder="50000"
                    className="pl-7"
                    {...register('amount', { valueAsNumber: true })}
                  />
                </div>
                <p className="text-sm text-muted-foreground">Min: $1,000 | Max: $1,000,000</p>
                {errors.amount && (
                  <p className="text-sm text-red-600">{errors.amount.message}</p>
                )}
              </div>

              <div className="flex justify-end gap-2">
                <Button type="button" onClick={handleNext}>
                  Continue
                  <ArrowRight className="h-4 w-4 ml-2" />
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Step 2: Terms & Conditions */}
        {currentStep === 2 && (
          <Card>
            <CardHeader>
              <CardTitle>Terms & Conditions</CardTitle>
              <CardDescription>Define your profit sharing terms</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Profit Sharing Model */}
              <div className="space-y-2">
                <Label htmlFor="profitSharingModel">Profit Sharing Model *</Label>
                <Select
                  value={watch('profitSharingModel')}
                  onValueChange={(value) =>
                    setValue('profitSharingModel', value as ProfitSharingModel)
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={ProfitSharingModel.PROPORTIONAL_BY_AMOUNT}>
                      Proportional by Amount
                    </SelectItem>
                    <SelectItem value={ProfitSharingModel.FIXED_PERCENTAGE}>
                      Fixed Percentage
                    </SelectItem>
                    <SelectItem value={ProfitSharingModel.FIXED_AMOUNT}>Fixed Amount</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Profit Percentage */}
              <div className="space-y-2">
                <Label htmlFor="profitPercentage">Profit Share Percentage *</Label>
                <div className="flex items-center gap-4">
                  <Input
                    id="profitPercentage"
                    type="number"
                    placeholder="15"
                    {...register('profitPercentage', { valueAsNumber: true })}
                  />
                  <span className="text-2xl font-semibold">%</span>
                </div>
                <p className="text-sm text-muted-foreground">Range: 1% - 50%</p>
                {errors.profitPercentage && (
                  <p className="text-sm text-red-600">{errors.profitPercentage.message}</p>
                )}
              </div>

              {/* Duration */}
              <div className="space-y-2">
                <Label htmlFor="duration">Investment Duration *</Label>
                <div className="flex items-center gap-4">
                  <Input
                    id="duration"
                    type="number"
                    placeholder="24"
                    {...register('duration', { valueAsNumber: true })}
                  />
                  <span className="text-lg font-medium">months</span>
                </div>
                <p className="text-sm text-muted-foreground">Range: 3 - 60 months</p>
                {errors.duration && (
                  <p className="text-sm text-red-600">{errors.duration.message}</p>
                )}
              </div>

              {/* Maturity Date Preview */}
              {investmentPreview.maturityDate && (
                <div className="bg-muted rounded-lg p-4">
                  <p className="text-sm font-medium mb-1">Maturity Date</p>
                  <p className="text-lg font-semibold">
                    {format(investmentPreview.maturityDate, 'MMMM dd, yyyy')}
                  </p>
                </div>
              )}

              <div className="flex justify-between gap-2">
                <Button type="button" variant="outline" onClick={handleBack}>
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back
                </Button>
                <Button type="button" onClick={handleNext}>
                  Continue
                  <ArrowRight className="h-4 w-4 ml-2" />
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Step 3: Product Selection */}
        {currentStep === 3 && (
          <Card>
            <CardHeader>
              <CardTitle>Product Selection (Optional)</CardTitle>
              <CardDescription>Choose which products this investment applies to</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <RadioGroup defaultValue="all">
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="all" id="all" />
                  <Label htmlFor="all">All Products</Label>
                </div>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="specific" id="specific" />
                  <Label htmlFor="specific">Specific Products</Label>
                </div>
                <div className="flex items-center space-x-2">
                  <RadioGroupItem value="category" id="category" />
                  <Label htmlFor="category">Product Category</Label>
                </div>
              </RadioGroup>

              {/* Notes */}
              <div className="space-y-2">
                <Label htmlFor="notes">Notes (Optional)</Label>
                <Textarea
                  id="notes"
                  placeholder="Add any additional notes or terms..."
                  rows={4}
                  {...register('notes')}
                />
                <p className="text-sm text-muted-foreground">Maximum 1000 characters</p>
              </div>

              <div className="flex justify-between gap-2">
                <Button type="button" variant="outline" onClick={handleBack}>
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back
                </Button>
                <Button type="button" onClick={handleNext}>
                  Continue
                  <ArrowRight className="h-4 w-4 ml-2" />
                </Button>
              </div>
            </CardContent>
          </Card>
        )}

        {/* Step 4: Review & Submit */}
        {currentStep === 4 && (
          <Card>
            <CardHeader>
              <CardTitle>Review & Submit</CardTitle>
              <CardDescription>Please review your investment details</CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Investment Summary */}
              <div className="border rounded-lg p-6 space-y-4">
                <h3 className="font-semibold text-lg">Investment Summary</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-muted-foreground">Shop</p>
                    <p className="font-medium">{selectedShop?.name || 'Not selected'}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Investment Type</p>
                    <p className="font-medium">{investmentType.replace('_', ' ')}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Amount</p>
                    <p className="font-medium">{formatCurrency(amount)}</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Profit Share</p>
                    <p className="font-medium">{profitPercentage}%</p>
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Duration</p>
                    <p className="font-medium">{duration} months</p>
                  </div>
                  {investmentPreview.maturityDate && (
                    <div className="col-span-2">
                      <p className="text-sm text-muted-foreground">Maturity Date</p>
                      <p className="font-medium">
                        {format(investmentPreview.maturityDate, 'MMMM dd, yyyy')}
                      </p>
                    </div>
                  )}
                </div>
              </div>

              {/* Expected Returns */}
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-6 space-y-3">
                <h3 className="font-semibold text-blue-900">Expected Returns (Estimated)</h3>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-blue-700">Monthly Return</p>
                    <p className="font-semibold text-blue-900">
                      {formatCurrency(investmentPreview.monthlyExpectedReturn.average)}
                    </p>
                    <p className="text-xs text-blue-600">
                      Range: {formatCurrency(investmentPreview.monthlyExpectedReturn.min)} -{' '}
                      {formatCurrency(investmentPreview.monthlyExpectedReturn.max)}
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-blue-700">Expected ROI</p>
                    <p className="font-semibold text-blue-900">
                      {investmentPreview.expectedROI.average.toFixed(1)}%
                    </p>
                    <p className="text-xs text-blue-600">
                      Range: {investmentPreview.expectedROI.min.toFixed(1)}% -{' '}
                      {investmentPreview.expectedROI.max.toFixed(1)}%
                    </p>
                  </div>
                </div>
              </div>

              {/* Terms Agreement */}
              <div className="space-y-4">
                <div className="flex items-start space-x-2">
                  <Checkbox
                    id="agreedToTerms"
                    checked={watch('agreedToTerms')}
                    onCheckedChange={(checked) => setValue('agreedToTerms', checked as boolean)}
                  />
                  <Label htmlFor="agreedToTerms" className="cursor-pointer text-sm">
                    I agree to the investment terms and conditions. I understand that returns are not
                    guaranteed and may vary based on business performance.
                  </Label>
                </div>
                {errors.agreedToTerms && (
                  <p className="text-sm text-red-600">{errors.agreedToTerms.message}</p>
                )}
              </div>

              <div className="flex justify-between gap-2">
                <Button type="button" variant="outline" onClick={handleBack}>
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back
                </Button>
                <div className="flex gap-2">
                  <Button type="button" variant="outline">
                    Save as Draft
                  </Button>
                  <Button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? 'Submitting...' : 'Submit Investment'}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        )}
      </form>
    </div>
  )
}
