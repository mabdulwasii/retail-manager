import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Settings,
  Building2,
  Mail,
  Phone,
  MapPin,
  Globe,
  Clock,
  Save,
  CheckCircle,
  AlertCircle,
  FileText,
} from 'lucide-react';
import { CloudTenantStatus, SubscriptionTier } from '@/services/cloudAggregatorService';
import cloudAggregatorService from '@/services/cloudAggregatorService';

/**
 * Tenant Settings Page
 * Manage tenant configuration and preferences
 *
 * Features:
 * - Company information management
 * - Contact details (email, phone, address)
 * - Timezone and locale settings
 * - Status management
 * - Subscription tier display (read-only)
 */

interface TenantSettings {
  id: string;
  tenantName: string;
  tenantEmail: string;
  companyRegistration: string;
  taxId: string;
  address: string;
  city: string;
  state: string;
  country: string;
  phoneNumber: string;
  status: CloudTenantStatus;
  subscriptionTier: SubscriptionTier;
  shopCount: number;
  timezone?: string;
  locale?: string;
}

const TIMEZONES = [
  'UTC',
  'America/New_York',
  'America/Chicago',
  'America/Denver',
  'America/Los_Angeles',
  'Europe/London',
  'Europe/Paris',
  'Asia/Tokyo',
  'Asia/Shanghai',
  'Australia/Sydney',
];

const LOCALES = [
  { value: 'en-US', label: 'English (US)' },
  { value: 'en-GB', label: 'English (UK)' },
  { value: 'fr-FR', label: 'French' },
  { value: 'de-DE', label: 'German' },
  { value: 'es-ES', label: 'Spanish' },
  { value: 'zh-CN', label: 'Chinese (Simplified)' },
  { value: 'ja-JP', label: 'Japanese' },
];

