import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, Eye, MoreVertical, XCircle, Trash2 } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { useAuth } from '@/context/ManualAuthContext'
import { useInvestmentRounds, useCloseInvestmentRound, useCompleteInvestmentRound, useDeleteInvestmentRound } from '@/hooks/investment/useInvestmentRounds'
import { RoundStatus } from '@/types/investment'
import { useCurrency } from '@/hooks/useCurrency'
import { format } from 'date-fns'
import { LoadingSpinner } from '@/components/ui/loading-spinner'

export const InvestmentRoundsPage: React.FC = () => {
  const navigate = useNavigate()
  const { user, hasPermission } = useAuth()
  const { formatCurrency } = useCurrency()
  const shopId = user?.shopId || ''

  const [page, setPage] = useState(0)
  const [size] = useState(20)
  const [statusFilter, setStatusFilter] = useState<string>('all')

  const canCreateRound = hasPermission('INVESTMENT_CREATE')
  const canManageRounds = hasPermission('INVESTMENT_UPDATE')

  const { data: roundsData, isLoading } = useInvestmentRounds({
    shopId,
    page,
    size,
    status: statusFilter === 'all' ? undefined : statusFilter,
  })

  const closeRound = useCloseInvestmentRound()
  const completeRound = useCompleteInvestmentRound()
  const deleteRound = useDeleteInvestmentRound()

  const rounds = roundsData?.content || []

  const getStatusBadge = (status: RoundStatus) => {
    const badges = {
      [RoundStatus.OPEN]: <Badge className="bg-green-500">Open</Badge>,
      [RoundStatus.CLOSED]: <Badge className="bg-blue-500">Closed</Badge>,
      [RoundStatus.COMPLETED]: <Badge className="bg-gray-500">Completed</Badge>,
      [RoundStatus.CANCELLED]: <Badge variant="destructive">Cancelled</Badge>,
    }
    return badges[status] || <Badge variant="secondary">{status}</Badge>
  }

  const getProfitModelLabel = (model: string) => {
    const labels: Record<string, string> = {
      PROPORTIONAL_BY_AMOUNT: 'Proportional',
      FIXED_SHARES: 'Fixed Shares',
      TIME_WEIGHTED: 'Time Weighted',
      TIERED: 'Tiered',
    }
    return labels[model] || model
  }

  const handleCloseRound = async (roundId: string) => {
    if (confirm('Are you sure you want to close this round? No new investors can be added after closing.')) {
      await closeRound.mutateAsync(roundId)
    }
  }

  const handleCompleteRound = async (roundId: string) => {
    if (confirm('Are you sure you want to mark this round as completed? This action cannot be undone.')) {
      await completeRound.mutateAsync(roundId)
    }
  }

  const handleDeleteRound = async (roundId: string) => {
    if (confirm('Are you sure you want to delete this investment round? This will permanently delete all associated investments.')) {
      await deleteRound.mutateAsync(roundId)
    }
  }

  return (
    <div className="space-y-6 p-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Investment Rounds</h1>
          <p className="text-muted-foreground mt-1">
            Manage investment rounds and track investor groups
          </p>
        </div>
        {canCreateRound && (
          <Button onClick={() => navigate('/investments/rounds/create')}>
            <Plus className="h-4 w-4 mr-2" />
            New Round
          </Button>
        )}
      </div>

      {/* Summary Cards */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Rounds</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{roundsData?.totalElements || 0}</div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Open Rounds</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-green-600">
              {rounds.filter(r => r.status === RoundStatus.OPEN).length}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Investment</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(rounds.reduce((sum, r) => sum + r.totalAmount, 0))}
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Investors</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {rounds.reduce((sum, r) => sum + r.totalInvestors, 0)}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <div className="flex gap-4">
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-[180px]">
            <SelectValue placeholder="Filter by status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">All Statuses</SelectItem>
            <SelectItem value={RoundStatus.OPEN}>Open</SelectItem>
            <SelectItem value={RoundStatus.CLOSED}>Closed</SelectItem>
            <SelectItem value={RoundStatus.COMPLETED}>Completed</SelectItem>
            <SelectItem value={RoundStatus.CANCELLED}>Cancelled</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Rounds Table */}
      <Card>
        <CardContent className="pt-6">
          {isLoading ? (
            <div className="flex justify-center items-center py-12">
              <LoadingSpinner size="lg" />
            </div>
          ) : rounds.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-muted-foreground mb-4">No investment rounds found</p>
              {canCreateRound && (
                <Button onClick={() => navigate('/investments/rounds/create')}>
                  <Plus className="h-4 w-4 mr-2" />
                  Create First Round
                </Button>
              )}
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Round Number</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Profit Model</TableHead>
                  <TableHead>Investors</TableHead>
                  <TableHead>Total Amount</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead>Maturity</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {rounds.map((round) => (
                  <TableRow key={round.id}>
                    <TableCell className="font-medium">{round.roundNumber}</TableCell>
                    <TableCell>{getStatusBadge(round.status)}</TableCell>
                    <TableCell>{round.investmentType.replace('_', ' ')}</TableCell>
                    <TableCell>{getProfitModelLabel(round.profitSharingModel)}</TableCell>
                    <TableCell>{round.totalInvestors}</TableCell>
                    <TableCell className="font-semibold">
                      {formatCurrency(round.totalAmount)}
                    </TableCell>
                    <TableCell>{format(new Date(round.createdAt), 'MMM dd, yyyy')}</TableCell>
                    <TableCell>
                      {round.maturityDate
                        ? format(new Date(round.maturityDate), 'MMM dd, yyyy')
                        : '—'}
                    </TableCell>
                    <TableCell className="text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="sm">
                            <MoreVertical className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuLabel>Actions</DropdownMenuLabel>
                          <DropdownMenuSeparator />
                          <DropdownMenuItem
                            onClick={() => navigate(`/investments/rounds/${round.id}`)}
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            View Details
                          </DropdownMenuItem>
                          {canManageRounds && round.status === RoundStatus.OPEN && (
                            <DropdownMenuItem onClick={() => handleCloseRound(round.id)}>
                              <XCircle className="mr-2 h-4 w-4" />
                              Close Round
                            </DropdownMenuItem>
                          )}
                          {canManageRounds && round.status === RoundStatus.CLOSED && (
                            <DropdownMenuItem onClick={() => handleCompleteRound(round.id)}>
                              Complete Round
                            </DropdownMenuItem>
                          )}
                          {canManageRounds && (
                            <>
                              <DropdownMenuSeparator />
                              <DropdownMenuItem
                                onClick={() => handleDeleteRound(round.id)}
                                className="text-destructive focus:text-destructive"
                              >
                                <Trash2 className="mr-2 h-4 w-4" />
                                Delete Round
                              </DropdownMenuItem>
                            </>
                          )}
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Pagination */}
      {roundsData && roundsData.totalPages > 1 && (
        <div className="flex justify-center gap-2">
          <Button
            variant="outline"
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
          >
            Previous
          </Button>
          <div className="flex items-center px-4">
            Page {page + 1} of {roundsData.totalPages}
          </div>
          <Button
            variant="outline"
            onClick={() => setPage(p => p + 1)}
            disabled={page >= roundsData.totalPages - 1}
          >
            Next
          </Button>
        </div>
      )}
    </div>
  )
}
