import React, { useState } from 'react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Cloud, Key, AlertCircle, CheckCircle, Loader2 } from 'lucide-react';

interface CloudConfigFormProps {
  onSubmit: (apiKey: string) => Promise<void>;
  onSkip: () => void;
  isValidating?: boolean;
}

/**
 * Cloud Configuration Form
 * Allows users to input API key and validate cloud connection
 */
export const CloudConfigForm: React.FC<CloudConfigFormProps> = ({
  onSubmit,
  onSkip,
  isValidating = false,
}) => {
  const [apiKey, setApiKey] = useState('');
  const [error, setError] = useState('');
  const [isValid, setIsValid] = useState(false);

  const validateApiKeyFormat = (key: string): boolean => {
    // API key should be 64 characters (UUID without dashes + hash)
    const trimmedKey = key.trim();
    if (trimmedKey.length === 0) {
      return false;
    }

    // Basic format validation (alphanumeric, 32-64 chars)
    const apiKeyPattern = /^[a-fA-F0-9]{32,64}$/;
    return apiKeyPattern.test(trimmedKey);
  };

  const handleApiKeyChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setApiKey(value);
    setError('');

    // Validate format
    if (value.trim().length > 0) {
      const isFormatValid = validateApiKeyFormat(value);
      setIsValid(isFormatValid);

      if (!isFormatValid && value.length >= 10) {
        setError('Invalid API key format. Please check and try again.');
      }
    } else {
      setIsValid(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!apiKey.trim()) {
      setError('Please enter your API key');
      return;
    }

    if (!validateApiKeyFormat(apiKey)) {
      setError('Invalid API key format');
      return;
    }

    try {
      await onSubmit(apiKey.trim());
    } catch (err: any) {
      setError(err.message || 'Failed to validate API key. Please check and try again.');
    }
  };

  const handlePaste = (e: React.ClipboardEvent) => {
    // Clean up pasted API key (remove spaces, newlines)
    const pastedText = e.clipboardData.getData('text');
    const cleanedKey = pastedText.replace(/[\s\n\r]/g, '');

    if (cleanedKey !== pastedText) {
      e.preventDefault();
      setApiKey(cleanedKey);

      // Trigger validation
      const isFormatValid = validateApiKeyFormat(cleanedKey);
      setIsValid(isFormatValid);
      if (!isFormatValid) {
        setError('Invalid API key format');
      }
    }
  };

  return (
    <Card className="w-full max-w-2xl mx-auto">
      <CardHeader>
        <div className="flex items-center gap-2 mb-2">
          <Cloud className="h-6 w-6 text-blue-600" />
          <CardTitle>Connect to RetailHQ Cloud</CardTitle>
        </div>
        <CardDescription>
          Enter your API key to enable cloud sync and access centralized reporting
        </CardDescription>
      </CardHeader>

      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* API Key Input */}
          <div className="space-y-2">
            <Label htmlFor="apiKey" className="flex items-center gap-2">
              <Key className="h-4 w-4" />
              API Key
            </Label>
            <Input
              id="apiKey"
              type="text"
              value={apiKey}
              onChange={handleApiKeyChange}
              onPaste={handlePaste}
              placeholder="Enter your 64-character API key"
              className={`font-mono ${isValid ? 'border-green-500' : ''}`}
              disabled={isValidating}
              autoComplete="off"
            />
            {isValid && !error && (
              <div className="flex items-center gap-1 text-sm text-green-600">
                <CheckCircle className="h-4 w-4" />
                <span>Valid format</span>
              </div>
            )}
          </div>

          {/* Error Message */}
          {error && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {/* Info Alert */}
          <Alert>
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>
              <strong>Don't have an API key?</strong>
              <br />
              You can register for a new RetailHQ Cloud account and receive your API key instantly.
              Click "Register New Tenant" below to get started.
            </AlertDescription>
          </Alert>

          {/* Actions */}
          <div className="flex flex-col sm:flex-row gap-3">
            <Button
              type="submit"
              disabled={!isValid || isValidating}
              className="flex-1"
            >
              {isValidating ? (
                <>
                  <Loader2 className="h-4 w-4 mr-2 animate-spin" />
                  Validating...
                </>
              ) : (
                'Connect to Cloud'
              )}
            </Button>

            <Button
              type="button"
              variant="outline"
              onClick={onSkip}
              disabled={isValidating}
              className="flex-1"
            >
              Skip for Now
            </Button>
          </div>

          {/* Register Link */}
          <div className="text-center pt-4 border-t">
            <p className="text-sm text-muted-foreground mb-3">
              New to RetailHQ Cloud?
            </p>
            <Button
              type="button"
              variant="link"
              onClick={() => window.open('/cloud/register', '_blank')}
              className="text-blue-600"
            >
              <Cloud className="h-4 w-4 mr-2" />
              Register New Tenant
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default CloudConfigForm;
