import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import { useAuth } from '@/context/ManualAuthContext'
import { useCurrency } from '@/hooks/useCurrency'
import { useInvestment, Investment } from '@/hooks/useInvestment'

// Investment components
import { InvestmentSummaryCards } from '@/components/investment/InvestmentSummaryCards'
import { InvestmentForm } from '@/components/investment/InvestmentForm'
import { InvestmentList } from '@/components/investment/InvestmentList'
import { InvestmentDetails } from '@/components/investment/InvestmentDetails'
import { DistributionManagement } from '@/components/investment/DistributionManagement'
import { WithdrawalForm } from '@/components/investment/WithdrawalForm'

import {
  TrendingUpIcon,
  PlusIcon,
  DollarSignIcon,
  BarChart3Icon,
  AlertCircleIcon
} from 'lucide-react'

interface InvestmentDashboardProps {
  shopId?: string
  viewMode?: 'shop' | 'investor' | 'admin'
}

export const InvestmentDashboard: React.FC<InvestmentDashboardProps> = ({
  shopId,
  viewMode: propViewMode
}) => {
  const { user } = useAuth()
  const { formatCurrency } = useCurrency()
  const { getInvestmentSummary, isLoading } = useInvestment()

  // Determine view mode based on user role or prop
  const getViewMode = () => {
    if (propViewMode) return propViewMode
    if (!user) return 'investor'

    const roles = user.roles || []
    if (roles.includes('TENANT_ADMIN') || roles.includes('SHOP_OWNER')) return 'admin'
    if (roles.includes('MANAGER')) return 'shop'
    return 'investor'
  }

  const viewMode = getViewMode()
  const [activeTab, setActiveTab] = useState('overview')
  const [summary, setSummary] = useState<any>(null)
  const [selectedInvestment, setSelectedInvestment] = useState<Investment | null>(null)

  // Modal states
  const [isCreateFormOpen, setIsCreateFormOpen] = useState(false)
  const [isDetailsModalOpen, setIsDetailsModalOpen] = useState(false)
  const [isWithdrawalModalOpen, setIsWithdrawalModalOpen] = useState(false)

  useEffect(() => {
    if (viewMode === 'investor') {
      fetchSummary()
    }
  }, [viewMode])

  const fetchSummary = async () => {
    try {
      const result = await getInvestmentSummary()
      if (result) {
        setSummary(result)
      }
    } catch (error) {
      console.error('Failed to fetch investment summary:', error)
    }
  }

  const handleCreateInvestment = () => {
    setIsCreateFormOpen(true)
  }

  const handleViewInvestment = (investment: Investment) => {
    setSelectedInvestment(investment)
    setIsDetailsModalOpen(true)
  }

  const handleWithdraw = (investment: Investment) => {
    setSelectedInvestment(investment)
    setIsWithdrawalModalOpen(true)
  }

  const handleInvestmentCreated = () => {
    fetchSummary()
    setIsCreateFormOpen(false)
  }

  const handleWithdrawalProcessed = (updatedInvestment: Investment) => {
    fetchSummary()
    setIsWithdrawalModalOpen(false)
  }

  const getPageTitle = () => {
    switch (viewMode) {
      case 'admin': return 'Investment Management'
      case 'shop': return 'Shop Investments'
      case 'investor': return 'My Investment Portfolio'
      default: return 'Investment Dashboard'
    }
  }

  const getPageDescription = () => {
    switch (viewMode) {
      case 'admin': return 'Manage all investments and profit distributions across the platform'
      case 'shop': return 'Track and manage investments in your shop'
      case 'investor': return 'Monitor your investments and track returns'
      default: return 'Investment and profit sharing platform'
    }
  }

  const canCreateInvestment = () => {
    return viewMode === 'investor' || (viewMode === 'admin' && shopId)
  }

  if (isLoading && !summary) {
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
          <h1 className="text-3xl font-bold flex items-center space-x-2">
            <TrendingUpIcon className="h-8 w-8 text-blue-600" />
            <span>{getPageTitle()}</span>
          </h1>
          <p className="text-gray-600 mt-1">{getPageDescription()}</p>
        </div>
        {canCreateInvestment() && (
          <Button onClick={handleCreateInvestment}>
            <PlusIcon className="h-4 w-4 mr-2" />
            New Investment
          </Button>
        )}
      </div>

      {/* Investment Summary (for investors) */}
      {viewMode === 'investor' && (
        <InvestmentSummaryCards
          summary={summary}
          isLoading={isLoading}
        />
      )}

      {/* Main Content Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="overview" className="flex items-center space-x-2">
            <BarChart3Icon className="h-4 w-4" />
            <span>Overview</span>
          </TabsTrigger>
          <TabsTrigger value="investments" className="flex items-center space-x-2">
            <DollarSignIcon className="h-4 w-4" />
            <span>Investments</span>
          </TabsTrigger>
          <TabsTrigger value="distributions" className="flex items-center space-x-2">
            <TrendingUpIcon className="h-4 w-4" />
            <span>Distributions</span>
          </TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-6">
          {viewMode === 'investor' && summary ? (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {/* Recent Investments */}
              <Card>
                <CardHeader>
                  <CardTitle>Recent Investments</CardTitle>
                </CardHeader>
                <CardContent>
                  {summary.investments && summary.investments.length > 0 ? (
                    <div className="space-y-3">
                      {summary.investments.slice(0, 3).map((investment: Investment) => (
                        <div key={investment.id} className="flex items-center justify-between p-3 border rounded-lg">
                          <div>
                            <p className="font-medium">{investment.investmentNumber}</p>
                            <p className="text-sm text-gray-600">{investment.shopName}</p>
                          </div>
                          <div className="text-right">
                            <p className="font-semibold">{formatCurrency(investment.amount)}</p>
                            <p className="text-sm text-green-600">
                              +{formatCurrency(investment.totalProfitEarned)}
                            </p>
                          </div>
                        </div>
                      ))}
                      {summary.investments.length > 3 && (
                        <Button
                          variant="ghost"
                          className="w-full"
                          onClick={() => setActiveTab('investments')}
                        >
                          View All Investments ({summary.investments.length})
                        </Button>
                      )}
                    </div>
                  ) : (
                    <div className="text-center py-8 text-gray-500">
                      <DollarSignIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                      <p>No investments yet</p>
                      <Button
                        variant="outline"
                        className="mt-3"
                        onClick={handleCreateInvestment}
                      >
                        Create First Investment
                      </Button>
                    </div>
                  )}
                </CardContent>
              </Card>

              {/* Recent Distributions */}
              <Card>
                <CardHeader>
                  <CardTitle>Recent Distributions</CardTitle>
                </CardHeader>
                <CardContent>
                  {summary.distributions && summary.distributions.length > 0 ? (
                    <div className="space-y-3">
                      {summary.distributions.slice(0, 3).map((distribution: any) => (
                        <div key={distribution.id} className="flex items-center justify-between p-3 border rounded-lg">
                          <div>
                            <p className="font-medium">{distribution.investmentNumber}</p>
                            <p className="text-sm text-gray-600">
                              {new Date(distribution.periodStart).toLocaleDateString()} - {new Date(distribution.periodEnd).toLocaleDateString()}
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="font-semibold text-green-600">
                              {formatCurrency(distribution.distributionAmount)}
                            </p>
                            <p className="text-sm text-gray-600">{distribution.status}</p>
                          </div>
                        </div>
                      ))}
                      {summary.distributions.length > 3 && (
                        <Button
                          variant="ghost"
                          className="w-full"
                          onClick={() => setActiveTab('distributions')}
                        >
                          View All Distributions ({summary.distributions.length})
                        </Button>
                      )}
                    </div>
                  ) : (
                    <div className="text-center py-8 text-gray-500">
                      <TrendingUpIcon className="h-12 w-12 mx-auto mb-2 text-gray-300" />
                      <p>No distributions yet</p>
                      <p className="text-sm">Profit distributions will appear here</p>
                    </div>
                  )}
                </CardContent>
              </Card>
            </div>
          ) : (
            <Card>
              <CardContent className="text-center py-12">
                <BarChart3Icon className="h-12 w-12 mx-auto text-gray-300 mb-4" />
                <h3 className="text-lg font-medium text-gray-900 mb-2">Overview Dashboard</h3>
                <p className="text-gray-600">
                  {viewMode === 'admin'
                    ? 'View overall investment statistics and platform metrics here.'
                    : 'Select a specific shop to view investment overview.'
                  }
                </p>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="investments">
          <InvestmentList
            shopId={shopId}
            onCreateInvestment={handleCreateInvestment}
            onViewInvestment={handleViewInvestment}
            viewMode={viewMode === 'admin' ? 'shop' : viewMode}
          />
        </TabsContent>

        <TabsContent value="distributions">
          <DistributionManagement
            viewMode={viewMode === 'investor' ? 'investor' : 'admin'}
            shopId={shopId}
          />
        </TabsContent>
      </Tabs>

      {/* Modals */}
      <InvestmentForm
        isOpen={isCreateFormOpen}
        onClose={() => setIsCreateFormOpen(false)}
        shopId={shopId || ''}
        onInvestmentCreated={handleInvestmentCreated}
      />

      <InvestmentDetails
        investment={selectedInvestment}
        isOpen={isDetailsModalOpen}
        onClose={() => setIsDetailsModalOpen(false)}
        onWithdraw={handleWithdraw}
      />

      <WithdrawalForm
        investment={selectedInvestment}
        isOpen={isWithdrawalModalOpen}
        onClose={() => setIsWithdrawalModalOpen(false)}
        onWithdrawalProcessed={handleWithdrawalProcessed}
      />
    </div>
  )
}