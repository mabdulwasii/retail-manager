import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { SubscriptionTierCard } from '@/components/cloud/SubscriptionTierCard';
import { ShopFormFields } from '@/components/cloud/ShopFormFields';
import { useRegisterTenant } from '@/hooks/useCloudTenants';
import { SubscriptionTier, ShopLinkRequest, TenantRegistrationRequest } from '@/services/cloudAggregatorService';
import { Cloud, ArrowLeft, ArrowRight, Building, Mail, Phone, MapPin, Globe, CheckCircle, AlertCircle } from 'lucide-react';

/**
 * Cloud Tenant Registration Page
 * Public multi-step wizard for new tenant registration
 */

interface TenantFormData {
  tenantName: string;
  tenantEmail: string;
  companyRegistration: string;
  taxId: string;
  address: string;
  city: string;
  country: string;
  phoneNumber: string;
}

const INITIAL_TENANT_DATA: TenantFormData = {
  tenantName: '',
  tenantEmail: '',
  companyRegistration: '',
  taxId: '',
  address: '',
  city: '',
  country: '',
  phoneNumber: '',
};

const INITIAL_SHOP: ShopLinkRequest = {
  shopName: '',
  shopEmail: '',
  address: '',
  city: '',
  country: '',
  phoneNumber: '',
};

