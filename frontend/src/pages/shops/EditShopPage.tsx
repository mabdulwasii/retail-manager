import React, { useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/ManualAuthContext'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { ArrowLeft, Store, Loader2, AlertCircle, Save } from 'lucide-react'
import { useShopById, useUpdateShop } from '@/hooks/useShops'
import { ShopUpdateRequest } from '@/services/shopService'
import { ShopStatusBadge } from '@/components/shops'

const shopSchema = yup.object().shape({
  name: yup
    .string()
    .required('Shop name is required')
    .min(2, 'Shop name must be at least 2 characters')
    .max(100, 'Shop name must not exceed 100 characters'),
  email: yup
    .string()
    .required('Email is required')
    .email('Must be a valid email address'),
  description: yup
    .string()
    .max(500, 'Description must not exceed 500 characters'),
  phoneNumber: yup
    .string()
    .matches(/^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/, {
      message: 'Phone number is not valid',
      excludeEmptyString: true
    }),
  address: yup.string().max(200, 'Address must not exceed 200 characters'),
  city: yup.string().max(100, 'City must not exceed 100 characters'),
  state: yup.string().max(100, 'State must not exceed 100 characters'),
  country: yup.string().max(100, 'Country must not exceed 100 characters'),
  postalCode: yup.string().max(20, 'Postal code must not exceed 20 characters'),
  taxId: yup.string().max(50, 'Tax ID must not exceed 50 characters'),
  openingDate: yup.string()
})

type ShopFormData = yup.InferType<typeof shopSchema>

export const EditShopPage: React.FC = () => {
  const { shopId } = useParams<{ shopId: string }>()
  const navigate = useNavigate()
  const { hasPermission } = useAuth()
  
  const { data: shop, isLoading: loadingShop, isError, error } = useShopById(shopId)
  const updateShopMutation = useUpdateShop()

  // Check if user has permission to update shops
  const canUpdateShop = hasPermission('SHOP_UPDATE')
  
  // Redirect if no permission
  useEffect(() => {
    if (!canUpdateShop) {
      navigate(`/shops/${shopId || ''}`)
    }
  }, [canUpdateShop, navigate, shopId])

  if (!canUpdateShop) {
    return null
  }

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting, isDirty },
    reset
  } = useForm<ShopFormData>({
    resolver: yupResolver(shopSchema)
  })

  useEffect(() => {
    if (shop) {
      reset({
        name: shop.name,
        email: shop.email,
        description: shop.description || '',
        phoneNumber: shop.phoneNumber || '',
        address: shop.address || '',
        city: shop.city || '',
        state: shop.state || '',
        country: shop.country || '',
        postalCode: shop.postalCode || '',
        taxId: shop.taxId || '',
        openingDate: shop.openingDate ? new Date(shop.openingDate).toISOString().split('T')[0] : ''
      })
    }
  }, [shop, reset])

  const onSubmit = async (data: ShopFormData) => {
    if (!shopId || !shop) return

    try {
      // Convert date to ISO 8601 format if provided
      let isoOpeningDate = shop.openingDate // Keep existing if not changed
      if (data.openingDate) {
        // Convert YYYY-MM-DD to full ISO timestamp
        const date = new Date(data.openingDate)
        // Set time to noon UTC to avoid timezone issues
        date.setUTCHours(12, 0, 0, 0)
        isoOpeningDate = date.toISOString()
      }

      const shopData: ShopUpdateRequest = {
        name: data.name,
        email: data.email,
        ...(data.description && { description: data.description }),
        ...(data.phoneNumber && { phoneNumber: data.phoneNumber }),
        ...(data.address && { address: data.address }),
        ...(data.city && { city: data.city }),
        ...(data.state && { state: data.state }),
        ...(data.country && { country: data.country }),
        ...(data.postalCode && { postalCode: data.postalCode }),
        ...(data.taxId && { taxId: data.taxId }),
        ...(isoOpeningDate && { openingDate: isoOpeningDate })
      }

      await updateShopMutation.mutateAsync({ shopId, data: shopData })
      
      // Navigate back to shop detail page
      navigate(`/shops/${shopId}`)
    } catch (error) {
      // Error handling is done in the mutation hook
      console.error('Failed to update shop:', error)
    }
  }

  const handleCancel = () => {
    navigate(`/shops/${shopId}`)
  }


  if (loadingShop) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
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
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div className="flex flex-col gap-4">
        <Button variant="ghost" className="w-fit" onClick={handleCancel}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back to Shop Details
        </Button>
        
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-primary/10 rounded-lg">
              <Store className="h-6 w-6 text-primary" />
            </div>
            <div>
              <div className="flex items-center gap-3">
                <h1 className="text-3xl font-bold tracking-tight">Edit Shop</h1>
                <ShopStatusBadge status={shop.status} showIcon />
              </div>
              <p className="text-muted-foreground mt-1">
                Update information for {shop.name}
              </p>
            </div>
          </div>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Basic Information</CardTitle>
            <CardDescription>
              Essential details about the shop
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="name">
                  Shop Name <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="name"
                  {...register('name')}
                  placeholder="Enter shop name"
                  aria-invalid={!!errors.name}
                />
                {errors.name && (
                  <p className="text-sm text-destructive">{errors.name.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="email">
                  Email Address <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="email"
                  type="email"
                  {...register('email')}
                  placeholder="shop@example.com"
                  aria-invalid={!!errors.email}
                />
                {errors.email && (
                  <p className="text-sm text-destructive">{errors.email.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="phoneNumber">Phone Number</Label>
                <Input
                  id="phoneNumber"
                  type="tel"
                  {...register('phoneNumber')}
                  placeholder="+1 (555) 123-4567"
                  aria-invalid={!!errors.phoneNumber}
                />
                {errors.phoneNumber && (
                  <p className="text-sm text-destructive">{errors.phoneNumber.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="openingDate">Opening Date</Label>
                <Input
                  id="openingDate"
                  type="date"
                  {...register('openingDate')}
                  aria-invalid={!!errors.openingDate}
                />
                {errors.openingDate && (
                  <p className="text-sm text-destructive">{errors.openingDate.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="taxId">Tax ID / VAT Number</Label>
                <Input
                  id="taxId"
                  {...register('taxId')}
                  placeholder="Enter tax identification number"
                  aria-invalid={!!errors.taxId}
                />
                {errors.taxId && (
                  <p className="text-sm text-destructive">{errors.taxId.message}</p>
                )}
              </div>

              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  {...register('description')}
                  placeholder="Brief description of the shop (optional)"
                  rows={3}
                  aria-invalid={!!errors.description}
                />
                {errors.description && (
                  <p className="text-sm text-destructive">{errors.description.message}</p>
                )}
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Location & Address</CardTitle>
            <CardDescription>
              Physical location details (optional)
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">

              <div className="space-y-2 md:col-span-2">
                <Label htmlFor="address">Street Address</Label>
                <Input
                  id="address"
                  {...register('address')}
                  placeholder="123 Main Street"
                  aria-invalid={!!errors.address}
                />
                {errors.address && (
                  <p className="text-sm text-destructive">{errors.address.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="city">City</Label>
                <Input
                  id="city"
                  {...register('city')}
                  placeholder="Enter city"
                  aria-invalid={!!errors.city}
                />
                {errors.city && (
                  <p className="text-sm text-destructive">{errors.city.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="state">State / Province</Label>
                <Input
                  id="state"
                  {...register('state')}
                  placeholder="Enter state or province"
                  aria-invalid={!!errors.state}
                />
                {errors.state && (
                  <p className="text-sm text-destructive">{errors.state.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="postalCode">Postal / ZIP Code</Label>
                <Input
                  id="postalCode"
                  {...register('postalCode')}
                  placeholder="12345"
                  aria-invalid={!!errors.postalCode}
                />
                {errors.postalCode && (
                  <p className="text-sm text-destructive">{errors.postalCode.message}</p>
                )}
              </div>

              <div className="space-y-2">
                <Label htmlFor="country">Country</Label>
                <Input
                  id="country"
                  {...register('country')}
                  placeholder="Enter country"
                  aria-invalid={!!errors.country}
                />
                {errors.country && (
                  <p className="text-sm text-destructive">{errors.country.message}</p>
                )}
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Shop Metadata</CardTitle>
            <CardDescription>System-generated information</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div>
                <p className="text-muted-foreground">Shop ID</p>
                <p className="font-mono">{shop.id}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Current Status</p>
                <ShopStatusBadge status={shop.status} showIcon />
              </div>
              <div>
                <p className="text-muted-foreground">Created</p>
                <p>{new Date(shop.createdAt).toLocaleString()}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Last Updated</p>
                <p>{new Date(shop.updatedAt).toLocaleString()}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <div className="flex flex-col sm:flex-row gap-3 justify-between">
              <Alert className="flex-1">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  {isDirty 
                    ? 'You have unsaved changes' 
                    : 'No changes made yet'}
                </AlertDescription>
              </Alert>
              
              <div className="flex gap-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleCancel}
                  disabled={isSubmitting || updateShopMutation.isPending}
                >
                  Cancel
                </Button>
                <Button
                  type="submit"
                  disabled={isSubmitting || updateShopMutation.isPending || !isDirty}
                >
                  {(isSubmitting || updateShopMutation.isPending) ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Saving Changes...
                    </>
                  ) : (
                    <>
                      <Save className="mr-2 h-4 w-4" />
                      Save Changes
                    </>
                  )}
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </form>
    </div>
  )
}
