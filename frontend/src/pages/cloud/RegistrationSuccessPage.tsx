import React, { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
  Cloud,
  Copy,
  Download,
  Eye,
  EyeOff,
  CheckCircle,
  AlertCircle,
  Mail,
  FileText,
  ExternalLink,
  ArrowRight,
} from 'lucide-react';
import { toast } from 'sonner';
import { CloudTenant, CloudShop } from '@/services/cloudAggregatorService';

interface LocationState {
  apiKey: string;
  tenant: CloudTenant;
  shops: CloudShop[];
}

/**
 * Registration Success Page
 * Displays API key and next steps after successful tenant registration
 */

export const RegistrationSuccessPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const state = location.state as LocationState | null;

  const [showApiKey, setShowApiKey] = useState(false);
  const [apiKeyCopied, setApiKeyCopied] = useState(false);

  // Redirect if no state (direct access without registration)
  React.useEffect(() => {
    if (!state || !state.apiKey) {
      navigate('/cloud/register');
    }
  }, [state, navigate]);

  if (!state) {
    return null;
  }

  const { apiKey, tenant, shops } = state;

  const handleCopyApiKey = () => {
    navigator.clipboard.writeText(apiKey);
    setApiKeyCopied(true);
    toast.success('API key copied to clipboard');

    // Reset copied state after 3 seconds
    setTimeout(() => setApiKeyCopied(false), 3000);
  };

  const handleDownloadApiKey = () => {
    const content = `RetailHQ Cloud - API Key
=====================================

Tenant: ${tenant.tenantName}
Email: ${tenant.tenantEmail}
Tenant ID: ${tenant.id}

API Key: ${apiKey}

=====================================
IMPORTANT: Keep this API key secure!
=====================================

This API key is required to sync your local shops with RetailHQ Cloud.

Next Steps:
1. Download the RetailHQ installer from https://retailhq.app/download
2. Run the installer on your local shop computer
3. During setup, select "Cloud-Enabled Mode"
4. Paste this API key when prompted
5. Your shop will automatically sync with the cloud

For support: https://retailhq.app/support
Documentation: https://docs.retailhq.app

Generated: ${new Date().toLocaleString()}
`;

    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `retailhq-api-key-${tenant.id}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    toast.success('API key downloaded');
  };

  const handleEmailApiKey = () => {
    const subject = 'Your RetailHQ Cloud API Key';
    const body = `Your RetailHQ Cloud registration is complete!

Tenant: ${tenant.tenantName}
API Key: ${apiKey}

IMPORTANT: Keep this API key secure and do not share it with anyone.

Use this key to configure your local shop installations.

For support, visit https://retailhq.app/support`;

    window.location.href = `mailto:${tenant.tenantEmail}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
  };

  const maskedApiKey = `${apiKey.substring(0, 8)}${'•'.repeat(24)}${apiKey.substring(apiKey.length - 4)}`;

  return (
    <div className="min-h-screen bg-gradient-to-br from-green-50 via-white to-blue-50">
      {/* Navigation */}
      <nav className="border-b bg-white/80 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-4">
            <Link to="/" className="flex items-center space-x-2">
              <Cloud className="h-8 w-8 text-blue-600" />
              <span className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent">
                RetailHQ Cloud
              </span>
            </Link>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="py-12 px-4 sm:px-6 lg:px-8">
        <div className="max-w-4xl mx-auto">
          {/* Success Message */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-100 mb-4">
              <CheckCircle className="h-10 w-10 text-green-600" />
            </div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              Registration Successful!
            </h1>
            <p className="text-gray-600">
              Your RetailHQ Cloud account has been created successfully
            </p>
          </div>

          {/* API Key Card */}
          <Card className="shadow-2xl mb-6 border-2 border-amber-200 bg-amber-50">
            <CardHeader>
              <div className="flex items-center gap-2">
                <AlertCircle className="h-5 w-5 text-amber-600" />
                <CardTitle className="text-amber-900">Your API Key - Save This Now!</CardTitle>
              </div>
              <CardDescription className="text-amber-800">
                This API key is shown only once. Save it securely - you'll need it to configure your local shops.
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* API Key Display */}
              <div className="bg-white p-4 rounded-lg border-2 border-amber-300">
                <div className="flex items-center justify-between mb-2">
                  <label className="text-sm font-medium text-gray-700">API Key</label>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => setShowApiKey(!showApiKey)}
                  >
                    {showApiKey ? (
                      <>
                        <EyeOff className="h-4 w-4 mr-2" />
                        Hide
                      </>
                    ) : (
                      <>
                        <Eye className="h-4 w-4 mr-2" />
                        Show
                      </>
                    )}
                  </Button>
                </div>
                <code className="block text-sm font-mono bg-gray-50 p-3 rounded border break-all">
                  {showApiKey ? apiKey : maskedApiKey}
                </code>
              </div>

              {/* Action Buttons */}
              <div className="grid md:grid-cols-3 gap-3">
                <Button onClick={handleCopyApiKey} variant="default" className="w-full">
                  {apiKeyCopied ? (
                    <>
                      <CheckCircle className="h-4 w-4 mr-2" />
                      Copied!
                    </>
                  ) : (
                    <>
                      <Copy className="h-4 w-4 mr-2" />
                      Copy Key
                    </>
                  )}
                </Button>
                <Button onClick={handleDownloadApiKey} variant="outline" className="w-full">
                  <Download className="h-4 w-4 mr-2" />
                  Download
                </Button>
                <Button onClick={handleEmailApiKey} variant="outline" className="w-full">
                  <Mail className="h-4 w-4 mr-2" />
                  Email to Me
                </Button>
              </div>
            </CardContent>
          </Card>

          {/* Account Summary */}
          <div className="grid md:grid-cols-2 gap-6 mb-6">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Account Details</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2 text-sm">
                <div>
                  <span className="font-medium">Business:</span> {tenant.tenantName}
                </div>
                <div>
                  <span className="font-medium">Email:</span> {tenant.tenantEmail}
                </div>
                <div>
                  <span className="font-medium">Plan:</span>{' '}
                  <span className="inline-flex px-2 py-1 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                    {tenant.subscriptionTier}
                  </span>
                </div>
                <div>
                  <span className="font-medium">Shops:</span> {shops.length} registered
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">Registered Shops</CardTitle>
              </CardHeader>
              <CardContent>
                <ul className="space-y-2 text-sm">
                  {shops.map((shop) => (
                    <li key={shop.id} className="flex items-center gap-2">
                      <CheckCircle className="h-4 w-4 text-green-600" />
                      <span>{shop.shopName}</span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          </div>

          {/* Next Steps */}
          <Card>
            <CardHeader>
              <CardTitle>Next Steps</CardTitle>
              <CardDescription>Follow these steps to start using RetailHQ Cloud</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex gap-4">
                  <div className="flex-shrink-0 w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center font-bold text-blue-600">
                    1
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">
                      Download RetailHQ Installer
                    </h4>
                    <p className="text-sm text-gray-600 mb-2">
                      Download the installer for your local shop computers
                    </p>
                    <Button variant="outline" size="sm" asChild>
                      <a href="https://retailhq.app/download" target="_blank" rel="noopener noreferrer">
                        <Download className="h-4 w-4 mr-2" />
                        Download Installer
                        <ExternalLink className="h-3 w-3 ml-1" />
                      </a>
                    </Button>
                  </div>
                </div>

                <div className="flex gap-4">
                  <div className="flex-shrink-0 w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center font-bold text-blue-600">
                    2
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Install on Shop Computers</h4>
                    <p className="text-sm text-gray-600">
                      Run the installer on each shop computer and select "Cloud-Enabled Mode"
                    </p>
                  </div>
                </div>

                <div className="flex gap-4">
                  <div className="flex-shrink-0 w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center font-bold text-blue-600">
                    3
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Configure with API Key</h4>
                    <p className="text-sm text-gray-600">
                      Paste your API key during installation to enable cloud sync
                    </p>
                  </div>
                </div>

                <div className="flex gap-4">
                  <div className="flex-shrink-0 w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center font-bold text-blue-600">
                    4
                  </div>
                  <div>
                    <h4 className="font-semibold text-gray-900 mb-1">Start Selling!</h4>
                    <p className="text-sm text-gray-600">
                      Your shops will automatically sync data to the cloud
                    </p>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Documentation Links */}
          <Card className="mt-6 bg-gray-50">
            <CardHeader>
              <CardTitle className="text-base">Need Help?</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid md:grid-cols-2 gap-4 text-sm">
                <a
                  href="https://docs.retailhq.app/getting-started"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 text-blue-600 hover:underline"
                >
                  <FileText className="h-4 w-4" />
                  Getting Started Guide
                  <ExternalLink className="h-3 w-3" />
                </a>
                <a
                  href="https://docs.retailhq.app/cloud-sync"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 text-blue-600 hover:underline"
                >
                  <FileText className="h-4 w-4" />
                  Cloud Sync Setup
                  <ExternalLink className="h-3 w-3" />
                </a>
                <a
                  href="https://retailhq.app/support"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 text-blue-600 hover:underline"
                >
                  <Mail className="h-4 w-4" />
                  Contact Support
                  <ExternalLink className="h-3 w-3" />
                </a>
                <a
                  href="https://docs.retailhq.app"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center gap-2 text-blue-600 hover:underline"
                >
                  <FileText className="h-4 w-4" />
                  Full Documentation
                  <ExternalLink className="h-3 w-3" />
                </a>
              </div>
            </CardContent>
          </Card>

          {/* CTA */}
          <div className="mt-8 text-center">
            <Button size="lg" asChild>
              <Link to="/cloud/tenants">
                Go to Dashboard
                <ArrowRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
            <p className="text-sm text-gray-600 mt-4">
              Access your cloud dashboard to manage shops and view analytics
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegistrationSuccessPage;