export const CloudTenantRegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [subscriptionTier, setSubscriptionTier] = useState<SubscriptionTier>(SubscriptionTier.FREE);
  const [tenantData, setTenantData] = useState<TenantFormData>(INITIAL_TENANT_DATA);
  const [shops, setShops] = useState<ShopLinkRequest[]>([INITIAL_SHOP]);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const registerTenant = useRegisterTenant();

  const handleTenantDataChange = (field: keyof TenantFormData, value: string) => {
    setTenantData((prev) => ({ ...prev, [field]: value }));
    // Clear error for this field
    if (errors[field]) {
      setErrors((prev) => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const validateStep = (currentStep: number): boolean => {
    const newErrors: Record<string, string> = {};

    if (currentStep === 2) {
      // Validate tenant information
      if (!tenantData.tenantName.trim()) {
        newErrors.tenantName = 'Business name is required';
      }
      if (!tenantData.tenantEmail.trim()) {
        newErrors.tenantEmail = 'Email is required';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(tenantData.tenantEmail)) {
        newErrors.tenantEmail = 'Invalid email format';
      }
    }

    if (currentStep === 3) {
      // Validate at least one shop with required fields
      if (shops.length === 0 || !shops[0].shopName.trim()) {
        newErrors.shops = 'At least one shop with a name is required';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleNext = () => {
    if (validateStep(step)) {
      setStep((prev) => prev + 1);
    }
  };

  const handleBack = () => {
    setStep((prev) => prev - 1);
  };

  const handleSubmit = () => {
    if (!validateStep(step)) {
      return;
    }

    const request: TenantRegistrationRequest = {
      tenantName: tenantData.tenantName,
      tenantEmail: tenantData.tenantEmail,
      subscriptionTier,
      companyRegistration: tenantData.companyRegistration || undefined,
      taxId: tenantData.taxId || undefined,
      address: tenantData.address || undefined,
      city: tenantData.city || undefined,
      country: tenantData.country || undefined,
      phoneNumber: tenantData.phoneNumber || undefined,
      shops,
    };

    registerTenant.mutate(request, {
      onSuccess: (data) => {
        // Navigate to success page with API key
        navigate('/cloud/register/success', {
          state: {
            apiKey: data.apiKey,
            tenant: data.tenant,
            shops: data.shops,
          },
        });
      },
    });
  };

  const renderStepIndicator = () => (
    <div className="flex items-center justify-center mb-8">
      {[1, 2, 3, 4].map((num) => (
        <React.Fragment key={num}>
          <div
            className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold transition-all ${
              step >= num
                ? 'bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg'
                : 'bg-gray-200 text-gray-500'
            }`}
          >
            {num}
          </div>
          {num < 4 && (
            <div
              className={`h-1 w-16 transition-all ${
                step > num
                  ? 'bg-gradient-to-r from-blue-600 to-purple-600'
                  : 'bg-gray-200'
              }`}
            />
          )}
        </React.Fragment>
      ))}
    </div>
  );

  const renderTierSelection = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Choose Your Plan</h2>
        <p className="text-gray-600">Select the subscription tier that fits your needs</p>
        <p className="text-sm text-muted-foreground mt-2">
          You can upgrade or downgrade anytime from your dashboard
        </p>
      </div>

      <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-6">
        {Object.values(SubscriptionTier).map((tier) => (
          <SubscriptionTierCard
            key={tier}
            tier={tier}
            selected={subscriptionTier === tier}
            onSelect={setSubscriptionTier}
          />
        ))}
      </div>

      <div className="flex justify-end">
        <Button onClick={handleNext} size="lg">
          Continue
          <ArrowRight className="ml-2 h-4 w-4" />
        </Button>
      </div>
    </div>
  );

  const renderTenantInfo = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Business Information</h2>
        <p className="text-gray-600">Tell us about your retail business</p>
      </div>

      <div className="space-y-4">
        {/* Business Name */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Business Name <span className="text-destructive">*</span>
          </label>
          <div className="relative">
            <Building className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              value={tenantData.tenantName}
              onChange={(e) => handleTenantDataChange('tenantName', e.target.value)}
              placeholder="ABC Retail Corporation"
              className="pl-10"
              required
            />
          </div>
          {errors.tenantName && (
            <p className="text-xs text-destructive mt-1">{errors.tenantName}</p>
          )}
        </div>

        {/* Email */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Email Address <span className="text-destructive">*</span>
          </label>
          <div className="relative">
            <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              type="email"
              value={tenantData.tenantEmail}
              onChange={(e) => handleTenantDataChange('tenantEmail', e.target.value)}
              placeholder="contact@abcretail.com"
              className="pl-10"
              required
            />
          </div>
          {errors.tenantEmail && (
            <p className="text-xs text-destructive mt-1">{errors.tenantEmail}</p>
          )}
          <p className="text-xs text-muted-foreground mt-1">
            This will be your primary contact email and username
          </p>
        </div>

        {/* Optional Fields */}
        <div className="grid md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Company Registration Number
            </label>
            <Input
              value={tenantData.companyRegistration}
              onChange={(e) => handleTenantDataChange('companyRegistration', e.target.value)}
              placeholder="REG123456"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Tax ID</label>
            <Input
              value={tenantData.taxId}
              onChange={(e) => handleTenantDataChange('taxId', e.target.value)}
              placeholder="TAX789012"
            />
          </div>
        </div>

        {/* Address */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Address</label>
          <div className="relative">
            <MapPin className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              value={tenantData.address}
              onChange={(e) => handleTenantDataChange('address', e.target.value)}
              placeholder="123 Business Avenue"
              className="pl-10"
            />
          </div>
        </div>

        {/* City and Country */}
        <div className="grid md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">City</label>
            <Input
              value={tenantData.city}
              onChange={(e) => handleTenantDataChange('city', e.target.value)}
              placeholder="New York"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Country</label>
            <div className="relative">
              <Globe className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
              <Input
                value={tenantData.country}
                onChange={(e) => handleTenantDataChange('country', e.target.value)}
                placeholder="USA"
                className="pl-10"
              />
            </div>
          </div>
        </div>

        {/* Phone */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">Phone Number</label>
          <div className="relative">
            <Phone className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              type="tel"
              value={tenantData.phoneNumber}
              onChange={(e) => handleTenantDataChange('phoneNumber', e.target.value)}
              placeholder="+1 (555) 123-4567"
              className="pl-10"
            />
          </div>
        </div>
      </div>

      <div className="flex justify-between">
        <Button variant="outline" onClick={handleBack}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <Button onClick={handleNext}>
          Continue
          <ArrowRight className="ml-2 h-4 w-4" />
        </Button>
      </div>
    </div>
  );

  const renderShopsSetup = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Add Your Shops</h2>
        <p className="text-gray-600">Register at least one shop location to get started</p>
      </div>

      <ShopFormFields shops={shops} onChange={setShops} />

      {errors.shops && (
        <div className="flex items-center gap-2 text-destructive text-sm">
          <AlertCircle className="h-4 w-4" />
          <p>{errors.shops}</p>
        </div>
      )}

      <div className="flex justify-between">
        <Button variant="outline" onClick={handleBack}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <Button onClick={handleNext}>
          Continue
          <ArrowRight className="ml-2 h-4 w-4" />
        </Button>
      </div>
    </div>
  );

  const renderReview = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Review & Submit</h2>
        <p className="text-gray-600">Please review your information before submitting</p>
      </div>

      <div className="space-y-4">
        {/* Subscription */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Subscription Plan</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-lg font-semibold">{subscriptionTier} Tier</p>
          </CardContent>
        </Card>

        {/* Business Info */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Business Information</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            <div>
              <span className="font-medium">Name:</span> {tenantData.tenantName}
            </div>
            <div>
              <span className="font-medium">Email:</span> {tenantData.tenantEmail}
            </div>
            {tenantData.phoneNumber && (
              <div>
                <span className="font-medium">Phone:</span> {tenantData.phoneNumber}
              </div>
            )}
            {tenantData.address && (
              <div>
                <span className="font-medium">Address:</span> {tenantData.address}
                {tenantData.city && `, ${tenantData.city}`}
                {tenantData.country && `, ${tenantData.country}`}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Shops */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Shops ({shops.length})</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {shops.map((shop, index) => (
              <div key={index} className="text-sm border-l-2 border-blue-500 pl-3">
                <p className="font-medium">{shop.shopName}</p>
                {shop.shopEmail && <p className="text-muted-foreground">{shop.shopEmail}</p>}
                {shop.address && (
                  <p className="text-muted-foreground">
                    {shop.address}
                    {shop.city && `, ${shop.city}`}
                  </p>
                )}
              </div>
            ))}
          </CardContent>
        </Card>
      </div>

      {/* Terms */}
      <Card className="bg-blue-50 border-blue-200">
        <CardContent className="pt-6">
          <p className="text-sm text-gray-700">
            By submitting this registration, you agree to our{' '}
            <Link to="/terms" className="text-blue-600 hover:underline font-medium">
              Terms of Service
            </Link>{' '}
            and{' '}
            <Link to="/privacy" className="text-blue-600 hover:underline font-medium">
              Privacy Policy
            </Link>
            .
          </p>
        </CardContent>
      </Card>

      {/* Submit */}
      <div className="flex justify-between">
        <Button variant="outline" onClick={handleBack} disabled={registerTenant.isPending}>
          <ArrowLeft className="mr-2 h-4 w-4" />
          Back
        </Button>
        <Button
          onClick={handleSubmit}
          disabled={registerTenant.isPending}
          size="lg"
          className="px-8"
        >
          {registerTenant.isPending ? (
            <>Processing...</>
          ) : (
            <>
              <CheckCircle className="mr-2 h-4 w-4" />
              Complete Registration
            </>
          )}
        </Button>
      </div>

      {registerTenant.isError && (
        <div className="flex items-center gap-2 text-destructive text-sm bg-red-50 p-3 rounded">
          <AlertCircle className="h-4 w-4" />
          <p>
            {registerTenant.error?.message ||
              'An error occurred during registration. Please try again.'}
          </p>
        </div>
      )}
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      {/* Navigation */}
      <nav className="border-b bg-white/80 backdrop-blur-sm sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <Link to="/" className="flex items-center space-x-2">
              <Cloud className="h-8 w-8 text-blue-600" />
              <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                RetailHQ Cloud
              </span>
            </Link>

            <div className="text-sm text-gray-600">
              Already registered?{' '}
              <Link to="/cloud/tenants" className="text-blue-600 hover:underline font-medium">
                Sign in
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto">
          <Card className="shadow-2xl">
            <CardContent className="p-8 md:p-12">
              {renderStepIndicator()}

              {step === 1 && renderTierSelection()}
              {step === 2 && renderTenantInfo()}
              {step === 3 && renderShopsSetup()}
              {step === 4 && renderReview()}
            </CardContent>
          </Card>

          <div className="text-center mt-8">
            <p className="text-sm text-gray-600">
              Need help?{' '}
              <Link to="/contact" className="text-blue-600 hover:underline">
                Contact our support team
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CloudTenantRegisterPage;
