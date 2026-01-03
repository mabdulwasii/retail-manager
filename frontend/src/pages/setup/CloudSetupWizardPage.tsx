import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Progress } from '@/components/ui/progress';
import {
  Cloud,
  Store,
  CheckCircle,
  AlertCircle,
  Loader2,
  ArrowLeft,
  ArrowRight,
  Sparkles
} from 'lucide-react';
import { CloudConfigForm } from '@/components/setup/CloudConfigForm';
import { ShopLinkageSelector } from '@/components/setup/ShopLinkageSelector';
import { useFirstRunDetection } from '@/hooks/useFirstRunDetection';
import { useRegisterTenant } from '@/hooks/useCloudTenants';
import { useQuery } from '@tanstack/react-query';
import shopService from '@/services/shopService';

/**
 * Cloud Setup Wizard Page
 * Multi-step wizard for configuring cloud sync during first-run installation
 *
 * Steps:
 * 1. Welcome - Introduction
 * 2. Mode Selection - Standalone vs Cloud-enabled
 * 3. Cloud Config - API key input (if cloud-enabled)
 * 4. Shop Linkage - Select shop to link
 * 5. Connection Test - Validate and save
 * 6. Complete - Success message
 */

enum SetupStep {
  WELCOME = 0,
  MODE_SELECTION = 1,
  CLOUD_CONFIG = 2,
  SHOP_LINKAGE = 3,
  CONNECTION_TEST = 4,
  COMPLETE = 5,
}

