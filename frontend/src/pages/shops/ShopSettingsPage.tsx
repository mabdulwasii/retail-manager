import React, { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Textarea } from '@/components/ui/textarea'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Separator } from '@/components/ui/separator'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  ArrowLeft,
  Store,
  Loader2,
  AlertCircle,
  Save,
  DollarSign,
  Receipt,
  CreditCard,
  Bell,
  Shield,
  RefreshCw
} from 'lucide-react'
import { useShopById } from '@/hooks/useShops'
import {
  useShopConfiguration,
  useUpdateShopConfiguration,
  useShopCustomization,
  useUpdateShopCustomization,
} from '@/hooks/useShopSettings'
import { ThemeVariant, FontSize, DashboardLayout } from '@/services/shopConfigurationService'

export const ShopSettingsPage: React.FC = () => {
  const { shopId } = useParams<{ shopId: string }>()
  const navigate = useNavigate()
  
  // Fetch shop data
  const { data: shop, isLoading: loadingShop, isError, error } = useShopById(shopId)
  
  // Fetch configuration and customization
  const {
    data: configuration,
    isLoading: loadingConfig,
    refetch: refetchConfig,
  } = useShopConfiguration(shopId)
  
  const {
    data: customization,
    isLoading: loadingCustomization,
    refetch: refetchCustomization,
  } = useShopCustomization(shopId)

  // Mutations
  const updateConfigMutation = useUpdateShopConfiguration()
  const updateCustomizationMutation = useUpdateShopCustomization()

  // Configuration state
  const [investmentEnabled, setInvestmentEnabled] = useState(false)
  const [analyticsEnabled, setAnalyticsEnabled] = useState(false)
  const [fraudDetectionEnabled, setFraudDetectionEnabled] = useState(false)
  const [autoBackupEnabled, setAutoBackupEnabled] = useState(false)
  const [currency, setCurrency] = useState('USD')
  const [taxRate, setTaxRate] = useState('0')
  const [maxDiscountPercent, setMaxDiscountPercent] = useState('20')
  const [receiptFooter, setReceiptFooter] = useState('')

  // Customization state
  const [receiptHeader, setReceiptHeader] = useState('')
  const [receiptShowLogo, setReceiptShowLogo] = useState(true)
  const [themeVariant, setThemeVariant] = useState<ThemeVariant>('LIGHT')
  const [fontSize, setFontSize] = useState<FontSize>('MEDIUM')
  const [dashboardLayout, setDashboardLayout] = useState<DashboardLayout>('GRID')
  const [enableAnimations, setEnableAnimations] = useState(true)
  const [showAdvancedFeatures, setShowAdvancedFeatures] = useState(false)

  useEffect(() => {
    if (configuration) {
      setInvestmentEnabled(configuration.investmentEnabled ?? false)
      setAnalyticsEnabled(configuration.analyticsEnabled ?? false)
      setFraudDetectionEnabled(configuration.fraudDetectionEnabled ?? false)
      setAutoBackupEnabled(configuration.autoBackupEnabled ?? false)
      setCurrency(configuration.currency || 'USD')
      setTaxRate(configuration.taxRate?.toString() || '0')
      setMaxDiscountPercent(configuration.maxDiscountPercentage?.toString() || '20')
      setReceiptFooter(configuration.receiptFooter || '')
    }
  }, [configuration])

  useEffect(() => {
    if (customization) {
      setReceiptHeader(customization.receiptHeader || '')
      setReceiptShowLogo(customization.receiptShowLogo ?? true)
      setThemeVariant(customization.themeVariant || 'LIGHT')
      setFontSize(customization.fontSize || 'MEDIUM')
      setDashboardLayout(customization.dashboardLayout || 'GRID')
      setEnableAnimations(customization.enableAnimations ?? true)
      setShowAdvancedFeatures(customization.showAdvancedFeatures ?? false)
    }
  }, [customization])

  const handleSaveConfiguration = async () => {
    if (!shopId) return

    try {
      await updateConfigMutation.mutateAsync({
        shopId,
        config: {
          investmentEnabled,
          analyticsEnabled,
          fraudDetectionEnabled,
          autoBackupEnabled,
          currency,
          taxRate: parseFloat(taxRate) || 0,
          maxDiscountPercentage: parseFloat(maxDiscountPercent) || 0,
          receiptFooter,
        },
      })
      refetchConfig()
    } catch (error) {
      console.error('Failed to save configuration:', error)
    }
  }

  const handleSaveCustomization = async () => {
    if (!shopId) return

    try {
      await updateCustomizationMutation.mutateAsync({
        shopId,
        customization: {
          receiptHeader,
          receiptShowLogo,
          themeVariant,
          fontSize,
          dashboardLayout,
          enableAnimations,
          showAdvancedFeatures,
        },
      })
      refetchCustomization()
    } catch (error) {
      console.error('Failed to save customization:', error)
    }
  }

  const handleSaveAll = async () => {
    await Promise.all([handleSaveConfiguration(), handleSaveCustomization()])
  }

  const isSaving = updateConfigMutation.isPending || updateCustomizationMutation.isPending

  const handleCancel = () => {
    navigate(`/shops/${shopId}`)
  }

  const isLoading = loadingShop || loadingConfig || loadingCustomization

  if (isLoading) {
    return (
      <div className="flex flex-col justify-center items-center min-h-[400px] gap-4">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
        <p className="text-sm text-muted-foreground">Loading shop settings...</p>
      </div>
    )
  }

  if (isError || !shop) {
    return (
      <div className="space-y-4">
        <Button variant="ghost" onClick={() => navigate('/shops')}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Shops
        </Button>
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            {error?.message || 'Shop not found'}
          </AlertDescription>
        </Alert>
      </div>
    )
  }

  return (
    <div className="space-y-6 max-w-5xl">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <Button variant="ghost" className="w-fit" onClick={handleCancel}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Shop Details
        </Button>
        
        <div className="flex items-center gap-3">
          <div className="p-2 bg-primary/10 rounded-lg">
            <Store className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-3xl font-bold tracking-tight">Shop Settings</h1>
            <p className="text-muted-foreground mt-1">
              Configure settings for {shop.name}
            </p>
          </div>
        </div>
      </div>

      <Tabs defaultValue="general" className="space-y-4">
        <TabsList className="grid w-full grid-cols-2 md:grid-cols-3 lg:grid-cols-5 h-auto gap-1">
          <TabsTrigger value="general">General</TabsTrigger>
          <TabsTrigger value="tax">Tax & Currency</TabsTrigger>
          <TabsTrigger value="receipts">Receipts</TabsTrigger>
          <TabsTrigger value="payments">Payments</TabsTrigger>
          <TabsTrigger value="notifications">Notifications</TabsTrigger>
        </TabsList>

        {/* General Settings */}
        <TabsContent value="general" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Shield className="h-5 w-5" />
                General Settings
              </CardTitle>
              <CardDescription>
                Basic configuration for shop operations
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-4">
                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label htmlFor="investment-enabled">Investment Features</Label>
                    <p className="text-sm text-muted-foreground">
                      Enable investment and profit sharing features
                    </p>
                  </div>
                  <Switch
                    id="investment-enabled"
                    checked={investmentEnabled}
                    onCheckedChange={setInvestmentEnabled}
                  />
                </div>

                <Separator />

                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label htmlFor="analytics-enabled">Analytics</Label>
                    <p className="text-sm text-muted-foreground">
                      Enable advanced analytics and reporting
                    </p>
                  </div>
                  <Switch
                    id="analytics-enabled"
                    checked={analyticsEnabled}
                    onCheckedChange={setAnalyticsEnabled}
                  />
                </div>

                <Separator />

                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label htmlFor="fraud-detection">Fraud Detection</Label>
                    <p className="text-sm text-muted-foreground">
                      Enable automatic fraud detection for transactions
                    </p>
                  </div>
                  <Switch
                    id="fraud-detection"
                    checked={fraudDetectionEnabled}
                    onCheckedChange={setFraudDetectionEnabled}
                  />
                </div>

                <Separator />

                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label htmlFor="auto-backup">Auto Backup</Label>
                    <p className="text-sm text-muted-foreground">
                      Automatically backup shop data daily
                    </p>
                  </div>
                  <Switch
                    id="auto-backup"
                    checked={autoBackupEnabled}
                    onCheckedChange={setAutoBackupEnabled}
                  />
                </div>

                <Separator />

                <div className="space-y-2">
                  <Label htmlFor="max-discount">Maximum Discount (%)</Label>
                  <Input
                    id="max-discount"
                    type="number"
                    value={maxDiscountPercent}
                    onChange={(e) => setMaxDiscountPercent(e.target.value)}
                    placeholder="20"
                    min="0"
                    max="100"
                    step="0.01"
                    className="max-w-xs"
                  />
                  <p className="text-xs text-muted-foreground">
                    Maximum discount percentage staff can apply
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Tax & Currency Settings */}
        <TabsContent value="tax" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <DollarSign className="h-5 w-5" />
                Tax & Currency Settings
              </CardTitle>
              <CardDescription>
                Configure tax rates and currency preferences
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="space-y-2">
                  <Label>Currency</Label>
                  <Select value={currency} onValueChange={setCurrency}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select currency" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="USD">USD - US Dollar</SelectItem>
                      <SelectItem value="EUR">EUR - Euro</SelectItem>
                      <SelectItem value="GBP">GBP - British Pound</SelectItem>
                      <SelectItem value="NGN">NGN - Nigerian Naira</SelectItem>
                      <SelectItem value="GHS">GHS - Ghanaian Cedi</SelectItem>
                      <SelectItem value="KES">KES - Kenyan Shilling</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="tax-rate">Default Tax Rate (%)</Label>
                  <Input
                    id="tax-rate"
                    type="number"
                    value={taxRate}
                    onChange={(e) => setTaxRate(e.target.value)}
                    placeholder="10"
                    min="0"
                    max="100"
                    step="0.01"
                  />
                </div>
              </div>

              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  Tax rate will be applied to all taxable items. You can override this per product.
                </AlertDescription>
              </Alert>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Receipt Settings */}
        <TabsContent value="receipts" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Receipt className="h-5 w-5" />
                Receipt Settings
              </CardTitle>
              <CardDescription>
                Customize receipt header and footer text
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="receipt-header">Receipt Header</Label>
                <Textarea
                  id="receipt-header"
                  value={receiptHeader}
                  onChange={(e) => setReceiptHeader(e.target.value)}
                  placeholder="Thank you for shopping with us!"
                  rows={3}
                />
                <p className="text-xs text-muted-foreground">
                  Text to display at the top of receipts
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="receipt-footer">Receipt Footer</Label>
                <Textarea
                  id="receipt-footer"
                  value={receiptFooter}
                  onChange={(e) => setReceiptFooter(e.target.value)}
                  placeholder="Visit us again soon!"
                  rows={3}
                />
                <p className="text-xs text-muted-foreground">
                  Text to display at the bottom of receipts
                </p>
              </div>

              <Alert>
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  Receipt customization applies to both printed and email receipts.
                </AlertDescription>
              </Alert>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Appearance Settings */}
        <TabsContent value="payments" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <CreditCard className="h-5 w-5" />
                Appearance & Display
              </CardTitle>
              <CardDescription>
                Customize the look and feel of your shop interface
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-4">
                <div className="grid gap-4 md:grid-cols-2">
                  <div className="space-y-2">
                    <Label>Theme</Label>
                    <Select value={themeVariant} onValueChange={(value: any) => setThemeVariant(value)}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select theme" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="LIGHT">Light</SelectItem>
                        <SelectItem value="DARK">Dark</SelectItem>
                        <SelectItem value="AUTO">Auto (System)</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div className="space-y-2">
                    <Label>Font Size</Label>
                    <Select value={fontSize} onValueChange={(value: any) => setFontSize(value)}>
                      <SelectTrigger>
                        <SelectValue placeholder="Select size" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="SMALL">Small</SelectItem>
                        <SelectItem value="MEDIUM">Medium</SelectItem>
                        <SelectItem value="LARGE">Large</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label>Dashboard Layout</Label>
                  <Select value={dashboardLayout} onValueChange={(value: any) => setDashboardLayout(value)}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select layout" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="GRID">Grid</SelectItem>
                      <SelectItem value="LIST">List</SelectItem>
                      <SelectItem value="CARD">Card</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <Separator />

                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label htmlFor="enable-animations">Enable Animations</Label>
                    <p className="text-sm text-muted-foreground">
                      Show animations and transitions
                    </p>
                  </div>
                  <Switch
                    id="enable-animations"
                    checked={enableAnimations}
                    onCheckedChange={setEnableAnimations}
                  />
                </div>

                <Separator />

                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label htmlFor="show-advanced">Show Advanced Features</Label>
                    <p className="text-sm text-muted-foreground">
                      Display advanced settings and options
                    </p>
                  </div>
                  <Switch
                    id="show-advanced"
                    checked={showAdvancedFeatures}
                    onCheckedChange={setShowAdvancedFeatures}
                  />
                </div>

                <Separator />

                <div className="flex items-center justify-between">
                  <div className="space-y-0.5">
                    <Label htmlFor="receipt-logo">Show Logo on Receipts</Label>
                    <p className="text-sm text-muted-foreground">
                      Display shop logo on printed receipts
                    </p>
                  </div>
                  <Switch
                    id="receipt-logo"
                    checked={receiptShowLogo}
                    onCheckedChange={setReceiptShowLogo}
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Receipt Customization */}
        <TabsContent value="notifications" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Bell className="h-5 w-5" />
                Receipt Customization
              </CardTitle>
              <CardDescription>
                Customize receipt header text (footer is in Tax & Currency tab)
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="receipt-header-custom">Receipt Header</Label>
                  <Textarea
                    id="receipt-header-custom"
                    value={receiptHeader}
                    onChange={(e) => setReceiptHeader(e.target.value)}
                    placeholder="Welcome to our store!"
                    rows={3}
                    maxLength={1000}
                  />
                  <p className="text-xs text-muted-foreground">
                    Text to display at the top of receipts (max 1000 characters)
                  </p>
                </div>

                <Alert>
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>
                    Receipt customization applies to both printed and digital receipts.
                  </AlertDescription>
                </Alert>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Save Actions */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex justify-between items-center">
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  refetchConfig()
                  refetchCustomization()
                }}
                disabled={isSaving}
              >
                <RefreshCw className="mr-2 h-4 w-4" />
                Refresh
              </Button>
            </div>
            <div className="flex gap-3">
              <Button
                variant="outline"
                onClick={handleCancel}
                disabled={isSaving}
              >
                Cancel
              </Button>
              <Button
                onClick={handleSaveAll}
                disabled={isSaving}
              >
                {isSaving ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Saving Settings...
                  </>
                ) : (
                  <>
                    <Save className="mr-2 h-4 w-4" />
                    Save All Settings
                  </>
                )}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
