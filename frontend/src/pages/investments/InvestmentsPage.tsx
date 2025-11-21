import { InvestmentList } from "@/components/investment/InvestmentList";
import { InvestmentSummaryCards } from "@/components/investment/InvestmentSummaryCards";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useInvestments } from "@/hooks/investment/useInvestments";
import { usePortfolioSummary } from "@/hooks/investment/usePortfolioSummary";
import { usePermissions } from "@/hooks/usePermissions";
import { downloadCSV, exportToPDF } from "@/lib/exportHelpers";
import { Download, FileDown, Plus, Users } from "lucide-react";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";

export const InvestmentsPage: React.FC = () => {
  const navigate = useNavigate();
  const permissions = usePermissions();

  // Check permissions based on backend permission matrix
  const canCreateInvestment = permissions.canCreateInvestment();
  // const canUpdateInvestment = permissions.canEditInvestment();
  // const canDeleteInvestment = permissions.canDeleteInvestment();
  // const canViewInvestments = permissions.canViewInvestments();

  const [page, setPage] = useState(0);
  const [size] = useState(20);
  const [sortBy, setSortBy] = useState("investmentDate");
  const [sortDir, setSortDir] = useState<"asc" | "desc">("desc");

  // Fetch investments
  const { data: investmentsData, isLoading } = useInvestments({
    page,
    size,
    sortBy,
    sortDir,
  });

  // Calculate portfolio summary
  const investments = investmentsData?.content || [];
  const portfolioSummary = usePortfolioSummary(investments);

  // Transform for InvestmentSummaryCards component
  const summaryForCards = {
    totalInvested: portfolioSummary.totalInvested,
    totalProfitEarned: portfolioSummary.totalReturns,
    totalWithdrawn: portfolioSummary.totalWithdrawn,
    availableBalance: portfolioSummary.availableBalance,
    activeInvestments: portfolioSummary.activeCount,
    totalInvestments: investments.length,
    pendingDistributions: 0,
    totalROI: portfolioSummary.averageROI,
  };

  const handleCreateInvestment = () => {
    navigate("/investments/create");
  };

  const handleExportReport = (format: "csv" | "pdf") => {
    if (!investments || investments.length === 0) {
      alert("No investment data to export");
      return;
    }

    const filename = `investments-report-${
      new Date().toISOString().split("T")[0]
    }`;

    if (format === "csv") {
      // Format investment data for CSV
      const formattedData = investments.map((inv) => ({
        Date: new Date(inv.investmentDate).toLocaleDateString(),
        Investor: inv.investorName || "N/A",
        Type: inv.investmentType?.replace("_", " ") || "N/A",
        Amount: inv.amount,
        // 'Profit Earned': inv.profitEarned || 0,
        // 'Withdrawn': inv.withdrawnAmount || 0,
        // 'Balance': (inv.amount + (inv.profitEarned || 0) - (inv.withdrawnAmount || 0)),
        // 'ROI %': inv.roi ? `${inv.roi.toFixed(2)}%` : '0%',
        Status: inv.status,
        //'Round': inv.roundNumber || 'N/A',
      }));
      downloadCSV(formattedData, `${filename}.csv`);
    } else {
      // Export to PDF
      exportToPDF("investments-content", "Investment Portfolio Report");
    }
  };

  const handleSort = (field: string) => {
    if (sortBy === field) {
      setSortDir(sortDir === "asc" ? "desc" : "asc");
    } else {
      setSortBy(field);
      setSortDir("desc");
    }
  };

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  return (
    <div className="space-y-6 p-6" id="investments-content">
      {/* Page Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Investment Portfolio</h1>
          <p className="text-muted-foreground mt-1">
            Track your investments, returns, and profit distributions
          </p>
        </div>
        <div className="flex gap-2">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline">
                <Download className="h-4 w-4 mr-2" />
                Export Report
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => handleExportReport("csv")}>
                <FileDown className="mr-2 h-4 w-4" />
                Export as CSV
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleExportReport("pdf")}>
                <FileDown className="mr-2 h-4 w-4" />
                Export as PDF
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          <Button
            variant="outline"
            onClick={() => navigate("/investments/rounds")}
          >
            <Users className="h-4 w-4 mr-2" />
            Investment Rounds
          </Button>
          {canCreateInvestment && (
            <Button onClick={handleCreateInvestment}>
              <Plus className="h-4 w-4 mr-2" />
              New Investment
            </Button>
          )}
        </div>
      </div>

      {/* Summary Cards */}
      <InvestmentSummaryCards summary={summaryForCards} isLoading={isLoading} />

      {/* Investments List */}
      <Card>
        <CardContent className="pt-6">
          <InvestmentList
            investments={investments}
            isLoading={isLoading}
            onSort={handleSort}
            sortBy={sortBy}
            sortDir={sortDir}
            pagination={{
              page,
              size,
              totalPages: investmentsData?.totalPages || 0,
              totalElements: investmentsData?.totalElements || 0,
              onPageChange: handlePageChange,
            }}
          />
        </CardContent>
      </Card>
    </div>
  );
};