export const CloudSetupWizardPage: React.FC = () => {
  const navigate = useNavigate();
  const { markSetupComplete } = useFirstRunDetection();

  const [currentStep, setCurrentStep] = useState<SetupStep>(SetupStep.WELCOME);
  const [setupMode, setSetupMode] = useState<'standalone' | 'cloud' | null>(null);
  const [apiKey, setApiKey] = useState<string>('');
  const [tenantId, setTenantId] = useState<string>('');
  const [selectedShopId, setSelectedShopId] = useState<string | null>(null);
  const [isValidating, setIsValidating] = useState(false);
  const [validationError, setValidationError] = useState<string>('');

  // Fetch shops for linkage
  const { data: shops = [], isLoading: isLoadingShops } = useQuery({
    queryKey: ['shops'],
    queryFn: () => shopService.getShops(),
    enabled: currentStep === SetupStep.SHOP_LINKAGE,
  });

  // Calculate progress percentage
  const totalSteps = setupMode === 'standalone' ? 3 : 6;
  const progressPercentage = (currentStep / totalSteps) * 100;

  const handleNext = () => {
    if (currentStep === SetupStep.MODE_SELECTION && setupMode === 'standalone') {
      // Skip cloud setup steps
      completeStandaloneSetup();
    } else {
      setCurrentStep((prev) => Math.min(prev + 1, SetupStep.COMPLETE));
    }
  };

  const handleBack = () => {
    setCurrentStep((prev) => Math.max(prev - 1, SetupStep.WELCOME));
  };

  const completeStandaloneSetup = async () => {
    try {
      await markSetupComplete();
      setCurrentStep(SetupStep.COMPLETE);
    } catch (error) {
      console.error('Failed to complete standalone setup:', error);
    }
  };

  const handleApiKeySubmit = async (key: string) => {
    setIsValidating(true);
    setValidationError('');

    try {
      // TODO: Call backend API to validate API key
      // For now, simulate validation
      await new Promise((resolve) => setTimeout(resolve, 1500));

      // Mock successful validation
      setApiKey(key);
      setTenantId('tenant-' + key.substring(0, 8)); // Mock tenant ID
      handleNext();
    } catch (error: any) {
      setValidationError(error.message || 'Failed to validate API key');
      throw error;
    } finally {
      setIsValidating(false);
    }
  };

  const handleSkipCloudSetup = () => {
    setSetupMode('standalone');
    completeStandaloneSetup();
  };

  const handleConnectionTest = async () => {
    setIsValidating(true);
    setValidationError('');

    try {
      // TODO: Call backend API to test connection and configure sync
      // POST /api/cloud/sync/configure
      await new Promise((resolve) => setTimeout(resolve, 2000));

      // Save cloud configuration
      await markSetupComplete({
        apiKey,
        tenantId,
        shopId: selectedShopId!,
        cloudSyncEnabled: true,
        configuredAt: new Date().toISOString(),
      });

      setCurrentStep(SetupStep.COMPLETE);
    } catch (error: any) {
      setValidationError(error.message || 'Failed to configure cloud sync');
    } finally {
      setIsValidating(false);
    }
  };

  const handleFinish = () => {
    navigate('/');
  };

  // Render step content
  const renderStepContent = () => {
    switch (currentStep) {
      case SetupStep.WELCOME:
        return (
          <Card className="w-full max-w-2xl mx-auto">
            <CardHeader className="text-center">
              <div className="flex justify-center mb-4">
                <div className="rounded-full bg-blue-100 p-4">
                  <Sparkles className="h-12 w-12 text-blue-600" />
                </div>
              </div>
              <CardTitle className="text-3xl">Welcome to RetailHQ</CardTitle>
              <CardDescription className="text-lg mt-2">
                Let's get your shop management system set up in just a few steps
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-4">
                <div className="flex items-start gap-3">
                  <CheckCircle className="h-5 w-5 text-green-600 mt-0.5" />
                  <div>
                    <h4 className="font-semibold">Manage Sales & Inventory</h4>
                    <p className="text-sm text-muted-foreground">
                      Track products, manage stock, and process sales effortlessly
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <CheckCircle className="h-5 w-5 text-green-600 mt-0.5" />
                  <div>
                    <h4 className="font-semibold">Optional Cloud Sync</h4>
                    <p className="text-sm text-muted-foreground">
                      Enable cloud sync for centralized reporting and multi-shop management
                    </p>
                  </div>
                </div>
                <div className="flex items-start gap-3">
                  <CheckCircle className="h-5 w-5 text-green-600 mt-0.5" />
                  <div>
                    <h4 className="font-semibold">Advanced Analytics</h4>
                    <p className="text-sm text-muted-foreground">
                      Get insights into sales trends, top products, and performance metrics
                    </p>
                  </div>
                </div>
              </div>

              <Button onClick={handleNext} className="w-full" size="lg">
                Get Started
                <ArrowRight className="h-4 w-4 ml-2" />
              </Button>
            </CardContent>
          </Card>
        );

      case SetupStep.MODE_SELECTION:
        return (
          <Card className="w-full max-w-2xl mx-auto">
            <CardHeader>
              <CardTitle>Choose Your Setup Mode</CardTitle>
              <CardDescription>
                Select how you want to use RetailHQ
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div
                className={`relative flex items-start space-x-3 rounded-lg border p-6 cursor-pointer transition-all hover:shadow-md ${
                  setupMode === 'cloud'
                    ? 'border-blue-500 bg-blue-50 ring-2 ring-blue-500'
                    : 'border-gray-200 hover:border-blue-300'
                }`}
                onClick={() => setSetupMode('cloud')}
              >
                <Cloud className="h-6 w-6 text-blue-600 mt-1" />
                <div className="flex-1">
                  <h4 className="font-semibold text-lg">Cloud-Enabled Mode</h4>
                  <p className="text-sm text-muted-foreground mt-1">
                    Connect to RetailHQ Cloud for centralized reporting, multi-shop management,
                    and advanced analytics. Requires internet connection.
                  </p>
                  <ul className="mt-3 space-y-1 text-sm text-muted-foreground">
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-600" />
                      Centralized dashboard across all shops
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-600" />
                      Real-time sync and backup
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-600" />
                      Advanced analytics and reporting
                    </li>
                  </ul>
                </div>
              </div>

              <div
                className={`relative flex items-start space-x-3 rounded-lg border p-6 cursor-pointer transition-all hover:shadow-md ${
                  setupMode === 'standalone'
                    ? 'border-blue-500 bg-blue-50 ring-2 ring-blue-500'
                    : 'border-gray-200 hover:border-blue-300'
                }`}
                onClick={() => setSetupMode('standalone')}
              >
                <Store className="h-6 w-6 text-gray-600 mt-1" />
                <div className="flex-1">
                  <h4 className="font-semibold text-lg">Standalone Mode</h4>
                  <p className="text-sm text-muted-foreground mt-1">
                    Use RetailHQ locally on this device only. All data stays on your machine.
                    No internet connection required.
                  </p>
                  <ul className="mt-3 space-y-1 text-sm text-muted-foreground">
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-600" />
                      Works offline
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-600" />
                      Complete data privacy
                    </li>
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-600" />
                      No subscription required
                    </li>
                  </ul>
                </div>
              </div>

              <div className="flex gap-3 mt-6">
                <Button variant="outline" onClick={handleBack} className="flex-1">
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back
                </Button>
                <Button onClick={handleNext} disabled={!setupMode} className="flex-1">
                  Continue
                  <ArrowRight className="h-4 w-4 ml-2" />
                </Button>
              </div>
            </CardContent>
          </Card>
        );

      case SetupStep.CLOUD_CONFIG:
        return (
          <div className="space-y-4">
            <CloudConfigForm
              onSubmit={handleApiKeySubmit}
              onSkip={handleSkipCloudSetup}
              isValidating={isValidating}
            />
            <div className="text-center">
              <Button variant="ghost" onClick={handleBack}>
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back
              </Button>
            </div>
          </div>
        );

      case SetupStep.SHOP_LINKAGE:
        return (
          <div className="space-y-4">
            <ShopLinkageSelector
              shops={shops}
              selectedShopId={selectedShopId}
              onSelect={setSelectedShopId}
              isLoading={isLoadingShops}
            />
            <div className="flex gap-3 max-w-2xl mx-auto">
              <Button variant="outline" onClick={handleBack} className="flex-1">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back
              </Button>
              <Button onClick={handleNext} disabled={!selectedShopId} className="flex-1">
                Continue
                <ArrowRight className="h-4 w-4 ml-2" />
              </Button>
            </div>
          </div>
        );

      case SetupStep.CONNECTION_TEST:
        return (
          <Card className="w-full max-w-2xl mx-auto">
            <CardHeader>
              <CardTitle>Test Connection</CardTitle>
              <CardDescription>
                Let's verify your cloud connection and complete the setup
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-3">
                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <span className="text-sm font-medium">API Key</span>
                  <span className="text-sm font-mono text-muted-foreground">
                    {apiKey.substring(0, 8)}...{apiKey.substring(apiKey.length - 8)}
                  </span>
                </div>
                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <span className="text-sm font-medium">Tenant ID</span>
                  <span className="text-sm text-muted-foreground">{tenantId}</span>
                </div>
                <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                  <span className="text-sm font-medium">Shop</span>
                  <span className="text-sm text-muted-foreground">
                    {shops.find(s => s.id === selectedShopId)?.name}
                  </span>
                </div>
              </div>

              {validationError && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{validationError}</AlertDescription>
                </Alert>
              )}

              <div className="flex gap-3">
                <Button variant="outline" onClick={handleBack} disabled={isValidating} className="flex-1">
                  <ArrowLeft className="h-4 w-4 mr-2" />
                  Back
                </Button>
                <Button onClick={handleConnectionTest} disabled={isValidating} className="flex-1">
                  {isValidating ? (
                    <>
                      <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                      Testing Connection...
                    </>
                  ) : (
                    <>
                      Test & Complete Setup
                      <CheckCircle className="h-4 w-4 ml-2" />
                    </>
                  )}
                </Button>
              </div>
            </CardContent>
          </Card>
        );

      case SetupStep.COMPLETE:
        return (
          <Card className="w-full max-w-2xl mx-auto">
            <CardHeader className="text-center">
              <div className="flex justify-center mb-4">
                <div className="rounded-full bg-green-100 p-4">
                  <CheckCircle className="h-12 w-12 text-green-600" />
                </div>
              </div>
              <CardTitle className="text-3xl">Setup Complete!</CardTitle>
              <CardDescription className="text-lg mt-2">
                {setupMode === 'cloud'
                  ? 'Your shop is now connected to RetailHQ Cloud'
                  : 'Your shop is ready to use in standalone mode'}
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {setupMode === 'cloud' && (
                <Alert className="bg-blue-50 border-blue-200">
                  <Cloud className="h-4 w-4 text-blue-600" />
                  <AlertDescription>
                    Cloud sync is now active. Your data will be automatically backed up and
                    synced to RetailHQ Cloud.
                  </AlertDescription>
                </Alert>
              )}

              <div className="space-y-3">
                <h4 className="font-semibold">What's Next?</h4>
                <ul className="space-y-2 text-sm text-muted-foreground">
                  <li className="flex items-center gap-2">
                    <CheckCircle className="h-4 w-4 text-green-600" />
                    Add products to your inventory
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="h-4 w-4 text-green-600" />
                    Create your first sale
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle className="h-4 w-4 text-green-600" />
                    Invite team members
                  </li>
                  {setupMode === 'cloud' && (
                    <li className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-600" />
                      View analytics dashboard
                    </li>
                  )}
                </ul>
              </div>

              <Button onClick={handleFinish} className="w-full" size="lg">
                Launch RetailHQ
                <ArrowRight className="h-4 w-4 ml-2" />
              </Button>
            </CardContent>
          </Card>
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50 py-12 px-4">
      <div className="max-w-4xl mx-auto">
        {/* Progress Bar */}
        {currentStep > SetupStep.WELCOME && currentStep < SetupStep.COMPLETE && (
          <div className="mb-8">
            <Progress value={progressPercentage} className="h-2" />
            <p className="text-sm text-muted-foreground text-center mt-2">
              Step {currentStep} of {totalSteps}
            </p>
          </div>
        )}

        {/* Step Content */}
        {renderStepContent()}
      </div>
    </div>
  );
};

export default CloudSetupWizardPage;
