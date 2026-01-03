import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Checkbox } from '@/components/ui/checkbox';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Copy, AlertCircle, CheckCircle2 } from 'lucide-react';
import { CreateApiKeyRequest, CreateApiKeyResponse } from '@/services/cloudApiKeysService';

interface CreateApiKeyDialogProps {
  isOpen: boolean;
  onClose: () => void;
  tenantId: string;
  onSubmit: (request: CreateApiKeyRequest) => Promise<CreateApiKeyResponse>;
}

const AVAILABLE_PERMISSIONS = [
  { id: 'READ', label: 'Read', description: 'View shop data and analytics' },
  { id: 'WRITE', label: 'Write', description: 'Create and update records' },
  { id: 'DELETE', label: 'Delete', description: 'Delete records' },
  { id: 'SYNC', label: 'Sync', description: 'Synchronize with cloud' },
  { id: 'ADMIN', label: 'Admin', description: 'Full administrative access' },
];

/**
 * Create API Key Dialog Component
 * Form for creating new API keys with permission selection
 */
export const CreateApiKeyDialog: React.FC<CreateApiKeyDialogProps> = ({
  isOpen,
  onClose,
  tenantId,
  onSubmit,
}) => {
  const [description, setDescription] = useState('');
  const [expiresInDays, setExpiresInDays] = useState<string>('');
  const [selectedPermissions, setSelectedPermissions] = useState<string[]>(['READ']);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [createdKey, setCreatedKey] = useState<CreateApiKeyResponse | null>(null);
  const [copied, setCopied] = useState(false);

  const handlePermissionToggle = (permissionId: string) => {
    setSelectedPermissions((prev) =>
      prev.includes(permissionId)
        ? prev.filter((p) => p !== permissionId)
        : [...prev, permissionId]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsSubmitting(true);

    try {
      const request: CreateApiKeyRequest = {
        tenantId,
        description: description.trim(),
        expiresInDays: expiresInDays ? parseInt(expiresInDays, 10) : undefined,
        permissions: selectedPermissions,
      };

      const response = await onSubmit(request);
      setCreatedKey(response);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to create API key');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCopyKey = () => {
    if (createdKey) {
      navigator.clipboard.writeText(createdKey.fullKey);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleClose = () => {
    setDescription('');
    setExpiresInDays('');
    setSelectedPermissions(['READ']);
    setError(null);
    setCreatedKey(null);
    setCopied(false);
    onClose();
  };

  // If key was created, show success view
  if (createdKey) {
    return (
      <Dialog open={isOpen} onOpenChange={handleClose}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <CheckCircle2 className="h-5 w-5 text-green-600" />
              API Key Created Successfully
            </DialogTitle>
            <DialogDescription>
              Copy this key now. For security reasons, it won't be shown again.
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                <strong>Warning:</strong> {createdKey.warning}
              </AlertDescription>
            </Alert>

            <div>
              <Label>Your API Key</Label>
              <div className="flex gap-2 mt-2">
                <Input
                  value={createdKey.fullKey}
                  readOnly
                  className="font-mono text-sm"
                />
                <Button variant="outline" onClick={handleCopyKey}>
                  <Copy className="h-4 w-4 mr-1" />
                  {copied ? 'Copied!' : 'Copy'}
                </Button>
              </div>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <p className="text-sm text-blue-900">
                <strong>Description:</strong> {createdKey.apiKey.description}
              </p>
              <p className="text-sm text-blue-900 mt-1">
                <strong>Permissions:</strong>{' '}
                {createdKey.apiKey.permissions.join(', ')}
              </p>
            </div>
          </div>

          <DialogFooter>
            <Button onClick={handleClose}>Done</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    );
  }

  // Creation form
  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Create New API Key</DialogTitle>
          <DialogDescription>
            Generate a new API key for cloud synchronization
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
          )}

          {/* Description */}
          <div>
            <Label htmlFor="description">Description *</Label>
            <Input
              id="description"
              placeholder="e.g., Production sync key for Shop A"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
              className="mt-1"
            />
          </div>

          {/* Expiry */}
          <div>
            <Label htmlFor="expiresInDays">Expires In (days)</Label>
            <Input
              id="expiresInDays"
              type="number"
              min="1"
              max="365"
              placeholder="Leave empty for no expiry"
              value={expiresInDays}
              onChange={(e) => setExpiresInDays(e.target.value)}
              className="mt-1"
            />
            <p className="text-xs text-muted-foreground mt-1">
              Optional. Leave empty for keys that never expire.
            </p>
          </div>

          {/* Permissions */}
          <div>
            <Label>Permissions *</Label>
            <div className="space-y-3 mt-2">
              {AVAILABLE_PERMISSIONS.map((permission) => (
                <div key={permission.id} className="flex items-start gap-3">
                  <Checkbox
                    id={permission.id}
                    checked={selectedPermissions.includes(permission.id)}
                    onCheckedChange={() => handlePermissionToggle(permission.id)}
                  />
                  <div>
                    <Label
                      htmlFor={permission.id}
                      className="font-medium cursor-pointer"
                    >
                      {permission.label}
                    </Label>
                    <p className="text-xs text-muted-foreground">
                      {permission.description}
                    </p>
                  </div>
                </div>
              ))}
            </div>
            {selectedPermissions.length === 0 && (
              <p className="text-xs text-red-600 mt-1">
                Select at least one permission
              </p>
            )}
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={handleClose}>
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={
                isSubmitting ||
                !description.trim() ||
                selectedPermissions.length === 0
              }
            >
              {isSubmitting ? 'Creating...' : 'Create API Key'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default CreateApiKeyDialog;
