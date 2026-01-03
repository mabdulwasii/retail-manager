import React from 'react';
import { Button } from '@/components/ui/button';
import { Calendar, Download } from 'lucide-react';
import { DateRangePeriod } from '@/services/cloudAnalyticsService';

interface AnalyticsDateFilterProps {
  selectedPeriod: DateRangePeriod;
  onPeriodChange: (period: DateRangePeriod) => void;
  onExport?: () => void;
  isExporting?: boolean;
}

/**
 * Analytics Date Filter Component
 * Date range selection and export controls
 */
export const AnalyticsDateFilter: React.FC<AnalyticsDateFilterProps> = ({
  selectedPeriod,
  onPeriodChange,
  onExport,
  isExporting = false,
}) => {
  const periods = [
    { value: DateRangePeriod.LAST_7_DAYS, label: 'Last 7 Days' },
    { value: DateRangePeriod.LAST_30_DAYS, label: 'Last 30 Days' },
    { value: DateRangePeriod.LAST_90_DAYS, label: 'Last 90 Days' },
  ];

  return (
    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
      {/* Date Range Buttons */}
      <div className="flex items-center gap-2">
        <Calendar className="h-4 w-4 text-muted-foreground" />
        <div className="flex gap-2">
          {periods.map((period) => (
            <Button
              key={period.value}
              variant={selectedPeriod === period.value ? 'default' : 'outline'}
              size="sm"
              onClick={() => onPeriodChange(period.value)}
            >
              {period.label}
            </Button>
          ))}
        </div>
      </div>

      {/* Export Button */}
      {onExport && (
        <Button
          variant="outline"
          size="sm"
          onClick={onExport}
          disabled={isExporting}
        >
          {isExporting ? (
            <>
              <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-current mr-2"></div>
              Exporting...
            </>
          ) : (
            <>
              <Download className="h-4 w-4 mr-2" />
              Export CSV
            </>
          )}
        </Button>
      )}
    </div>
  );
};

export default AnalyticsDateFilter;
