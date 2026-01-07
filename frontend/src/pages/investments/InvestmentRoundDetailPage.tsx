import React, { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { ArrowLeft, XCircle, CheckCircle, Calendar, DollarSign, Users, TrendingUp } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Separator } from '@/components/ui/separator'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Textarea } from '@/components/ui/textarea'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/context/UnifiedAuthContext'
import {
  useInvestmentRound,
  useCloseInvestmentRound,
  useCompleteInvestmentRound,
  useCancelInvestmentRound,
  useDeleteInvestmentRound,
} from '@/hooks/investment/useInvestmentRounds'
import { RoundStatus } from '@/types/investment'
import { useCurrency } from '@/hooks/useCurrency'
import { format } from 'date-fns'
import { LoadingSpinner } from '@/components/ui/loading-spinner'

export const InvestmentRoundDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { hasPermission } = useAuth()
  const { formatCurrency } = useCurrency()

  const [cancelDialogOpen, setCancelDialogOpen] = useState(false)
  const [cancelReason, setCancelReason] = useState('')

  const canManageRounds = hasPermission('INVESTMENT_UPDATE')

  const { data: round, isLoading } = useInvestmentRound(id!)
  const closeRound = useCloseInvestmentRound()
  const completeRound = useCompleteInvestmentRound()
  const cancelRound = useCancelInvestmentRound()
  const deleteRound = useDeleteInvestmentRound()

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (!round) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <p className="text-xl font-semibold mb-2">Investment round not found</p>
          <Button onClick={() => navigate('/investments/rounds')}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Rounds
          </Button>
        </div>
      </div>
    )
  }

  const getStatusBadge = (status: RoundStatus) => {
    const badges = {
      [RoundStatus.OPEN]: <Badge className="bg-green-500">Open</Badge>,
      [RoundStatus.CLOSED]: <Badge className="bg-blue-500">Closed</Badge>,
      [RoundStatus.COMPLETED]: <Badge className="bg-gray-500">Completed</Badge>,
      [RoundStatus.CANCELLED]: <Badge variant="destructive">Cancelled</Badge>,
    }
    return badges[status]
  }

  const getProfitModelLabel = (model: string) => {
    const labels: Record<string, string> = {
      PROPORTIONAL_BY_AMOUNT: 'Proportional by Amount',
      FIXED_SHARES: 'Fixed Shares',
      TIME_WEIGHTED: 'Time Weighted',
      TIERED: 'Tiered',
    }
    return labels[model] || model
  }

  const handleCloseRound = async () => {
    if (confirm('Are you sure you want to close this round? No new investors can be added after closing.')) {
      await closeRound.mutateAsync(round.id)
    }
  }

  const handleCompleteRound = async () => {
    if (confirm('Are you sure you want to mark this round as completed?')) {
      await completeRound.mutateAsync(round.id)
    }
  }

  const handleCancelRound = async () => {
    await cancelRound.mutateAsync({ roundId: round.id, reason: cancelReason })
    setCancelDialogOpen(false)
    setCancelReason('')
  }

  const handleDeleteRound = async () => {
    if (confirm('Are you sure you want to delete this investment round? This action cannot be undone and will delete all associated investments.')) {
      await deleteRound.mutateAsync(round.id)
      navigate('/investments/rounds')
    }
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="sm" onClick={() => navigate('/investments/rounds')}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back
          </Button>
          <div>
            <h1 className="text-3xl font-bold">{round.roundNumber}</h1>
            <p className="text-muted-foreground mt-1">{round.shopName}</p>
          </div>
          {getStatusBadge(round.status)}
        </div>
        <div className="flex gap-2">
          {canManageRounds && round.status === RoundStatus.OPEN && (
            <>
              <Button variant="outline" onClick={handleCloseRound}>
                <XCircle className="h-4 w-4 mr-2" />
                Close Round
              </Button>
              <Button variant="destructive" onClick={() => setCancelDialogOpen(true)}>
                Cancel Round
              </Button>
            </>
          )}
          {canManageRounds && round.status === RoundStatus.CLOSED && (
            <Button onClick={handleCompleteRound}>
              <CheckCircle className="h-4 w-4 mr-2" />
              Mark as Completed
            </Button>
          )}
          {canManageRounds && (
            <Button variant="destructive" onClick={handleDeleteRound}>
              Delete Round
            </Button>
          )}
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Amount</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{formatCurrency(round.totalAmount)}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Investors</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{round.totalInvestors}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Average Investment</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(round.totalAmount / round.totalInvestors)}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Maturity Date</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-lg font-bold">
              {round.maturityDate ? format(new Date(round.maturityDate), 'MMM dd, yyyy') : 'No maturity'}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Round Details */}
      <Card>
        <CardHeader>
          <CardTitle>Round Details</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label className="text-muted-foreground">Investment Type</Label>
              <p className="font-medium">{round.investmentType.replace('_', ' ')}</p>
            </div>
            <div>
              <Label className="text-muted-foreground">Profit Sharing Model</Label>
              <p className="font-medium">{getProfitModelLabel(round.profitSharingModel)}</p>
            </div>
            <div>
              <Label className="text-muted-foreground">Created Date</Label>
              <p className="font-medium">{format(new Date(round.createdAt), 'PPP')}</p>
            </div>
            {round.closedAt && (
              <div>
                <Label className="text-muted-foreground">Closed Date</Label>
                <p className="font-medium">{format(new Date(round.closedAt), 'PPP')}</p>
              </div>
            )}
            {round.completedAt && (
              <div>
                <Label className="text-muted-foreground">Completed Date</Label>
                <p className="font-medium">{format(new Date(round.completedAt), 'PPP')}</p>
              </div>
            )}
          </div>

          {round.notes && (
            <>
              <Separator />
              <div>
                <Label className="text-muted-foreground">Notes</Label>
                <p className="text-sm mt-1">{round.notes}</p>
              </div>
            </>
          )}

          {round.timeWeightingRules && (
            <>
              <Separator />
              <div>
                <Label className="text-muted-foreground">Time Weighting Configuration</Label>
                <div className="grid grid-cols-3 gap-2 mt-2 text-sm">
                  <div>
                    <p className="text-muted-foreground">Base (0-{round.timeWeightingRules.baseYears}y)</p>
                    <p className="font-semibold">{round.timeWeightingRules.baseMultiplier}x</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Tier 2 ({round.timeWeightingRules.year2Threshold}y+)</p>
                    <p className="font-semibold">{round.timeWeightingRules.year2Multiplier}x</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Tier 3 ({round.timeWeightingRules.year3Threshold}y+)</p>
                    <p className="font-semibold">{round.timeWeightingRules.year3Multiplier}x</p>
                  </div>
                </div>
              </div>
            </>
          )}

          {round.tierConfiguration && (
            <>
              <Separator />
              <div>
                <Label className="text-muted-foreground">Tier Configuration</Label>
                <div className="grid grid-cols-3 gap-2 mt-2 text-sm">
                  <div>
                    <p className="text-muted-foreground">Tier 1 ({formatCurrency(round.tierConfiguration.tier1Threshold)}+)</p>
                    <p className="font-semibold">{round.tierConfiguration.tier1Multiplier}x</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Tier 2 ({formatCurrency(round.tierConfiguration.tier2Threshold)}+)</p>
                    <p className="font-semibold">{round.tierConfiguration.tier2Multiplier}x</p>
                  </div>
                  <div>
                    <p className="text-muted-foreground">Tier 3 ({formatCurrency(round.tierConfiguration.tier3Threshold)}+)</p>
                    <p className="font-semibold">{round.tierConfiguration.tier3Multiplier}x</p>
                  </div>
                </div>
              </div>
            </>
          )}
        </CardContent>
      </Card>

      {/* Investors Table */}
      <Card>
        <CardHeader>
          <CardTitle>Investors</CardTitle>
          <CardDescription>{round.totalInvestors} investors in this round</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Investment #</TableHead>
                <TableHead>Investor</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Fixed Shares</TableHead>
                <TableHead>Total Profit</TableHead>
                <TableHead>Available Balance</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {round.investments.map((investment) => (
                <TableRow key={investment.id}>
                  <TableCell className="font-mono text-sm">{investment.investmentNumber}</TableCell>
                  <TableCell>
                    <div>
                      <p className="font-medium">{investment.investorName}</p>
                      <p className="text-xs text-muted-foreground">{investment.investorEmail}</p>
                    </div>
                  </TableCell>
                  <TableCell className="font-semibold">{formatCurrency(investment.amount)}</TableCell>
                  <TableCell>{investment.fixedShares || '—'}</TableCell>
                  <TableCell className="text-green-600 font-semibold">
                    {formatCurrency(investment.totalProfitEarned)}
                  </TableCell>
                  <TableCell className="font-semibold">
                    {formatCurrency(investment.availableBalance)}
                  </TableCell>
                  <TableCell>
                    <Badge
                      className={
                        investment.status === 'ACTIVE'
                          ? 'bg-green-500'
                          : investment.status === 'MATURED'
                          ? 'bg-blue-500'
                          : 'bg-gray-500'
                      }
                    >
                      {investment.status}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-right">
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => navigate(`/investments/${investment.id}`)}
                    >
                      View Details
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Cancel Round Dialog */}
      <Dialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cancel Investment Round</DialogTitle>
            <DialogDescription>
              This will cancel the round and all associated investments. This action cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="cancel-reason">Reason for Cancellation</Label>
              <Textarea
                id="cancel-reason"
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                placeholder="Enter reason for cancelling this round..."
                rows={3}
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCancelDialogOpen(false)}>
              Close
            </Button>
            <Button variant="destructive" onClick={handleCancelRound} disabled={!cancelReason}>
              Cancel Round
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
