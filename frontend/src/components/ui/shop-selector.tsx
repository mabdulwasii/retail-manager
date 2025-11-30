import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useAuth } from "@/context/ManualAuthContext";
import { useActiveShops } from "@/hooks/useDashboard";
import { Permission } from "@/types/permissions";
import { Building2, Loader2 } from "lucide-react";
import React, { useEffect } from "react";

interface ShopSelectorProps {
  value?: string;
  onValueChange: (value: string) => void;
  className?: string;
  placeholder?: string;
  showAllOption?: boolean;
}

export const ShopSelector: React.FC<ShopSelectorProps> = ({
  value,
  onValueChange,
  className,
  placeholder = "Select shop",
  showAllOption = false,
}) => {
  const { hasAnyPermission } = useAuth();
  const { data: shops, isLoading } = useActiveShops();

  const canListShops = hasAnyPermission([
    Permission.SHOP_LIST,
    Permission.SHOP_LIST_ALL,
  ]);

  useEffect(() => {
    if (shops && shops.length === 1 && !value) {
      onValueChange(shops[0].id);
    }
  }, [shops, value, onValueChange]);

  const currentValue = value || "";

  if (!canListShops) {
    return <></>;
  }

  if (isLoading) {
    return (
      <div className="flex items-center space-x-2 px-3 py-2 border rounded-md bg-background">
        <Loader2 className="h-4 w-4 animate-spin text-muted-foreground" />
        <span className="text-sm text-muted-foreground">Loading shops...</span>
      </div>
    );
  }

  if (!shops || shops.length === 0) {
    return (
      <div className="flex items-center space-x-2 px-3 py-2 border rounded-md bg-muted/50">
        <Building2 className="h-4 w-4 text-muted-foreground" />
        <span className="text-sm text-muted-foreground">
          No shops available
        </span>
      </div>
    );
  }

  if (shops.length === 1) {
    return (
      <div className="flex items-center space-x-2 px-3 py-2 border rounded-md bg-muted/50">
        <Building2 className="h-4 w-4 text-muted-foreground" />
        <span className="text-sm font-medium text-foreground">
          {shops[0].name}
        </span>
      </div>
    );
  }

  // Show selector for multiple shops (user already has canListShops permission)
  return (
    <Select value={currentValue} onValueChange={onValueChange}>
      <SelectTrigger className={className}>
        <div className="flex items-center space-x-2">
          <Building2 className="h-4 w-4" />
          <SelectValue placeholder={placeholder} />
        </div>
      </SelectTrigger>
      <SelectContent>
        {showAllOption && (
          <SelectItem value="">
            <div className="flex items-center space-x-2">
              <span className="font-medium">All Shops</span>
            </div>
          </SelectItem>
        )}
        {shops.map((shop) => (
          <SelectItem key={shop.id} value={shop.id}>
            <div className="flex items-center justify-between w-full">
              <span>{shop.name}</span>
              {shop.status === "ACTIVE" && (
                <span className="ml-2 text-xs text-green-600 dark:text-green-400">
                  ●
                </span>
              )}
            </div>
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
};
