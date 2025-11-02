import React from 'react'
import { useNavigate } from 'react-router-dom'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { LoadingSpinner } from '@/components/ui/loading-spinner'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { useCurrency } from '@/hooks/useCurrency'
import { ArrowUpDown, Eye } from 'lucide-react'
import { format } from 'date-fns'
import type { Investment } from '@/types/investment'

interface PaginationProps {
  page: number
  size: number
  totalPages: number
  totalElements: number
  onPageChange: (page: number) => void
}

interface InvestmentListProps {
  investments: Investment[]
  isLoading: boolean
  onSort: (field: string) => void
  sortBy: string
  sortDir: 'asc' | 'desc'
  pagination: PaginationProps
}

export const InvestmentList: React.FC<InvestmentListProps> = ({
  investments,
  isLoading,
  onSort,
  sortBy,
  sortDir,
  pagination
}) => {
  const navigate = useNavigate()
  const { formatCurrency } = useCurrency()

  const getStatusColor = (status: string) => {
    const colors: Record<string, string> = {
      ACTIVE: 'bg-green-100 text-green-800',
      MATURED: 'bg-blue-100 text-blue-800',
      WITHDRAWN: 'bg-gray-100 text-gray-800',
      DEFAULTED: 'bg-red-100 text-red-800',
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
  }

  const calculateROI = (investment: Investment) => {
    if (investment.amount === 0) return 0
    return ((investment.totalProfitEarned / investment.amount) * 100).toFixed(1)
  }

  const handleViewInvestment = (id: string) => {
    navigate(`/investments/${id}`)
  }

  const SortButton = ({ field, label }: { field: string; label: string }) => (
    <Button
      variant="ghost"
      size="sm"
      onClick={() => onSort(field)}
      className="h-8 font-medium"
    >
      {label}
      <ArrowUpDown className="ml-2 h-4 w-4" />
    </Button>
  )

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <LoadingSpinner size="lg" />
      </div>
    )
  }

  if (investments.length === 0) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">No investments found</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {/* Table */}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="w-[180px]">
                <SortButton field="investmentNumber" label="Investment #" />
              </TableHead>
              <TableHead>
                <SortButton field="shopName" label="Shop" />
              </TableHead>
              <TableHead className="text-right">
                <SortButton field="amount" label="Amount" />
              </TableHead>
              <TableHead className="text-center">
                <SortButton field="status" label="Status" />
              </TableHead>
              <TableHead className="text-right">
                <SortButton field="totalProfitEarned" label="Returns" />
              </TableHead>
              <TableHead className="text-right">ROI</TableHead>
              <TableHead className="text-right">Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {investments.map((investment) => (
              <TableRow key={investment.id}>
                <TableCell>
                  <div>
                    <p className="font-medium">{investment.investmentNumber}</p>
                    <p className="text-sm text-muted-foreground">
                      {format(new Date(investment.investmentDate), 'MMM dd, yyyy')}
                    </p>
                  </div>
                </TableCell>
                <TableCell>
                  <div>
                    <p className="font-medium">{investment.shopName}</p>
                    <p className="text-sm text-muted-foreground">
                      {investment.investmentType.replace(/_/g, ' ')}
                    </p>
                  </div>
                </TableCell>
                <TableCell className="text-right font-semibold">
                  {formatCurrency(investment.amount)}
                </TableCell>
                <TableCell className="text-center">
                  <Badge className={getStatusColor(investment.status)}>
                    {investment.status}
                  </Badge>
                </TableCell>
                <TableCell className="text-right font-medium text-green-600">
                  {formatCurrency(investment.totalProfitEarned)}
                </TableCell>
                <TableCell className="text-right">
                  <span className={`font-medium ${
                    Number(calculateROI(investment)) >= 0 ? 'text-green-600' : 'text-red-600'
                  }`}>
                    {calculateROI(investment)}%
                  </span>
                </TableCell>
                <TableCell className="text-right">
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => handleViewInvestment(investment.id)}
                  >
                    <Eye className="h-4 w-4 mr-1" />
                    View
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Pagination */}
      {pagination.totalPages > 1 && (
        <div className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">
            Showing {pagination.page * pagination.size + 1} to{' '}
            {Math.min((pagination.page + 1) * pagination.size, pagination.totalElements)} of{' '}
            {pagination.totalElements} investments
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => pagination.onPageChange(pagination.page - 1)}
              disabled={pagination.page === 0}
            >
              Previous
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => pagination.onPageChange(pagination.page + 1)}
              disabled={pagination.page >= pagination.totalPages - 1}
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}