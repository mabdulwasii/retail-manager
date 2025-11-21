import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ChevronRight, Home } from 'lucide-react';
import { cn } from '@/lib/utils';

// Map of path segments to display names
const pathMap: Record<string, string> = {
  '': 'Home',
  'dashboard': 'Dashboard',
  'shops': 'Shops',
  'products': 'Products',
  'inventory': 'Inventory',
  'sales': 'Sales',
  'receipts': 'Receipts',
  'investments': 'Investments',
  'analytics': 'Analytics',
  'audit': 'Audit Logs',
  'profile': 'Profile',
};

interface BreadcrumbProps {
  className?: string;
}

export const Breadcrumb: React.FC<BreadcrumbProps> = ({ className }) => {
  const location = useLocation();
  const pathSegments = location.pathname.split('/').filter(Boolean);
  
  return (
    <nav 
      className={cn("flex items-center space-x-1 text-sm text-muted-foreground", className)}
      aria-label="Breadcrumb"
    >
      <ol className="flex items-center space-x-1">
        <li>
          <Link 
            to="/dashboard" 
            className="flex items-center hover:text-primary transition-colors"
          >
            <Home className="h-4 w-4" />
            <span className="sr-only">Dashboard</span>
          </Link>
        </li>
        
        {pathSegments.map((segment, index) => {
          const path = `/${pathSegments.slice(0, index + 1).join('/')}`;
          const displayName = pathMap[segment] || segment;
          
          return (
            <React.Fragment key={path}>
              <li className="flex items-center">
                <ChevronRight className="h-4 w-4" />
              </li>
              <li>
                {index === pathSegments.length - 1 ? (
                  <span className="font-medium text-foreground">{displayName}</span>
                ) : (
                  <Link 
                    to={path}
                    className="hover:text-primary transition-colors"
                  >
                    {displayName}
                  </Link>
                )}
              </li>
            </React.Fragment>
          );
        })}
      </ol>
    </nav>
  );
};
