import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAuth } from "@/context/ManualAuthContext";
import { useCurrency } from "@/hooks/useCurrency";
import {
  TimePeriod,
  useAllShops,
  useFraudStatistics,
  useInventorySummary,
  useRevenueAnalytics,
  useSalesSummary,
} from "@/hooks/useDashboard";
import {
  Activity,
  AlertTriangle,
  Building,
  CheckCircle,
  DollarSign,
  Eye,
  Loader2,
  Server,
  Settings,
  Shield,
  ShoppingCart,
  Users,
} from "lucide-react";
import React, { useState } from "react";
import { Link } from "react-router-dom";
import { ShopSelector } from "@/components/ui/shop-selector";

export const AdminDashboard: React.FC = () => {
  const { user } = useAuth();
  const { formatCurrency } = useCurrency();
  const [period, setPeriod] = useState<TimePeriod>("month");
  const [selectedShopId, setSelectedShopId] = useState<string | undefined>(undefined); // Admins see all shops by default

  // Call only the APIs we actually need (not the heavy useDashboardData)
  const {
    data: shopsData,
    isLoading: shopsLoading,
    error: shopsError,
  } = useAllShops();
  const {
    data: salesSummary,
    isLoading: salesLoading,
    refetch: refetchSales,
  } = useSalesSummary(selectedShopId, period);
  const {
    data: revenueAnalytics,
    isLoading: revenueLoading,
    refetch: refetchRevenue,
  } = useRevenueAnalytics(selectedShopId, period);
  const {
    data: inventorySummary,
    isLoading: inventoryLoading,
    refetch: refetchInventory,
  } = useInventorySummary(selectedShopId);
  const {
    data: fraudStats,
    isLoading: fraudLoading,
    refetch: refetchFraud,
  } = useFraudStatistics(selectedShopId, period);

  // useAllShops already returns the array (not a paginated response)
  const shops = shopsData || [];
  const isLoading =
    shopsLoading ||
    salesLoading ||
    revenueLoading ||
    inventoryLoading ||
    fraudLoading;
  const hasError = shopsError;

  const refetch = () => {
    refetchSales();
    refetchRevenue();
    refetchInventory();
    refetchFraud();
  };

  // Calculate system metrics
  const activeShopsCount = shops.filter((s) => s.status === "ACTIVE").length;
  const totalShopsCount = shops.length;
  const systemHealth =
    activeShopsCount === totalShopsCount
      ? 100
      : (activeShopsCount / totalShopsCount) * 100;
  const hasAlerts =
    (inventorySummary?.lowStockItems || 0) > 0 ||
    (fraudStats?.highRiskCount || 0) > 0;

  // Calculate system stats from real data
  const systemStats = [
    {
      title: "Total Shops",
      value: shopsLoading ? "..." : totalShopsCount.toString(),
      description: "Across all tenants",
      icon: Building,
      trend: `${activeShopsCount} active`,
      color: "text-blue-600",
      status: "good",
    },
    {
      title: "Total Revenue",
      value: formatCurrency(salesSummary?.totalRevenue || 0),
      description: `This ${period}`,
      icon: DollarSign,
      trend: revenueAnalytics
        ? `${revenueAnalytics.growthRate > 0 ? "+" : ""}${
            revenueAnalytics.growthRate?.toFixed(1) || 0
          }%`
        : "0%",
      color: "text-green-600",
      status: (revenueAnalytics?.growthRate || 0) >= 0 ? "good" : "warning",
    },
    {
      title: "Total Transactions",
      value: salesSummary
        ? salesSummary.totalTransactions?.toString() || "0"
        : "0",
      description: `This ${period}`,
      icon: ShoppingCart,
      trend: salesSummary
        ? `Avg: ${formatCurrency(salesSummary.averageTransactionValue || 0)}`
        : `Avg: ${formatCurrency(0)}`,
      color: "text-purple-600",
      status: "good",
    },
    {
      title: "System Health",
      value: `${systemHealth.toFixed(1)}%`,
      description: "Shop availability",
      icon: Activity,
      trend: hasAlerts ? "Alerts active" : "All systems OK",
      color:
        systemHealth >= 95
          ? "text-emerald-600"
          : systemHealth >= 80
          ? "text-yellow-600"
          : "text-red-600",
      status:
        systemHealth >= 95 ? "good" : systemHealth >= 80 ? "warning" : "error",
    },
  ];

  // Generate activities from real system data
  const recentActivities = [
    ...(shops.length > 0
      ? [
          {
            type: "system",
            description: `${totalShopsCount} total shops in system (${activeShopsCount} active)`,
            time: "Current Status",
            severity:
              activeShopsCount === totalShopsCount ? "success" : "warning",
          },
        ]
      : []),
    ...(salesSummary
      ? [
          {
            type: "analytics",
            description: `${salesSummary.totalTransactions} transactions processed this ${period}`,
            time: `${period} Summary`,
            severity: "info",
          },
        ]
      : []),
    ...(revenueAnalytics
      ? [
          {
            type: "financial",
            description: `Revenue ${
              revenueAnalytics.growthRate >= 0 ? "increased" : "decreased"
            } by ${Math.abs(revenueAnalytics.growthRate).toFixed(1)}%`,
            time: `${period} Growth`,
            severity: revenueAnalytics.growthRate >= 0 ? "success" : "warning",
          },
        ]
      : []),
    ...((inventorySummary?.lowStockItems || 0) > 0
      ? [
          {
            type: "inventory",
            description: `${inventorySummary?.lowStockItems} items running low on stock`,
            time: "Inventory Alert",
            severity: "warning",
          },
        ]
      : []),
    ...((fraudStats?.highRiskCount || 0) > 0
      ? [
          {
            type: "security",
            description: `${fraudStats?.highRiskCount} high-risk transactions detected`,
            time: "Security Alert",
            severity: "error",
          },
        ]
      : [
          {
            type: "security",
            description: "No security threats detected",
            time: "Security Status",
            severity: "success",
          },
        ]),
  ].slice(0, 6);

  // System alerts from real data
  const systemAlerts = [
    ...((inventorySummary?.lowStockItems || 0) > 0
      ? [
          {
            type: "warning" as const,
            message: `${inventorySummary?.lowStockItems} products across shops are low on stock`,
            time: "Inventory Alert",
            action: "View Inventory",
            link: "/inventory?filter=lowStock",
          },
        ]
      : []),
    ...((inventorySummary?.expiredItems || 0) > 0
      ? [
          {
            type: "error" as const,
            message: `${inventorySummary?.expiredItems} expired items need immediate attention`,
            time: "Inventory Alert",
            action: "Remove Items",
            link: "/inventory?filter=expired",
          },
        ]
      : []),
    ...((fraudStats?.highRiskCount || 0) > 0
      ? [
          {
            type: "error" as const,
            message: `${fraudStats?.highRiskCount} high-risk transactions flagged for review`,
            time: "Security Alert",
            action: "Investigate",
            link: "/fraud-detection?risk=high",
          },
        ]
      : []),
    ...((fraudStats?.criticalRiskCount || 0) > 0
      ? [
          {
            type: "error" as const,
            message: `CRITICAL: ${fraudStats?.criticalRiskCount} critical risk transactions require immediate action`,
            time: "Security Alert",
            action: "Urgent Review",
            link: "/fraud-detection?risk=critical",
          },
        ]
      : []),
    ...(systemHealth < 95
      ? [
          {
            type: "warning" as const,
            message: `System health at ${systemHealth.toFixed(1)}% - ${
              totalShopsCount - activeShopsCount
            } shop(s) offline`,
            time: "System Status",
            action: "Check Shops",
            link: "/shops",
          },
        ]
      : [
          {
            type: "info" as const,
            message: "All systems operational - No critical alerts",
            time: "System Status",
            action: "View Dashboard",
            link: "/dashboard",
          },
        ]),
  ].slice(0, 5);

  // Handle errors
  if (hasError) {
    return (
      <div className="space-y-6">
        <Card>
          <CardContent className="flex items-center justify-center py-8">
            <div className="text-center">
              <AlertTriangle className="h-12 w-12 text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">
                Error Loading Dashboard
              </h3>
              <p className="text-muted-foreground mb-4">
                Unable to load dashboard data. Please check your connection.
              </p>
              <Button onClick={() => refetch()}>Try Again</Button>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Welcome Header */}
      <div className="flex justify-between items-start">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            System Administration
          </h1>
          <p className="text-muted-foreground">
            Welcome back, {user?.firstName || user?.username}. Here's your
            system overview.
          </p>
        </div>
        <div className="flex space-x-2">
          <ShopSelector 
            value={selectedShopId || ''}
            onValueChange={setSelectedShopId}
            className="w-[200px]"
            placeholder="All Shops"
            showAllOption={true}
          />
          <Select
            value={period}
            onValueChange={(value) => setPeriod(value as TimePeriod)}
          >
            <SelectTrigger className="w-32">
              <SelectValue placeholder="Period" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="today">Today</SelectItem>
              <SelectItem value="week">This Week</SelectItem>
              <SelectItem value="month">This Month</SelectItem>
              <SelectItem value="year">This Year</SelectItem>
            </SelectContent>
          </Select>
          <Button
            variant="outline"
            onClick={() => refetch()}
            disabled={isLoading}
          >
            {isLoading ? (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            ) : (
              <Activity className="mr-2 h-4 w-4" />
            )}
            Refresh
          </Button>
          <Button variant="outline" asChild>
            <Link to="/audit">
              <Eye className="mr-2 h-4 w-4" />
              Audit Logs
            </Link>
          </Button>
          <Button asChild>
            <Link to="/system-settings">
              <Settings className="mr-2 h-4 w-4" />
              System Settings
            </Link>
          </Button>
        </div>
      </div>

      {/* System Stats */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {systemStats.map((stat, index) => (
          <Card key={index} className="hover:shadow-md transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">
                {stat.title}
              </CardTitle>
              <stat.icon className={`h-4 w-4 ${stat.color}`} />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
              <p className="text-xs text-muted-foreground">
                {stat.description}
              </p>
              <div className="text-xs text-green-600 mt-1">{stat.trend}</div>
            </CardContent>
          </Card>
        ))}
      </div>

      {/* Quick Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Quick Actions</CardTitle>
          <CardDescription>Common administrative tasks</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/tenants">
                <Building className="h-6 w-6 mb-2" />
                Manage Tenants
              </Link>
            </Button>
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/users">
                <Users className="h-6 w-6 mb-2" />
                User Management
              </Link>
            </Button>
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/security">
                <Shield className="h-6 w-6 mb-2" />
                Security Center
              </Link>
            </Button>
            <Button variant="outline" className="h-20 flex-col" asChild>
              <Link to="/system-monitor">
                <Server className="h-6 w-6 mb-2" />
                System Monitor
              </Link>
            </Button>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        {/* Recent System Activities */}
        <Card className="col-span-2">
          <CardHeader>
            <CardTitle>Recent System Activities</CardTitle>
            <CardDescription>Latest system events and changes</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {recentActivities.map((activity, index) => (
                <div key={index} className="flex items-start space-x-4">
                  <div
                    className={`w-2 h-2 rounded-full mt-2 ${
                      activity.severity === "warning"
                        ? "bg-yellow-500"
                        : activity.severity === "error"
                        ? "bg-red-500"
                        : activity.severity === "success"
                        ? "bg-green-500"
                        : "bg-blue-500"
                    }`}
                  ></div>
                  <div className="flex-1 space-y-1">
                    <p className="text-sm font-medium leading-none">
                      {activity.description}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {activity.time}
                    </p>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        {/* System Alerts */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <AlertTriangle className="h-5 w-5" />
              <span>System Alerts</span>
            </CardTitle>
            <CardDescription>
              Important notifications requiring attention
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {systemAlerts.length > 0 ? (
                systemAlerts.map((alert, index) => (
                  <div
                    key={index}
                    className={`p-3 rounded-lg border ${
                      alert.type === "error"
                        ? "bg-red-50 border-red-200"
                        : alert.type === "warning"
                        ? "bg-yellow-50 border-yellow-200"
                        : "bg-blue-50 border-blue-200"
                    }`}
                  >
                    <div className="flex items-start space-x-3">
                      <div
                        className={`w-2 h-2 rounded-full mt-2 ${
                          alert.type === "warning"
                            ? "bg-yellow-500"
                            : alert.type === "error"
                            ? "bg-red-500"
                            : "bg-blue-500"
                        }`}
                      ></div>
                      <div className="flex-1">
                        <p className="text-sm font-medium">{alert.message}</p>
                        <p className="text-xs text-muted-foreground mt-1">
                          {alert.time}
                        </p>
                        <Button
                          size="sm"
                          variant="outline"
                          className="mt-2"
                          asChild
                        >
                          <Link to={alert.link}>{alert.action}</Link>
                        </Button>
                      </div>
                    </div>
                  </div>
                ))
              ) : (
                <div className="text-center py-8">
                  <CheckCircle className="h-12 w-12 mx-auto text-green-500 mb-2" />
                  <p className="text-sm font-medium">All Systems Operational</p>
                  <p className="text-xs text-muted-foreground mt-1">
                    No alerts at this time
                  </p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* System Performance Charts */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>System Performance</CardTitle>
            <CardDescription>Resource utilization over time</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[200px] flex items-center justify-center text-muted-foreground">
              Performance charts would be rendered here
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Tenant Growth</CardTitle>
            <CardDescription>New tenant registrations</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="h-[200px] flex items-center justify-center text-muted-foreground">
              Growth analytics charts would be rendered here
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