export const TenantSettingsPage: React.FC = () => {
  // TODO: Get actual tenant ID from auth context
  const tenantId = 'demo-tenant-id';

  const [settings, setSettings] = useState<TenantSettings | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Form state
  const [formData, setFormData] = useState<Partial<TenantSettings>>({
    tenantName: '',
    tenantEmail: '',
    companyRegistration: '',
    taxId: '',
    address: '',
    city: '',
    state: '',
    country: '',
    phoneNumber: '',
    timezone: 'UTC',
    locale: 'en-US',
  });

  useEffect(() => {
    loadTenantSettings();
  }, [tenantId]);

  const loadTenantSettings = async () => {
    try {
      setIsLoading(true);
      setError(null);

      // TODO: Replace with actual API call
      // const tenant = await cloudAggregatorService.getTenant(tenantId);

      // Mock data for now
      const mockSettings: TenantSettings = {
        id: tenantId,
        tenantName: 'Demo Retail Company',
        tenantEmail: 'contact@demoretail.com',
        companyRegistration: 'RC123456',
        taxId: 'TAX-999-888',
        address: '123 Main Street',
        city: 'New York',
        state: 'NY',
        country: 'USA',
        phoneNumber: '+1-555-0100',
        status: CloudTenantStatus.ACTIVE,
        subscriptionTier: SubscriptionTier.PREMIUM,
        shopCount: 5,
        timezone: 'America/New_York',
        locale: 'en-US',
      };

      setSettings(mockSettings);
      setFormData({
        tenantName: mockSettings.tenantName,
        tenantEmail: mockSettings.tenantEmail,
        companyRegistration: mockSettings.companyRegistration,
        taxId: mockSettings.taxId,
        address: mockSettings.address,
        city: mockSettings.city,
        state: mockSettings.state,
        country: mockSettings.country,
        phoneNumber: mockSettings.phoneNumber,
        timezone: mockSettings.timezone,
        locale: mockSettings.locale,
      });
    } catch (err) {
      console.error('Failed to load tenant settings:', err);
      setError('Failed to load tenant settings. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (field: string, value: string) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    // Clear messages on change
    setError(null);
    setSuccess(null);
  };

  const handleSave = async () => {
    try {
      setIsSaving(true);
      setError(null);
      setSuccess(null);

      // Validate required fields
      if (!formData.tenantName || !formData.tenantEmail) {
        setError('Tenant name and email are required');
        return;
      }

      // Email validation
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.tenantEmail || '')) {
        setError('Please enter a valid email address');
        return;
      }

      // TODO: Replace with actual API call
      // await cloudAggregatorService.updateTenant(tenantId, formData);

      // Simulate API call
      await new Promise((resolve) => setTimeout(resolve, 1000));

      setSuccess('Settings saved successfully');

      // Reload settings
      await loadTenantSettings();
    } catch (err) {
      console.error('Failed to save settings:', err);
      setError('Failed to save settings. Please try again.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Settings className="h-8 w-8" />
            Tenant Settings
          </h1>
          <p className="text-muted-foreground mt-2">
            Manage your tenant configuration and preferences
          </p>
        </div>
        <Card>
          <CardContent className="py-12">
            <div className="flex items-center justify-center">
              <div className="text-center">
                <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                <p className="mt-4 text-muted-foreground">Loading settings...</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
          <Settings className="h-8 w-8" />
          Tenant Settings
        </h1>
        <p className="text-muted-foreground mt-2">
          Manage your tenant configuration and preferences
        </p>
      </div>

      {/* Success/Error Messages */}
      {success && (
        <Alert className="border-green-500 bg-green-50">
          <CheckCircle className="h-4 w-4 text-green-600" />
          <AlertDescription className="text-green-800">{success}</AlertDescription>
        </Alert>
      )}
      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Company Information */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Building2 className="h-5 w-5" />
            Company Information
          </CardTitle>
          <CardDescription>Basic information about your organization</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="tenantName">
                Company Name <span className="text-destructive">*</span>
              </Label>
              <Input
                id="tenantName"
                value={formData.tenantName || ''}
                onChange={(e) => handleInputChange('tenantName', e.target.value)}
                placeholder="Enter company name"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="tenantEmail">
                Company Email <span className="text-destructive">*</span>
              </Label>
              <div className="relative">
                <Mail className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="tenantEmail"
                  type="email"
                  className="pl-9"
                  value={formData.tenantEmail || ''}
                  onChange={(e) => handleInputChange('tenantEmail', e.target.value)}
                  placeholder="contact@company.com"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="companyRegistration">Company Registration</Label>
              <div className="relative">
                <FileText className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="companyRegistration"
                  className="pl-9"
                  value={formData.companyRegistration || ''}
                  onChange={(e) => handleInputChange('companyRegistration', e.target.value)}
                  placeholder="RC123456"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="taxId">Tax ID</Label>
              <div className="relative">
                <FileText className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                <Input
                  id="taxId"
                  className="pl-9"
                  value={formData.taxId || ''}
                  onChange={(e) => handleInputChange('taxId', e.target.value)}
                  placeholder="TAX-XXX-XXX"
                />
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Contact Details */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <MapPin className="h-5 w-5" />
            Contact Details
          </CardTitle>
          <CardDescription>Physical address and contact information</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="address">Street Address</Label>
            <Input
              id="address"
              value={formData.address || ''}
              onChange={(e) => handleInputChange('address', e.target.value)}
              placeholder="123 Main Street"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <Label htmlFor="city">City</Label>
              <Input
                id="city"
                value={formData.city || ''}
                onChange={(e) => handleInputChange('city', e.target.value)}
                placeholder="New York"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="state">State/Province</Label>
              <Input
                id="state"
                value={formData.state || ''}
                onChange={(e) => handleInputChange('state', e.target.value)}
                placeholder="NY"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="country">Country</Label>
              <Input
                id="country"
                value={formData.country || ''}
                onChange={(e) => handleInputChange('country', e.target.value)}
                placeholder="USA"
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="phoneNumber">Phone Number</Label>
            <div className="relative">
              <Phone className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                id="phoneNumber"
                type="tel"
                className="pl-9"
                value={formData.phoneNumber || ''}
                onChange={(e) => handleInputChange('phoneNumber', e.target.value)}
                placeholder="+1-555-0100"
              />
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Timezone & Locale */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Globe className="h-5 w-5" />
            Timezone & Locale Settings
          </CardTitle>
          <CardDescription>Configure regional preferences</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="timezone">
                <Clock className="inline h-4 w-4 mr-2" />
                Timezone
              </Label>
              <Select
                value={formData.timezone || 'UTC'}
                onValueChange={(value) => handleInputChange('timezone', value)}
              >
                <SelectTrigger id="timezone">
                  <SelectValue placeholder="Select timezone" />
                </SelectTrigger>
                <SelectContent>
                  {TIMEZONES.map((tz) => (
                    <SelectItem key={tz} value={tz}>
                      {tz}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="locale">
                <Globe className="inline h-4 w-4 mr-2" />
                Locale
              </Label>
              <Select
                value={formData.locale || 'en-US'}
                onValueChange={(value) => handleInputChange('locale', value)}
              >
                <SelectTrigger id="locale">
                  <SelectValue placeholder="Select locale" />
                </SelectTrigger>
                <SelectContent>
                  {LOCALES.map((loc) => (
                    <SelectItem key={loc.value} value={loc.value}>
                      {loc.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Subscription Info (Read-only) */}
      <Card>
        <CardHeader>
          <CardTitle>Subscription Information</CardTitle>
          <CardDescription>Current subscription details (read-only)</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <Label className="text-muted-foreground">Subscription Tier</Label>
              <p className="text-lg font-semibold mt-1">{settings?.subscriptionTier}</p>
            </div>
            <div>
              <Label className="text-muted-foreground">Status</Label>
              <p className="text-lg font-semibold mt-1">{settings?.status}</p>
            </div>
            <div>
              <Label className="text-muted-foreground">Total Shops</Label>
              <p className="text-lg font-semibold mt-1">{settings?.shopCount}</p>
            </div>
          </div>
          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              To change your subscription tier or manage billing, visit the Subscriptions page.
            </AlertDescription>
          </Alert>
        </CardContent>
      </Card>

      {/* Save Button */}
      <div className="flex justify-end gap-3">
        <Button variant="outline" onClick={loadTenantSettings} disabled={isSaving}>
          Cancel
        </Button>
        <Button onClick={handleSave} disabled={isSaving}>
          {isSaving ? (
            <>
              <div className="inline-block animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
              Saving...
            </>
          ) : (
            <>
              <Save className="h-4 w-4 mr-2" />
              Save Changes
            </>
          )}
        </Button>
      </div>
    </div>
  );
};

export default TenantSettingsPage;
