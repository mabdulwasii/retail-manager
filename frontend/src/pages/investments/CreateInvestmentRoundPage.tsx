import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ArrowLeft, ArrowRight, Plus, Trash2, Check } from 'lucide-react'
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
import { useAuth } from '@/context/ManualAuthContext'
import { useCreateInvestmentRound } from '@/hooks/investment/useInvestmentRounds'
import { InvestmentType, ProfitSharingModel, InvestmentRoundInvestor } from '@/types/investment'
import { useCurrency } from '@/hooks/useCurrency'
import { useActiveShops } from '@/hooks/useShops'
import { useShopUsers } from '@/hooks/useUsers'
import { toast } from 'sonner'

const STEPS = [
  { id: 1, title: 'Round Information', description: 'Basic details' },
  { id: 2, title: 'Profit Model', description: 'Configure sharing' },
  { id: 3, title: 'Add Investors', description: 'Select investors' },
  { id: 4, title: 'Review', description: 'Confirm details' },
]

export const CreateInvestmentRoundPage: React.FC = () => {
  const navigate = useNavigate()
  const { user } = useAuth()
  const { formatCurrency } = useCurrency()
  const createRound = useCreateInvestmentRound()

  const { data: shops = [] } = useActiveShops()

  const [currentStep, setCurrentStep] = useState(1)
  const [formData, setFormData] = useState({
    shopId: user?.shopId || '',
    investmentType: InvestmentType.SHOP_WIDE,
    profitSharingModel: ProfitSharingModel.PROPORTIONAL_BY_AMOUNT,
    maturityMonths: 24,
    notes: '',
    // Time weighting
    timeWeightBaseYears: 1.0,
    timeWeightBaseMultiplier: 1.0,
    timeWeightYear2Threshold: 2.0,
    timeWeightYear2Multiplier: 1.2,
    timeWeightYear3Threshold: 3.0,
    timeWeightYear3Multiplier: 1.5,
    timeWeightMaxMultiplier: 2.0,
    // Tiered
    tier1Threshold: 0,
    tier1Multiplier: 1.0,
    tier2Threshold: 50000,
    tier2Multiplier: 1.1,
    tier3Threshold: 100000,
    tier3Multiplier: 1.2,
  })

  const [investors, setInvestors] = useState<InvestmentRoundInvestor[]>([
    { investorId: '', amount: 0, notes: '' },
  ])

  const selectedShop = shops.find(s => s.id === formData.shopId)

  // Fetch shop users for investor selection
  const { data: shopUsers = [], isLoading: usersLoading } = useShopUsers({
    shopId: formData.shopId,
    status: 'ACTIVE',
    enabled: !!formData.shopId,
  })

  const handleInputChange = (field: string, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const handleAddInvestor = () => {
    setInvestors([...investors, { investorId: '', amount: 0, notes: '' }])
  }

  const handleRemoveInvestor = (index: number) => {
    if (investors.length > 1) {
      setInvestors(investors.filter((_, i) => i !== index))
    }
  }

  const handleInvestorChange = (index: number, field: keyof InvestmentRoundInvestor, value: any) => {
    const updated = [...investors]
    updated[index] = { ...updated[index], [field]: value }
    setInvestors(updated)
  }

  const validateStep = (step: number): boolean => {
    switch (step) {
      case 1:
        if (!formData.shopId) {
          toast.error('Please select a shop')
          return false
        }
        return true
      case 2:
        return true
      case 3:
        const validInvestors = investors.filter(inv => inv.investorId && inv.amount > 0)
        if (validInvestors.length === 0) {
          toast.error('Please add at least one investor with valid amount')
          return false
        }
        if (formData.profitSharingModel === ProfitSharingModel.FIXED_SHARES) {
          const hasShares = investors.every(inv => inv.fixedShares && inv.fixedShares > 0)
          if (!hasShares) {
            toast.error('Fixed shares required for all investors in FIXED_SHARES model')
            return false
          }
        }
        return true
      default:
        return true
    }
  }

  const handleNext = () => {
    if (validateStep(currentStep)) {
      setCurrentStep(prev => Math.min(prev + 1, STEPS.length))
    }
  }

  const handleBack = () => {
    setCurrentStep(prev => Math.max(prev - 1, 1))
  }

  const handleSubmit = async () => {
    if (!validateStep(3)) return

    const maturityDate = new Date()
    maturityDate.setMonth(maturityDate.getMonth() + formData.maturityMonths)

    const payload: any = {
      shopId: formData.shopId,
      investmentType: formData.investmentType,
      profitSharingModel: formData.profitSharingModel,
      maturityDate: maturityDate.toISOString(),
      notes: formData.notes || undefined,
      investors: investors.filter(inv => inv.investorId && inv.amount > 0),
    }

    // Add time weighting rules if applicable
    if (formData.profitSharingModel === ProfitSharingModel.TIME_WEIGHTED) {
      payload.timeWeightingRules = {
        baseYears: formData.timeWeightBaseYears,
        baseMultiplier: formData.timeWeightBaseMultiplier,
        year2Threshold: formData.timeWeightYear2Threshold,
        year2Multiplier: formData.timeWeightYear2Multiplier,
        year3Threshold: formData.timeWeightYear3Threshold,
        year3Multiplier: formData.timeWeightYear3Multiplier,
        maxMultiplier: formData.timeWeightMaxMultiplier,
      }
    }

    // Add tier configuration if applicable
    if (formData.profitSharingModel === ProfitSharingModel.TIERED) {
      payload.tierConfiguration = {
        tier1Threshold: formData.tier1Threshold,
        tier1Multiplier: formData.tier1Multiplier,
        tier2Threshold: formData.tier2Threshold,
        tier2Multiplier: formData.tier2Multiplier,
        tier3Threshold: formData.tier3Threshold,
        tier3Multiplier: formData.tier3Multiplier,
      }
    }

    try {
      await createRound.mutateAsync({ shopId: formData.shopId, request: payload })
      navigate('/investments/rounds')
    } catch (error) {
      console.error('Failed to create round:', error)
    }
  }

  const totalAmount = investors.reduce((sum, inv) => sum + (inv.amount || 0), 0)
  const totalShares = investors.reduce((sum, inv) => sum + (inv.fixedShares || 0), 0)

  return (
    <div className="space-y-6 p-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="sm" onClick={() => navigate('/investments/rounds')}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
        <div>
          <h1 className="text-3xl font-bold">Create Investment Round</h1>
          <p className="text-muted-foreground mt-1">Set up a new investment round with multiple investors</p>
        </div>
      </div>

      {/* Progress Steps */}
      <div className="flex items-center justify-between">
        {STEPS.map((step, index) => (
          <React.Fragment key={step.id}>
            <div className="flex items-center">
              <div
                className={`flex items-center justify-center w-10 h-10 rounded-full border-2 ${
                  currentStep >= step.id
                    ? 'bg-primary border-primary text-primary-foreground'
                    : 'border-gray-300 text-gray-400'
                }`}
              >
                {currentStep > step.id ? <Check className="h-5 w-5" /> : step.id}
              </div>
              <div className="ml-3">
                <p className={`text-sm font-medium ${currentStep >= step.id ? 'text-primary' : 'text-gray-500'}`}>
                  {step.title}
                </p>
                <p className="text-xs text-muted-foreground">{step.description}</p>
              </div>
            </div>
            {index < STEPS.length - 1 && (
              <div className={`flex-1 h-0.5 mx-4 ${currentStep > step.id ? 'bg-primary' : 'bg-gray-300'}`} />
            )}
          </React.Fragment>
        ))}
      </div>

      {/* Step Content */}
      <Card>
        <CardContent className="pt-6">
          {currentStep === 1 && (
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="shopId">Shop *</Label>
                <Select
                  value={formData.shopId}
                  onValueChange={(value) => {
                    handleInputChange('shopId', value)
                    // Reset investors when shop changes
                    setInvestors([{ investorId: '', amount: 0, notes: '' }])
                  }}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select shop" />
                  </SelectTrigger>
                  <SelectContent>
                    {shops.map(shop => (
                      <SelectItem key={shop.id} value={shop.id}>
                        {shop.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="investmentType">Investment Type *</Label>
                <Select
                  value={formData.investmentType}
                  onValueChange={(value) => handleInputChange('investmentType', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={InvestmentType.SHOP_WIDE}>Shop-Wide</SelectItem>
                    <SelectItem value={InvestmentType.PRODUCT_SPECIFIC}>Product-Specific</SelectItem>
                    <SelectItem value={InvestmentType.CATEGORY_SPECIFIC}>Category-Specific</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label htmlFor="maturityMonths">Maturity Duration (months) *</Label>
                <Input
                  id="maturityMonths"
                  type="number"
                  value={formData.maturityMonths}
                  onChange={(e) => handleInputChange('maturityMonths', parseInt(e.target.value) || 0)}
                  min={3}
                  max={60}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="notes">Notes</Label>
                <Textarea
                  id="notes"
                  value={formData.notes}
                  onChange={(e) => handleInputChange('notes', e.target.value)}
                  placeholder="Optional notes about this investment round..."
                  rows={3}
                />
              </div>
            </div>
          )}

          {currentStep === 2 && (
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="profitSharingModel">Profit Sharing Model *</Label>
                <Select
                  value={formData.profitSharingModel}
                  onValueChange={(value) => handleInputChange('profitSharingModel', value)}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={ProfitSharingModel.PROPORTIONAL_BY_AMOUNT}>
                      Proportional by Amount
                    </SelectItem>
                    <SelectItem value={ProfitSharingModel.FIXED_SHARES}>Fixed Shares</SelectItem>
                    <SelectItem value={ProfitSharingModel.TIME_WEIGHTED}>Time Weighted</SelectItem>
                    <SelectItem value={ProfitSharingModel.TIERED}>Tiered</SelectItem>
                  </SelectContent>
                </Select>
                <p className="text-xs text-muted-foreground">
                  {formData.profitSharingModel === ProfitSharingModel.PROPORTIONAL_BY_AMOUNT &&
                    'Profits shared based on investment amount percentage'}
                  {formData.profitSharingModel === ProfitSharingModel.FIXED_SHARES &&
                    'Profits shared based on fixed share allocation'}
                  {formData.profitSharingModel === ProfitSharingModel.TIME_WEIGHTED &&
                    'Profits weighted by time invested'}
                  {formData.profitSharingModel === ProfitSharingModel.TIERED &&
                    'Profits with bonus multipliers for larger investments'}
                </p>
              </div>

              {formData.profitSharingModel === ProfitSharingModel.TIME_WEIGHTED && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Time Weighting Configuration</CardTitle>
                    <CardDescription>Configure multipliers for long-term investors</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label>Base Years</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={formData.timeWeightBaseYears}
                          onChange={(e) => handleInputChange('timeWeightBaseYears', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Base Multiplier</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={formData.timeWeightBaseMultiplier}
                          onChange={(e) => handleInputChange('timeWeightBaseMultiplier', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Year 2 Threshold</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={formData.timeWeightYear2Threshold}
                          onChange={(e) => handleInputChange('timeWeightYear2Threshold', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Year 2 Multiplier</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={formData.timeWeightYear2Multiplier}
                          onChange={(e) => handleInputChange('timeWeightYear2Multiplier', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Year 3 Threshold</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={formData.timeWeightYear3Threshold}
                          onChange={(e) => handleInputChange('timeWeightYear3Threshold', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Year 3 Multiplier</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={formData.timeWeightYear3Multiplier}
                          onChange={(e) => handleInputChange('timeWeightYear3Multiplier', parseFloat(e.target.value))}
                        />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}

              {formData.profitSharingModel === ProfitSharingModel.TIERED && (
                <Card>
                  <CardHeader>
                    <CardTitle className="text-base">Tier Configuration</CardTitle>
                    <CardDescription>Configure bonus multipliers for investment tiers</CardDescription>
                  </CardHeader>
                  <CardContent className="space-y-3">
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label>Tier 1 Threshold</Label>
                        <Input
                          type="number"
                          value={formData.tier1Threshold}
                          onChange={(e) => handleInputChange('tier1Threshold', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Tier 1 Multiplier</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={formData.tier1Multiplier}
                          onChange={(e) => handleInputChange('tier1Multiplier', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Tier 2 Threshold</Label>
                        <Input
                          type="number"
                          value={formData.tier2Threshold}
                          onChange={(e) => handleInputChange('tier2Threshold', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Tier 2 Multiplier</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={formData.tier2Multiplier}
                          onChange={(e) => handleInputChange('tier2Multiplier', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Tier 3 Threshold</Label>
                        <Input
                          type="number"
                          value={formData.tier3Threshold}
                          onChange={(e) => handleInputChange('tier3Threshold', parseFloat(e.target.value))}
                        />
                      </div>
                      <div>
                        <Label>Tier 3 Multiplier</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={formData.tier3Multiplier}
                          onChange={(e) => handleInputChange('tier3Multiplier', parseFloat(e.target.value))}
                        />
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )}
            </div>
          )}

          {currentStep === 3 && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <Label>Investors ({investors.length})</Label>
                <Button type="button" variant="outline" size="sm" onClick={handleAddInvestor}>
                  <Plus className="h-4 w-4 mr-2" />
                  Add Investor
                </Button>
              </div>

              {investors.map((investor, index) => (
                <Card key={index}>
                  <CardContent className="pt-6">
                    <div className="space-y-3">
                      <div className="flex items-start gap-2">
                        <div className="flex-1 space-y-3">
                          <div>
                            <Label>Investor *</Label>
                            <Select
                              value={investor.investorId}
                              onValueChange={(value) => handleInvestorChange(index, 'investorId', value)}
                              disabled={!formData.shopId || usersLoading}
                            >
                              <SelectTrigger>
                                <SelectValue
                                  placeholder={
                                    !formData.shopId
                                      ? "Select shop first"
                                      : usersLoading
                                      ? "Loading investors..."
                                      : "Select an investor"
                                  }
                                />
                              </SelectTrigger>
                              <SelectContent>
                                {shopUsers.length === 0 && !usersLoading ? (
                                  <div className="px-2 py-4 text-sm text-muted-foreground text-center">
                                    No active users available for this shop
                                  </div>
                                ) : (
                                  shopUsers.map((user) => (
                                    <SelectItem key={user.id} value={user.id}>
                                      {user.firstName && user.lastName
                                        ? `${user.firstName} ${user.lastName} (${user.email})`
                                        : user.email}
                                    </SelectItem>
                                  ))
                                )}
                              </SelectContent>
                            </Select>
                            {investor.investorId && (
                              <p className="text-xs text-muted-foreground">
                                {shopUsers.find(u => u.id === investor.investorId)?.roles.join(', ')}
                              </p>
                            )}
                          </div>
                          <div className="grid grid-cols-2 gap-3">
                            <div>
                              <Label>Amount *</Label>
                              <Input
                                type="number"
                                value={investor.amount}
                                onChange={(e) => handleInvestorChange(index, 'amount', parseFloat(e.target.value) || 0)}
                                placeholder="0.00"
                              />
                            </div>
                            {formData.profitSharingModel === ProfitSharingModel.FIXED_SHARES && (
                              <div>
                                <Label>Fixed Shares *</Label>
                                <Input
                                  type="number"
                                  value={investor.fixedShares || ''}
                                  onChange={(e) =>
                                    handleInvestorChange(index, 'fixedShares', parseInt(e.target.value) || undefined)
                                  }
                                  placeholder="0"
                                />
                              </div>
                            )}
                          </div>
                          <div>
                            <Label>Notes</Label>
                            <Input
                              value={investor.notes || ''}
                              onChange={(e) => handleInvestorChange(index, 'notes', e.target.value)}
                              placeholder="Optional notes"
                            />
                          </div>
                        </div>
                        {investors.length > 1 && (
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon"
                            onClick={() => handleRemoveInvestor(index)}
                          >
                            <Trash2 className="h-4 w-4 text-destructive" />
                          </Button>
                        )}
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}

              <Card>
                <CardContent className="pt-6">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label className="text-muted-foreground">Total Amount</Label>
                      <p className="text-2xl font-bold">{formatCurrency(totalAmount)}</p>
                    </div>
                    {formData.profitSharingModel === ProfitSharingModel.FIXED_SHARES && (
                      <div>
                        <Label className="text-muted-foreground">Total Shares</Label>
                        <p className="text-2xl font-bold">{totalShares}</p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            </div>
          )}

          {currentStep === 4 && (
            <div className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle>Round Details</CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <Label className="text-muted-foreground">Shop</Label>
                      <p className="font-medium">{selectedShop?.name || formData.shopId}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Investment Type</Label>
                      <p className="font-medium">{formData.investmentType.replace('_', ' ')}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Profit Model</Label>
                      <p className="font-medium">{formData.profitSharingModel.replace('_', ' ')}</p>
                    </div>
                    <div>
                      <Label className="text-muted-foreground">Maturity</Label>
                      <p className="font-medium">{formData.maturityMonths} months</p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              <Card>
                <CardHeader>
                  <CardTitle>Investors Summary</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="grid grid-cols-3 gap-4 pb-4 border-b">
                      <div>
                        <Label className="text-muted-foreground">Total Investors</Label>
                        <p className="text-2xl font-bold">{investors.length}</p>
                      </div>
                      <div>
                        <Label className="text-muted-foreground">Total Investment</Label>
                        <p className="text-2xl font-bold">{formatCurrency(totalAmount)}</p>
                      </div>
                      {formData.profitSharingModel === ProfitSharingModel.FIXED_SHARES && (
                        <div>
                          <Label className="text-muted-foreground">Total Shares</Label>
                          <p className="text-2xl font-bold">{totalShares}</p>
                        </div>
                      )}
                    </div>

                    {/* Individual Investors List */}
                    <div className="space-y-3">
                      <Label className="text-sm font-semibold">Investor Details</Label>
                      {investors.map((investor, index) => {
                        const investorUser = shopUsers.find(u => u.id === investor.investorId)
                        return (
                          <div key={index} className="border rounded-lg p-3 space-y-2">
                            <div className="flex items-center justify-between">
                              <div>
                                <p className="font-medium">
                                  {investorUser
                                    ? (investorUser.firstName && investorUser.lastName
                                      ? `${investorUser.firstName} ${investorUser.lastName}`
                                      : investorUser.email)
                                    : investor.investorId}
                                </p>
                                {investorUser && (
                                  <p className="text-xs text-muted-foreground">{investorUser.email}</p>
                                )}
                              </div>
                              <div className="text-right">
                                <p className="font-semibold">{formatCurrency(investor.amount)}</p>
                                {formData.profitSharingModel === ProfitSharingModel.FIXED_SHARES && investor.fixedShares && (
                                  <p className="text-xs text-muted-foreground">{investor.fixedShares} shares</p>
                                )}
                              </div>
                            </div>
                            {investor.notes && (
                              <p className="text-sm text-muted-foreground italic">{investor.notes}</p>
                            )}
                          </div>
                        )
                      })}
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Navigation */}
      <div className="flex justify-between">
        <Button variant="outline" onClick={handleBack} disabled={currentStep === 1}>
          <ArrowLeft className="h-4 w-4 mr-2" />
          Back
        </Button>
        {currentStep < STEPS.length ? (
          <Button onClick={handleNext}>
            Next
            <ArrowRight className="h-4 w-4 ml-2" />
          </Button>
        ) : (
          <Button onClick={handleSubmit} disabled={createRound.isPending}>
            {createRound.isPending ? 'Creating...' : 'Create Round'}
          </Button>
        )}
      </div>
    </div>
  )
}
