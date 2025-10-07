import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger, DialogFooter } from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Switch } from '@/components/ui/switch'
import { useAuth } from '@/context/ManualAuthContext'
import {
  useFraudDetection,
  FraudRule,
  CreateFraudRuleRequest,
  FraudRuleType,
  RiskLevel,
  PaginatedResponse
} from '@/hooks/useFraudDetection'
import {
  FilterIcon,
  PlusIcon,
  EditIcon,
  TrashIcon,
  ShieldIcon,
  SettingsIcon,
  AlertTriangleIcon,
  CheckCircleIcon,
  XCircleIcon,
  InfoIcon
} from 'lucide-react'

interface FraudRuleListProps {
  shopId?: string
  viewMode?: 'shop' | 'admin'
}

export const FraudRuleList: React.FC<FraudRuleListProps> = ({
  shopId,
  viewMode = 'admin'
}) => {
  const { user } = useAuth()
  const {
    getFraudRules,
    createFraudRule,
    updateFraudRule,
    deleteFraudRule,
    updateRuleStatus,
    isLoading
  } = useFraudDetection()

  const [rules, setRules] = useState<FraudRule[]>([])
  const [totalPages, setTotalPages] = useState(0)
  const [currentPage, setCurrentPage] = useState(0)
  const [isRefreshing, setIsRefreshing] = useState(false)

  // Filters
  const [ruleTypeFilter, setRuleTypeFilter] = useState<FraudRuleType | ''>('')
  const [enabledFilter, setEnabledFilter] = useState<boolean | ''>('')
  const [searchQuery, setSearchQuery] = useState('')

  // Dialog states
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false)
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false)
  const [selectedRule, setSelectedRule] = useState<FraudRule | null>(null)
  const [isProcessing, setIsProcessing] = useState(false)

  // Form state
  const [formData, setFormData] = useState<CreateFraudRuleRequest>({
    ruleName: '',
    ruleType: 'HIGH_AMOUNT_TRANSACTION',
    description: '',
    enabled: true,
    thresholdAmount: undefined,
    thresholdCount: undefined,
    timeWindowMinutes: undefined,
    riskScoreWeight: 1.0,
    severity: 'MEDIUM',
    autoBlock: false,
    requiresManualReview: true,
    ruleConfiguration: undefined
  })

  useEffect(() => {
    fetchRules()
  }, [currentPage, ruleTypeFilter, enabledFilter, searchQuery, shopId])

  const fetchRules = async () => {
    try {
      setIsRefreshing(true)
      const response = await getFraudRules({
        shopId,
        ruleType: ruleTypeFilter || undefined,
        enabled: enabledFilter !== '' ? enabledFilter : undefined,
        page: currentPage,
        size: 10,
        sortBy: 'createdAt',
        sortDir: 'desc'
      })

      if (response) {
        let filteredRules = response.content

        if (searchQuery) {
          filteredRules = filteredRules.filter(rule =>
            rule.ruleName.toLowerCase().includes(searchQuery.toLowerCase()) ||
            (rule.description && rule.description.toLowerCase().includes(searchQuery.toLowerCase()))
          )
        }

        setRules(filteredRules)
        setTotalPages(response.totalPages)
      }
    } catch (error) {
      console.error('Failed to fetch fraud rules:', error)
    } finally {
      setIsRefreshing(false)
    }
  }

  const getRuleTypeDisplay = (type: FraudRuleType) => {
    const typeMap: Record<FraudRuleType, string> = {
      'HIGH_AMOUNT_TRANSACTION': 'High Amount Transaction',
      'HIGH_FREQUENCY_TRANSACTIONS': 'High Frequency Transactions',
      'UNUSUAL_TIME_TRANSACTION': 'Unusual Time Transaction',
      'RAPID_SUCCESSIVE_TRANSACTIONS': 'Rapid Successive Transactions',
      'UNUSUAL_PAYMENT_METHOD': 'Unusual Payment Method',
      'SUSPICIOUS_CUSTOMER_PATTERN': 'Suspicious Customer Pattern',
      'INVENTORY_MISMATCH': 'Inventory Mismatch',
      'GEOGRAPHIC_ANOMALY': 'Geographic Anomaly',
      'VELOCITY_CHECK': 'Velocity Check',
      'BLACKLIST_CHECK': 'Blacklist Check',
      'CUSTOM_RULE': 'Custom Rule'
    }
    return typeMap[type] || type
  }

  const getRiskLevelColor = (level: RiskLevel) => {
    switch (level) {
      case 'CRITICAL': return 'bg-red-100 text-red-800 border-red-200'
      case 'HIGH': return 'bg-orange-100 text-orange-800 border-orange-200'
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-200'
      case 'LOW': return 'bg-blue-100 text-blue-800 border-blue-200'
      default: return 'bg-gray-100 text-gray-800 border-gray-200'
    }
  }

  const formatNaira = (amount: number) => {
    return new Intl.NumberFormat('en-NG', {
      style: 'currency',
      currency: 'NGN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount)
  }

  const handleCreateRule = async () => {
    try {
      setIsProcessing(true)
      const request: CreateFraudRuleRequest = {
        ...formData,
        shopId: viewMode === 'shop' ? shopId : undefined
      }

      const result = await createFraudRule(request)
      if (result) {
        setRules(prev => [result, ...prev])
        setIsCreateDialogOpen(false)
        resetForm()
      }
    } catch (error) {
      console.error('Failed to create fraud rule:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleUpdateRule = async () => {
    if (!selectedRule) return

    try {
      setIsProcessing(true)
      const result = await updateFraudRule(selectedRule.id, formData)
      if (result) {
        setRules(prev =>
          prev.map(rule => rule.id === selectedRule.id ? result : rule)
        )
        setIsEditDialogOpen(false)
        setSelectedRule(null)
        resetForm()
      }
    } catch (error) {
      console.error('Failed to update fraud rule:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleDeleteRule = async () => {
    if (!selectedRule) return

    try {
      setIsProcessing(true)
      const success = await deleteFraudRule(selectedRule.id)
      if (success) {
        setRules(prev => prev.filter(rule => rule.id !== selectedRule.id))
        setIsDeleteDialogOpen(false)
        setSelectedRule(null)
      }
    } catch (error) {
      console.error('Failed to delete fraud rule:', error)
    } finally {
      setIsProcessing(false)
    }
  }

  const handleToggleRuleStatus = async (rule: FraudRule) => {
    try {
      const result = await updateRuleStatus(rule.id, !rule.enabled)
      if (result) {
        setRules(prev =>
          prev.map(r => r.id === rule.id ? result : r)
        )
      }
    } catch (error) {
      console.error('Failed to update rule status:', error)
    }
  }

  const openEditDialog = (rule: FraudRule) => {
    setSelectedRule(rule)
    setFormData({
      ruleName: rule.ruleName,
      ruleType: rule.ruleType,
      description: rule.description || '',
      enabled: rule.enabled,
      thresholdAmount: rule.thresholdAmount,
      thresholdCount: rule.thresholdCount,
      timeWindowMinutes: rule.timeWindowMinutes,
      riskScoreWeight: rule.riskScoreWeight,
      severity: rule.severity,
      autoBlock: rule.autoBlock,
      requiresManualReview: rule.requiresManualReview,
      ruleConfiguration: rule.ruleConfiguration
    })
    setIsEditDialogOpen(true)
  }

  const openDeleteDialog = (rule: FraudRule) => {
    setSelectedRule(rule)
    setIsDeleteDialogOpen(true)
  }

  const resetForm = () => {
    setFormData({
      ruleName: '',
      ruleType: 'HIGH_AMOUNT_TRANSACTION',
      description: '',
      enabled: true,
      thresholdAmount: undefined,
      thresholdCount: undefined,
      timeWindowMinutes: undefined,
      riskScoreWeight: 1.0,
      severity: 'MEDIUM',
      autoBlock: false,
      requiresManualReview: true,
      ruleConfiguration: undefined
    })
  }

  const renderRuleForm = () => (
    <div className="space-y-4">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <Label htmlFor="ruleName">Rule Name</Label>
          <Input
            id="ruleName"
            value={formData.ruleName}
            onChange={(e) => setFormData(prev => ({ ...prev, ruleName: e.target.value }))}
            placeholder="Enter rule name"
          />
        </div>
        <div>
          <Label htmlFor="ruleType">Rule Type</Label>
          <Select
            value={formData.ruleType}
            onValueChange={(value) => setFormData(prev => ({ ...prev, ruleType: value as FraudRuleType }))}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="HIGH_AMOUNT_TRANSACTION">High Amount Transaction</SelectItem>
              <SelectItem value="HIGH_FREQUENCY_TRANSACTIONS">High Frequency Transactions</SelectItem>
              <SelectItem value="UNUSUAL_TIME_TRANSACTION">Unusual Time Transaction</SelectItem>
              <SelectItem value="RAPID_SUCCESSIVE_TRANSACTIONS">Rapid Successive Transactions</SelectItem>
              <SelectItem value="UNUSUAL_PAYMENT_METHOD">Unusual Payment Method</SelectItem>
              <SelectItem value="SUSPICIOUS_CUSTOMER_PATTERN">Suspicious Customer Pattern</SelectItem>
              <SelectItem value="INVENTORY_MISMATCH">Inventory Mismatch</SelectItem>
              <SelectItem value="GEOGRAPHIC_ANOMALY">Geographic Anomaly</SelectItem>
              <SelectItem value="VELOCITY_CHECK">Velocity Check</SelectItem>
              <SelectItem value="BLACKLIST_CHECK">Blacklist Check</SelectItem>
              <SelectItem value="CUSTOM_RULE">Custom Rule</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <div>
        <Label htmlFor="description">Description</Label>
        <Textarea
          id="description"
          value={formData.description}
          onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
          placeholder="Describe what this rule detects"
          rows={3}
        />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <Label htmlFor="thresholdAmount">Threshold Amount (₦)</Label>
          <Input
            id="thresholdAmount"
            type="number"
            value={formData.thresholdAmount || ''}
            onChange={(e) => setFormData(prev => ({
              ...prev,
              thresholdAmount: e.target.value ? parseFloat(e.target.value) : undefined
            }))}
            placeholder="0"
          />
        </div>
        <div>
          <Label htmlFor="thresholdCount">Threshold Count</Label>
          <Input
            id="thresholdCount"
            type="number"
            value={formData.thresholdCount || ''}
            onChange={(e) => setFormData(prev => ({
              ...prev,
              thresholdCount: e.target.value ? parseInt(e.target.value) : undefined
            }))}
            placeholder="0"
          />
        </div>
        <div>
          <Label htmlFor="timeWindow">Time Window (minutes)</Label>
          <Input
            id="timeWindow"
            type="number"
            value={formData.timeWindowMinutes || ''}
            onChange={(e) => setFormData(prev => ({
              ...prev,
              timeWindowMinutes: e.target.value ? parseInt(e.target.value) : undefined
            }))}
            placeholder="0"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <Label htmlFor="riskScoreWeight">Risk Score Weight</Label>
          <Input
            id="riskScoreWeight"
            type="number"
            step="0.1"
            min="0"
            max="10"
            value={formData.riskScoreWeight}
            onChange={(e) => setFormData(prev => ({
              ...prev,
              riskScoreWeight: parseFloat(e.target.value) || 1.0
            }))}
          />
        </div>
        <div>
          <Label htmlFor="severity">Severity</Label>
          <Select
            value={formData.severity}
            onValueChange={(value) => setFormData(prev => ({ ...prev, severity: value as RiskLevel }))}
          >
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="LOW">Low</SelectItem>
              <SelectItem value="MEDIUM">Medium</SelectItem>
              <SelectItem value="HIGH">High</SelectItem>
              <SelectItem value="CRITICAL">Critical</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <Switch
            id="enabled"
            checked={formData.enabled}
            onCheckedChange={(checked) => setFormData(prev => ({ ...prev, enabled: checked }))}
          />
          <Label htmlFor="enabled">Rule Enabled</Label>
        </div>
        <div className="flex items-center space-x-2">
          <Switch
            id="autoBlock"
            checked={formData.autoBlock}
            onCheckedChange={(checked) => setFormData(prev => ({ ...prev, autoBlock: checked }))}
          />
          <Label htmlFor="autoBlock">Auto Block</Label>
        </div>
        <div className="flex items-center space-x-2">
          <Switch
            id="manualReview"
            checked={formData.requiresManualReview}
            onCheckedChange={(checked) => setFormData(prev => ({ ...prev, requiresManualReview: checked }))}
          />
          <Label htmlFor="manualReview">Manual Review</Label>
        </div>
      </div>

      {formData.ruleType === 'CUSTOM_RULE' && (
        <div>
          <Label htmlFor="ruleConfiguration">Rule Configuration (JSON)</Label>
          <Textarea
            id="ruleConfiguration"
            value={formData.ruleConfiguration || ''}
            onChange={(e) => setFormData(prev => ({ ...prev, ruleConfiguration: e.target.value }))}
            placeholder='{"conditions": [], "actions": []}'
            rows={4}
          />
        </div>
      )}
    </div>
  )

  if (isLoading && rules.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold flex items-center space-x-2">
            <ShieldIcon className="h-6 w-6 text-blue-600" />
            <span>Fraud Detection Rules</span>
          </h2>
          <p className="text-gray-600 mt-1">
            Configure automated fraud detection rules for your {viewMode === 'shop' ? 'shop' : 'system'}
          </p>
        </div>
        <Dialog open={isCreateDialogOpen} onOpenChange={setIsCreateDialogOpen}>
          <DialogTrigger asChild>
            <Button onClick={resetForm}>
              <PlusIcon className="h-4 w-4 mr-2" />
              Add Rule
            </Button>
          </DialogTrigger>
          <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Create Fraud Detection Rule</DialogTitle>
            </DialogHeader>
            {renderRuleForm()}
            <DialogFooter>
              <Button variant="outline" onClick={() => setIsCreateDialogOpen(false)}>
                Cancel
              </Button>
              <Button onClick={handleCreateRule} disabled={isProcessing || !formData.ruleName}>
                {isProcessing ? <LoadingSpinner size="sm" /> : 'Create Rule'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <FilterIcon className="h-5 w-5" />
            <span>Filters</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div>
              <Label>Search</Label>
              <Input
                placeholder="Search rules..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div>
              <Label>Rule Type</Label>
              <Select value={ruleTypeFilter} onValueChange={setRuleTypeFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All types" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">All Types</SelectItem>
                  <SelectItem value="HIGH_AMOUNT_TRANSACTION">High Amount Transaction</SelectItem>
                  <SelectItem value="HIGH_FREQUENCY_TRANSACTIONS">High Frequency Transactions</SelectItem>
                  <SelectItem value="UNUSUAL_TIME_TRANSACTION">Unusual Time Transaction</SelectItem>
                  <SelectItem value="RAPID_SUCCESSIVE_TRANSACTIONS">Rapid Successive Transactions</SelectItem>
                  <SelectItem value="SUSPICIOUS_CUSTOMER_PATTERN">Suspicious Customer Pattern</SelectItem>
                  <SelectItem value="CUSTOM_RULE">Custom Rule</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label>Status</Label>
              <Select value={enabledFilter.toString()} onValueChange={(value) => setEnabledFilter(value === '' ? '' : value === 'true')}>
                <SelectTrigger>
                  <SelectValue placeholder="All statuses" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">All Statuses</SelectItem>
                  <SelectItem value="true">Enabled</SelectItem>
                  <SelectItem value="false">Disabled</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-end">
              <Button variant="outline" onClick={fetchRules} disabled={isRefreshing}>
                {isRefreshing ? <LoadingSpinner size="sm" /> : 'Refresh'}
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Rules List */}
      <div className="space-y-4">
        {rules.map((rule) => (
          <Card key={rule.id} className="hover:shadow-md transition-shadow">
            <CardContent className="p-6">
              <div className="flex items-start justify-between">
                <div className="flex-1 space-y-3">
                  <div className="flex items-center space-x-3">
                    <h3 className="text-lg font-semibold">{rule.ruleName}</h3>
                    <Badge className={getRiskLevelColor(rule.severity)}>
                      {rule.severity}
                    </Badge>
                    <Badge variant={rule.enabled ? 'default' : 'secondary'}>
                      {rule.enabled ? (
                        <><CheckCircleIcon className="h-3 w-3 mr-1" />Enabled</>
                      ) : (
                        <><XCircleIcon className="h-3 w-3 mr-1" />Disabled</>
                      )}
                    </Badge>
                    {rule.autoBlock && (
                      <Badge className="bg-red-100 text-red-800">
                        Auto Block
                      </Badge>
                    )}
                    {rule.requiresManualReview && (
                      <Badge className="bg-yellow-100 text-yellow-800">
                        Manual Review
                      </Badge>
                    )}
                  </div>

                  <p className="text-gray-600">{rule.description || 'No description provided'}</p>

                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                    <div>
                      <span className="font-medium text-gray-500">Type:</span>
                      <p>{getRuleTypeDisplay(rule.ruleType)}</p>
                    </div>
                    {rule.thresholdAmount && (
                      <div>
                        <span className="font-medium text-gray-500">Amount Threshold:</span>
                        <p>{formatNaira(rule.thresholdAmount)}</p>
                      </div>
                    )}
                    {rule.thresholdCount && (
                      <div>
                        <span className="font-medium text-gray-500">Count Threshold:</span>
                        <p>{rule.thresholdCount}</p>
                      </div>
                    )}
                    <div>
                      <span className="font-medium text-gray-500">Risk Weight:</span>
                      <p>{rule.riskScoreWeight}x</p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-4 text-xs text-gray-500">
                    <span>Created: {new Date(rule.createdAt).toLocaleDateString()}</span>
                    {rule.updatedAt && (
                      <span>Updated: {new Date(rule.updatedAt).toLocaleDateString()}</span>
                    )}
                  </div>
                </div>

                <div className="flex items-center space-x-2 ml-4">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleToggleRuleStatus(rule)}
                  >
                    <SettingsIcon className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => openEditDialog(rule)}
                  >
                    <EditIcon className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => openDeleteDialog(rule)}
                    className="text-red-600 hover:text-red-700"
                  >
                    <TrashIcon className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>
        ))}

        {rules.length === 0 && !isRefreshing && (
          <Card>
            <CardContent className="text-center py-12">
              <ShieldIcon className="h-12 w-12 mx-auto text-gray-300 mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">No Fraud Rules Found</h3>
              <p className="text-gray-600 mb-4">
                {searchQuery || ruleTypeFilter || enabledFilter !== ''
                  ? 'No rules match your current filters. Try adjusting your search criteria.'
                  : 'Get started by creating your first fraud detection rule to protect your business.'}
              </p>
              <Button onClick={() => setIsCreateDialogOpen(true)}>
                <PlusIcon className="h-4 w-4 mr-2" />
                Create First Rule
              </Button>
            </CardContent>
          </Card>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-center space-x-2">
          <Button
            variant="outline"
            onClick={() => setCurrentPage(prev => Math.max(0, prev - 1))}
            disabled={currentPage === 0}
          >
            Previous
          </Button>
          <span className="flex items-center px-4 py-2 text-sm text-gray-600">
            Page {currentPage + 1} of {totalPages}
          </span>
          <Button
            variant="outline"
            onClick={() => setCurrentPage(prev => Math.min(totalPages - 1, prev + 1))}
            disabled={currentPage >= totalPages - 1}
          >
            Next
          </Button>
        </div>
      )}

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Edit Fraud Detection Rule</DialogTitle>
          </DialogHeader>
          {renderRuleForm()}
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsEditDialogOpen(false)}>
              Cancel
            </Button>
            <Button onClick={handleUpdateRule} disabled={isProcessing || !formData.ruleName}>
              {isProcessing ? <LoadingSpinner size="sm" /> : 'Update Rule'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="flex items-center space-x-2">
              <AlertTriangleIcon className="h-5 w-5 text-red-600" />
              <span>Delete Fraud Rule</span>
            </DialogTitle>
          </DialogHeader>
          <div className="py-4">
            <p className="text-gray-600">
              Are you sure you want to delete the rule <strong>"{selectedRule?.ruleName}"</strong>?
            </p>
            <div className="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
              <div className="flex items-start space-x-2">
                <InfoIcon className="h-5 w-5 text-yellow-600 mt-0.5" />
                <div className="text-sm text-yellow-800">
                  <p className="font-medium">Warning:</p>
                  <p>This action cannot be undone. The rule will be permanently removed and will no longer detect fraud patterns.</p>
                </div>
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setIsDeleteDialogOpen(false)}>
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={handleDeleteRule}
              disabled={isProcessing}
            >
              {isProcessing ? <LoadingSpinner size="sm" /> : 'Delete Rule'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}