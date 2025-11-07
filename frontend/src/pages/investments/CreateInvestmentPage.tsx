import React, { useState, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { ArrowLeft, ArrowRight, Check, DollarSign, InfoIcon, Loader2 } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
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
import { useAuth } from '@/context/ManualAuthContext'
import { addMonths, format } from 'date-fns'
import { toast } from 'sonner'

// Validation schema matching backend requirements
const investmentSchema = yup.object().shape({
  shopId: yup.string().required('Shop is required'),
  investmentType: yup.string().oneOf(Object.values(InvestmentType)).required('Investment type is required'),
  amount: yup.number()
    .min(1000, 'Minimum investment is ₦1,000')
    .max(1000000, 'Maximum investment is ₦1,000,000')
    .required('Investment amount is required'),
  profitSharingModel: yup.string().required('Profit sharing model is required'),
  profitPercentage: yup.number()
    .transform((value, originalValue) => {
      // Skip validation if empty or not needed
      return originalValue === '' || originalValue === null ? undefined : value
    })
    .nullable()
    .optional()
    .min(0, 'Percentage must be at least 0%')
    .max(100, 'Percentage cannot exceed 100%')
    .when('profitSharingModel', {
      is: (val: string) => ['PROPORTIONAL_BY_AMOUNT', 'TIME_WEIGHTED', 'TIERED'].includes(val),
      then: (schema) => schema.required('Profit percentage is required for this model'),
    }),
  fixedShares: yup.number()
    .transform((value, originalValue) => {
      return originalValue === '' || originalValue === null ? undefined : value
    })
    .nullable()
    .optional()
    .positive('Fixed shares must be positive')
    .when('profitSharingModel', {
      is: 'FIXED_SHARES',
      then: (schema) => schema.required('Fixed shares is required for this model'),
    }),
  duration: yup.number()
    .min(3, 'Minimum duration is 3 months')
    .max(60, 'Maximum duration is 60 months')
    .required('Duration is required'),
  productIds: yup.array().of(yup.string())
    .when('investmentType', {
      is: InvestmentType.PRODUCT_SPECIFIC,
      then: (schema) => schema.min(1, 'At least one product must be selected for product-specific investment').required('Products are required for product-specific investment'),
      otherwise: (schema) => schema.test(
        'no-products-for-shop-wide',
        'Shop-wide investments cannot have product selection',
        function(value) {
          const investmentType = this.parent.investmentType
          if (investmentType === InvestmentType.SHOP_WIDE && value && value.length > 0) {
            return false
          }
          return true
        }
      ),
    }),
  categoryFilter: yup.string()
    .when('investmentType', {
      is: InvestmentType.CATEGORY_SPECIFIC,
      then: (schema) => schema.required('Category is required for category-specific investment').min(1, 'Category cannot be empty'),
      otherwise: (schema) => schema.test(
        'no-category-for-shop-wide',
        'Shop-wide investments cannot have category filter',
        function(value) {
          const investmentType = this.parent.investmentType
          if (investmentType === InvestmentType.SHOP_WIDE && value && value.length > 0) {
            return false
          }
          return true
        }
      ),
    }),
  notes: yup.string().max(1000, 'Notes cannot exceed 1000 characters').optional(),
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
  const { user } = useAuth()
  const [currentStep, setCurrentStep] = useState(1)
  const createInvestment = useCreateInvestment()
  
  // Fetch active shops
  const { data: shops = [], isLoading: shopsLoading } = useActiveShops()
  
  // Get investor ID from current user
  const investorId = user?.id

  const form = useForm<InvestmentFormValues>({
    resolver: yupResolver(investmentSchema),
    defaultValues: {
      shopId: '',
      investmentType: InvestmentType.SHOP_WIDE,
      amount: 0,
      profitSharingModel: 'PROPORTIONAL_BY_AMOUNT' as any,
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
  
  // Get selected shop for display in review section
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
    if (!investorId) {
      toast.error('User ID not found. Please log in again.')
      console.error('User ID not found')
      return
    }

    try {
      const maturityDate = investmentPreview.maturityDate
        ? investmentPreview.maturityDate.toISOString()
        : undefined

      // Build payload with only defined optional fields (exactOptionalPropertyTypes compliance)
      const payload: InvestmentCreateRequest = {
        investorId, // Add investor ID from current user
        shopId: data.shopId,
        investmentType: data.investmentType as InvestmentType,
        amount: data.amount,
        profitSharingModel: data.profitSharingModel as any, // Allow custom values
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
      toast.success('Investment created successfully!')
      navigate('/investments')
    } catch (error: any) {
      console.error('Failed to create investment:', error)
      const errorMessage = error?.response?.data?.message || error?.message || 'Failed to create investment'
      toast.error(errorMessage)
    }
  }

  // Handle form errors and show toast
  const onError = (errors: any) => {
    console.log('Form validation errors:', errors)
    
    // Get first error message
    const firstError = Object.values(errors)[0] as any
    const errorMessage = firstError?.message || 'Please fix the form errors'
    
    toast.error(errorMessage)
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
      <form onSubmit={handleSubmit(onSubmit, onError)}>
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
                  value={shopId || undefined}
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
                <div className="grid grid-cols-1 gap-3">
                  <label
                    className={`flex items-center space-x-3 p-4 border rounded-lg cursor-pointer hover:bg-gray-50 transition ${
                      investmentType === InvestmentType.SHOP_WIDE ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
                    }`}
                  >
                    <input
                      type="radio"
                      value={InvestmentType.SHOP_WIDE}
                      checked={investmentType === InvestmentType.SHOP_WIDE}
                      onChange={(e) => {
                        setValue('investmentType', e.target.value as InvestmentType)
                        setValue('productIds', []) // Clear products
                        setValue('categoryFilter', '') // Clear category
                      }}
                      className="text-blue-600"
                    />
                    <div className="flex-1">
                      <div className="font-medium">Shop-Wide Investment</div>
                      <div className="text-sm text-muted-foreground">
                        Invest in the entire shop performance
                      </div>
                    </div>
                  </label>
                  <label
                    className={`flex items-center space-x-3 p-4 border rounded-lg cursor-pointer hover:bg-gray-50 transition ${
                      investmentType === InvestmentType.PRODUCT_SPECIFIC ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
                    }`}
                  >
                    <input
                      type="radio"
                      value={InvestmentType.PRODUCT_SPECIFIC}
                      checked={investmentType === InvestmentType.PRODUCT_SPECIFIC}
                      onChange={(e) => {
                        setValue('investmentType', e.target.value as InvestmentType)
                        setValue('categoryFilter', '') // Clear category
                      }}
                      className="text-blue-600"
                    />
                    <div className="flex-1">
                      <div className="font-medium">Product-Specific</div>
                      <div className="text-sm text-muted-foreground">
                        Invest in specific products
                      </div>
                    </div>
                  </label>
                  <label
                    className={`flex items-center space-x-3 p-4 border rounded-lg cursor-pointer hover:bg-gray-50 transition ${
                      investmentType === InvestmentType.CATEGORY_SPECIFIC ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
                    }`}
                  >
                    <input
                      type="radio"
                      value={InvestmentType.CATEGORY_SPECIFIC}
                      checked={investmentType === InvestmentType.CATEGORY_SPECIFIC}
                      onChange={(e) => {
                        setValue('investmentType', e.target.value as InvestmentType)
                        setValue('productIds', []) // Clear products
                      }}
                      className="text-blue-600"
                    />
                    <div className="flex-1">
                      <div className="font-medium">Category-Specific</div>
                      <div className="text-sm text-muted-foreground">
                        Invest in specific product categories
                      </div>
                    </div>
                  </label>
                </div>
              </div>

              {/* Investment Amount */}
              <div className="space-y-2">
                <Label htmlFor="amount">Investment Amount *</Label>
                <div className="relative">
                  <DollarSign className="absolute left-3 top-3 h-4 w-4 text-gray-400" />
                  <Input
                    id="amount"
                    type="number"
                    min="100"
                    step="0.01"
                    placeholder="50000"
                    className="pl-10"
                    {...register('amount', { valueAsNumber: true })}
                  />
                </div>
                <p className="text-sm text-muted-foreground">Minimum: ₦1,000</p>
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
              <div className="space-y-3">
                <Label>Profit Sharing Model</Label>
                <div className="grid grid-cols-1 gap-3">
                  <label
                    className={`flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 ${
                      watch('profitSharingModel') === 'PROPORTIONAL_BY_AMOUNT' ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
                    }`}
                  >
                    <input
                      type="radio"
                      value="PROPORTIONAL_BY_AMOUNT"
                      checked={watch('profitSharingModel') === 'PROPORTIONAL_BY_AMOUNT'}
                      onChange={(e) => {
                        setValue('profitSharingModel', e.target.value as any)
                        setValue('fixedShares', undefined) // Clear fixed shares
                      }}
                      className="text-blue-600"
                    />
                    <div>
                      <div className="font-medium">Proportional by Amount</div>
                      <div className="text-sm text-gray-600">Profits shared based on investment amount</div>
                    </div>
                  </label>
                  <label
                    className={`flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 ${
                      watch('profitSharingModel') === 'FIXED_SHARES' ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
                    }`}
                  >
                    <input
                      type="radio"
                      value="FIXED_SHARES"
                      checked={watch('profitSharingModel') === 'FIXED_SHARES'}
                      onChange={(e) => {
                        setValue('profitSharingModel', e.target.value as any)
                        setValue('profitPercentage', undefined) // Clear percentage
                      }}
                      className="text-blue-600"
                    />
                    <div>
                      <div className="font-medium">Fixed Shares</div>
                      <div className="text-sm text-gray-600">Fixed number of profit shares</div>
                    </div>
                  </label>
                  <label
                    className={`flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 ${
                      watch('profitSharingModel') === 'TIME_WEIGHTED' ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
                    }`}
                  >
                    <input
                      type="radio"
                      value="TIME_WEIGHTED"
                      checked={watch('profitSharingModel') === 'TIME_WEIGHTED'}
                      onChange={(e) => {
                        setValue('profitSharingModel', e.target.value as any)
                        setValue('fixedShares', undefined) // Clear fixed shares
                      }}
                      className="text-blue-600"
                    />
                    <div>
                      <div className="font-medium">Time-Weighted</div>
                      <div className="text-sm text-gray-600">Profits based on investment duration</div>
                    </div>
                  </label>
                  <label
                    className={`flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 ${
                      watch('profitSharingModel') === 'TIERED' ? 'border-blue-500 bg-blue-50' : 'border-gray-200'
                    }`}
                  >
                    <input
                      type="radio"
                      value="TIERED"
                      checked={watch('profitSharingModel') === 'TIERED'}
                      onChange={(e) => {
                        setValue('profitSharingModel', e.target.value as any)
                        setValue('fixedShares', undefined) // Clear fixed shares
                      }}
                      className="text-blue-600"
                    />
                    <div>
                      <div className="font-medium">Tiered System</div>
                      <div className="text-sm text-gray-600">Different rates for different tiers</div>
                    </div>
                  </label>
                </div>
              </div>

              {/* Conditional Fields Based on Profit Sharing Model */}
              {(watch('profitSharingModel') === 'PROPORTIONAL_BY_AMOUNT' || watch('profitSharingModel') === 'TIERED') && (
                <div className="space-y-2">
                  <Label htmlFor="profitPercentage">Expected Profit Percentage (%)</Label>
                  <Input
                    id="profitPercentage"
                    type="number"
                    min="0.1"
                    max="100"
                    step="0.1"
                    placeholder="Enter expected profit percentage"
                    {...register('profitPercentage', { valueAsNumber: true })}
                  />
                  {errors.profitPercentage && (
                    <p className="text-sm text-red-600">{errors.profitPercentage.message}</p>
                  )}
                </div>
              )}

              {watch('profitSharingModel') === 'FIXED_SHARES' && (
                <div className="space-y-2">
                  <Label htmlFor="fixedShares">Number of Fixed Shares</Label>
                  <Input
                    id="fixedShares"
                    type="number"
                    min="1"
                    step="1"
                    placeholder="Enter number of shares"
                    {...register('fixedShares', { valueAsNumber: true })}
                  />
                  {errors.fixedShares && (
                    <p className="text-sm text-red-600">{errors.fixedShares.message}</p>
                  )}
                </div>
              )}

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

              {/* Estimated Returns Preview */}
              {amount > 0 && profitPercentage && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                  <div className="flex items-start space-x-2">
                    <InfoIcon className="h-5 w-5 text-blue-600 mt-0.5" />
                    <div className="flex-1">
                      <h4 className="font-medium text-blue-900">Estimated Returns</h4>
                      <div className="text-sm text-blue-800 mt-1 space-y-1">
                        <p>Investment Amount: <strong>{formatCurrency(amount)}</strong></p>
                        <p>Expected Monthly Return: <strong>{formatCurrency(investmentPreview.monthlyExpectedReturn.average)}</strong></p>
                        <p>Expected Annual Return: <strong>{formatCurrency(investmentPreview.monthlyExpectedReturn.average * 12)}</strong></p>
                        {investmentPreview.maturityDate && (
                          <p>Maturity Date: <strong>{format(investmentPreview.maturityDate, 'MMMM dd, yyyy')}</strong></p>
                        )}
                      </div>
                      <p className="text-xs text-blue-600 mt-2">
                        * These are estimates based on your profit percentage. Actual returns may vary.
                      </p>
                    </div>
                  </div>
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
              <div className="space-y-3">
                <Label>Product Scope *</Label>
                <div className="grid grid-cols-1 gap-3">
                  <label className="flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 transition border-blue-500 bg-blue-50">
                    <input
                      type="radio"
                      value="all"
                      defaultChecked
                      className="text-blue-600"
                    />
                    <div className="flex-1">
                      <div className="font-medium">All Products</div>
                      <div className="text-sm text-muted-foreground">
                        Investment applies to all products in the shop
                      </div>
                    </div>
                  </label>
                  <label className="flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 transition border-gray-200">
                    <input
                      type="radio"
                      value="specific"
                      className="text-blue-600"
                      disabled
                    />
                    <div className="flex-1">
                      <div className="font-medium">Specific Products</div>
                      <div className="text-sm text-muted-foreground">
                        Choose specific products (Coming soon)
                      </div>
                    </div>
                  </label>
                  <label className="flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50 transition border-gray-200">
                    <input
                      type="radio"
                      value="category"
                      className="text-blue-600"
                      disabled
                    />
                    <div className="flex-1">
                      <div className="font-medium">Product Category</div>
                      <div className="text-sm text-muted-foreground">
                        Filter by category (Coming soon)
                      </div>
                    </div>
                  </label>
                </div>
              </div>

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
                  <Button 
                    type="submit" 
                    disabled={isSubmitting || createInvestment.isPending}
                    className="min-w-[160px]"
                  >
                    {(isSubmitting || createInvestment.isPending) ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Submitting...
                      </>
                    ) : (
                      'Submit Investment'
                    )}
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
