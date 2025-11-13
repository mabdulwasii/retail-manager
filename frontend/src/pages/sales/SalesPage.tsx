import { SalesHistory } from "@/components/sales/SalesHistory";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { downloadCSV, exportToPDF } from '@/lib/exportHelpers';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAuth } from "@/context/ManualAuthContext";
import { useCurrency } from "@/hooks/useCurrency";
import { PagedSalesResponse, SalesFilter, useSales } from "@/hooks/useSales";
import {
  AlertCircle,
  Calendar,
  DollarSign,
  Download,
  Filter,
  RefreshCw,
  Search,
  ShoppingBag,
  TrendingUp,
  FileDown,
} from "lucide-react";
import React, { useEffect, useState } from "react";
import { usePermissions } from "@/hooks/usePermissions";

export const SalesPage: React.FC = () => {
  const { user } = useAuth();
  const { formatCurrency } = useCurrency();
  const { sales, fetchSales, isLoading, error } = useSales();
  const permissions = usePermissions();

  // Check permissions based on backend permission matrix
  const canViewSales = permissions.canViewSales();
  const canUpdateSales = permissions.canEditSale();
  const canDeleteSales = permissions.canDeleteSale();
  const canCreateSales = permissions.canCreateSale();

  const [searchQuery, setSearchQuery] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [paymentMethodFilter, setPaymentMethodFilter] = useState<string>("all");
  const [dateRange, setDateRange] = useState({
    startDate: "",
    endDate: "",
  });
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalPages: 0,
    totalElements: 0,
  });

  // Fetch sales on mount only once
  useEffect(() => {
    if (user?.shopId) {
      loadSales();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.shopId]);

  const loadSales = async (page: number = 0) => {
    const filter: SalesFilter = {
      page,
      size: 20,
      sort: 'transactionDate,desc',
    };

    if (user?.shopId) filter.shopId = user.shopId;
    if (statusFilter !== "all") filter.status = statusFilter;
    if (paymentMethodFilter !== "all")
      filter.paymentMethod = paymentMethodFilter;
    if (dateRange.startDate) filter.startDate = dateRange.startDate;
    if (dateRange.endDate) filter.endDate = dateRange.endDate;

    const response = await fetchSales(filter);
    if (response) {
      setPagination({
        page: response.number,
        size: response.size,
        totalPages: response.totalPages,
        totalElements: response.totalElements,
      });
    }
  };

  const handleSearch = () => {
    loadSales();
  };

  const handleReset = () => {
    setSearchQuery("");
    setStatusFilter("all");
    setPaymentMethodFilter("all");
    setDateRange({ startDate: "", endDate: "" });
    setPagination({ page: 0, size: 20, totalPages: 0, totalElements: 0 });
    loadSales(0);
  };

  const handlePageChange = (newPage: number) => {
    loadSales(newPage);
  };

  const handleExport = (format: 'csv' | 'pdf') => {
    if (!sales || sales.length === 0) {
      alert('No sales data to export');
      return;
    }

    const filename = `sales-report-${new Date().toISOString().split('T')[0]}`;

    if (format === 'csv') {
      // Format sales data for CSV
      const formattedData = sales.map(sale => ({
        'Receipt #': sale.receiptNumber,
        'Date': new Date(sale.transactionDate).toLocaleDateString(),
        'Customer': sale.customerName || 'Walk-in',
        'Items': sale.items?.length || 0,
        'Subtotal': sale.subtotal,
        'Tax': sale.taxAmount,
        'Discount': sale.discountAmount,
        'Total': sale.totalAmount,
        'Payment Method': sale.paymentMethod,
        'Status': sale.status,
      }));
      downloadCSV(formattedData, `${filename}.csv`);
    } else {
      // Export to PDF
      exportToPDF('sales-content', 'Sales Report');
    }
  };

  // Calculate summary statistics
  const totalRevenue = sales.reduce((sum, sale) => sum + sale.totalAmount, 0);
  const totalTransactions = sales.length;
  const completedTransactions = sales.filter(
    (s) => s.status === "COMPLETED"
  ).length;
  const averageTransaction =
    totalTransactions > 0 ? totalRevenue / totalTransactions : 0;

  const renderSalesContent = () => {
    if (isLoading) {
      return (
        <div className="flex justify-center py-12">
          <div className="text-muted-foreground">Loading sales...</div>
        </div>
      );
    }

    if (sales.length === 0) {
      return (
        <div className="flex flex-col items-center justify-center py-12">
          <ShoppingBag className="w-16 h-16 text-muted-foreground/20 mb-4" />
          <p className="text-muted-foreground font-medium">
            No sales found
          </p>
          <p className="text-sm text-muted-foreground mt-1">
            Sales transactions will appear here
          </p>
        </div>
      );
    }

    return (
      <>
        <SalesHistory transactions={sales} />
        
        {pagination.totalPages > 1 && (
          <div className="flex items-center justify-between mt-6 pt-4 border-t">
            <div className="text-sm text-muted-foreground">
              Page {pagination.page + 1} of {pagination.totalPages}
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => handlePageChange(pagination.page - 1)}
                disabled={pagination.page === 0}
              >
                Previous
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handlePageChange(pagination.page + 1)}
                disabled={pagination.page >= pagination.totalPages - 1}
              >
                Next
              </Button>
            </div>
          </div>
        )}
      </>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Sales History</h1>
          <p className="text-muted-foreground mt-1">
            View and manage all sales transactions
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={handleReset}>
            <RefreshCw className="w-4 h-4 mr-2" />
            Reset
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline">
                <Download className="w-4 h-4 mr-2" />
                Export
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => handleExport('csv')}>
                <FileDown className="mr-2 h-4 w-4" />
                Export as CSV
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleExport('pdf')}>
                <FileDown className="mr-2 h-4 w-4" />
                Export as PDF
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {/* Exportable Content */}
      <div id="sales-content" className="space-y-6">
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Revenue</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(totalRevenue)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">All time sales</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Transactions</CardTitle>
            <ShoppingBag className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{totalTransactions}</div>
            <p className="text-xs text-muted-foreground mt-1">
              {completedTransactions} completed
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">
              Avg. Transaction
            </CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(averageTransaction)}
            </div>
            <p className="text-xs text-muted-foreground mt-1">Per sale</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Today</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {
                sales.filter((s) => {
                  const saleDate = new Date(s.transactionDate).toDateString();
                  const today = new Date().toDateString();
                  return saleDate === today;
                }).length
              }
            </div>
            <p className="text-xs text-muted-foreground mt-1">Sales today</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Filter className="w-5 h-5" />
            Filters
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            {/* Search */}
            <div className="space-y-2">
              <label className="text-sm font-medium">Search</label>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  placeholder="Receipt number..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-9"
                />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Status</label>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger>
                  <SelectValue placeholder="All statuses" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Statuses</SelectItem>
                  <SelectItem value="COMPLETED">Completed</SelectItem>
                  <SelectItem value="PENDING">Pending</SelectItem>
                  <SelectItem value="REFUNDED">Refunded</SelectItem>
                  <SelectItem value="CANCELLED">Cancelled</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Payment Method</label>
              <Select
                value={paymentMethodFilter}
                onValueChange={setPaymentMethodFilter}
              >
                <SelectTrigger>
                  <SelectValue placeholder="All methods" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Methods</SelectItem>
                  <SelectItem value="CASH">Cash</SelectItem>
                  <SelectItem value="CARD">Card</SelectItem>
                  <SelectItem value="MOBILE">Mobile</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Date Range - Spans 2 columns on larger screens */}
            <div className="space-y-2 sm:col-span-2 lg:col-span-1">
              <label className="text-sm font-medium">Date Range</label>
              <div className="grid grid-cols-2 gap-2">
                <Input
                  type="date"
                  value={dateRange.startDate}
                  onChange={(e) =>
                    setDateRange({ ...dateRange, startDate: e.target.value })
                  }
                  placeholder="Start"
                />
                <Input
                  type="date"
                  value={dateRange.endDate}
                  onChange={(e) =>
                    setDateRange({ ...dateRange, endDate: e.target.value })
                  }
                  placeholder="End"
                />
              </div>
            </div>
          </div>

          <div className="mt-4 flex justify-end">
            <Button onClick={handleSearch}>
              <Search className="w-4 h-4 mr-2" />
              Apply Filters
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Recent Transactions</CardTitle>
          <CardDescription>
            Showing {sales.length} of {pagination.totalElements} transaction{pagination.totalElements !== 1 ? "s" : ""}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {renderSalesContent()}
        </CardContent>
      </Card>
      </div>
    </div>
  );
};
