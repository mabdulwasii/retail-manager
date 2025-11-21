# Component Library

**Version**: 1.0
**Last Updated**: January 2025
**Framework**: React 18 + TypeScript + shadcn/ui

---

## Base Components (shadcn/ui)

All base components are located in `src/components/ui/`.

### Form Components

#### Button
```typescript
import { Button } from '@/components/ui/button';

<Button variant="default" size="md" onClick={handleClick}>
  Click Me
</Button>

// Variants: default, secondary, destructive, outline, ghost, link
// Sizes: sm, md, lg
```

#### Input
```typescript
import { Input } from '@/components/ui/input';

<Input
  type="text"
  placeholder="Enter text..."
  value={value}
  onChange={(e) => setValue(e.target.value)}
/>
```

#### Select
```typescript
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

<Select value={value} onValueChange={setValue}>
  <SelectTrigger>
    <SelectValue placeholder="Select option" />
  </SelectTrigger>
  <SelectContent>
    <SelectItem value="option1">Option 1</SelectItem>
    <SelectItem value="option2">Option 2</SelectItem>
  </SelectContent>
</Select>
```

### Data Display Components

#### Card
```typescript
import { Card, CardHeader, CardTitle, CardDescription, CardContent, CardFooter } from '@/components/ui/card';

<Card>
  <CardHeader>
    <CardTitle>Card Title</CardTitle>
    <CardDescription>Card description</CardDescription>
  </CardHeader>
  <CardContent>
    Content goes here
  </CardContent>
  <CardFooter>
    Footer content
  </CardFooter>
</Card>
```

#### Badge
```typescript
import { Badge } from '@/components/ui/badge';

<Badge variant="default">Active</Badge>
<Badge variant="secondary">Pending</Badge>
<Badge variant="destructive">Inactive</Badge>
<Badge variant="outline">Draft</Badge>
```

#### Avatar
```typescript
import { Avatar, AvatarImage, AvatarFallback } from '@/components/ui/avatar';

<Avatar>
  <AvatarImage src="/avatar.jpg" alt="User" />
  <AvatarFallback>JD</AvatarFallback>
</Avatar>
```

### Feedback Components

#### Alert
```typescript
import { Alert, AlertTitle, AlertDescription } from '@/components/ui/alert';

<Alert variant="default">
  <AlertTitle>Note</AlertTitle>
  <AlertDescription>
    This is an informational alert.
  </AlertDescription>
</Alert>

// Variants: default, destructive
```

#### Dialog (Modal)
```typescript
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';

<Dialog open={open} onOpenChange={setOpen}>
  <DialogTrigger asChild>
    <Button>Open Dialog</Button>
  </DialogTrigger>
  <DialogContent>
    <DialogHeader>
      <DialogTitle>Dialog Title</DialogTitle>
      <DialogDescription>
        Dialog description
      </DialogDescription>
    </DialogHeader>
    {/* Dialog content */}
  </DialogContent>
</Dialog>
```

### Navigation Components

#### Tabs
```typescript
import { Tabs, TabsList, TabsTrigger, TabsContent } from '@/components/ui/tabs';

<Tabs value={activeTab} onValueChange={setActiveTab}>
  <TabsList>
    <TabsTrigger value="tab1">Tab 1</TabsTrigger>
    <TabsTrigger value="tab2">Tab 2</TabsTrigger>
  </TabsList>
  <TabsContent value="tab1">
    Content for tab 1
  </TabsContent>
  <TabsContent value="tab2">
    Content for tab 2
  </TabsContent>
</Tabs>
```

#### Dropdown Menu
```typescript
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

<DropdownMenu>
  <DropdownMenuTrigger asChild>
    <Button variant="outline">Open Menu</Button>
  </DropdownMenuTrigger>
  <DropdownMenuContent>
    <DropdownMenuItem onClick={handleAction1}>
      Action 1
    </DropdownMenuItem>
    <DropdownMenuItem onClick={handleAction2}>
      Action 2
    </DropdownMenuItem>
  </DropdownMenuContent>
</DropdownMenu>
```

---

## Domain-Specific Components

### Dashboard Components

Located in `src/components/dashboard/`.

#### RoleBasedDashboard
Renders appropriate dashboard based on user role.

```typescript
import { RoleBasedDashboard } from '@/components/dashboard/RoleBasedDashboard';

<RoleBasedDashboard />
```

### Inventory Components

Located in `src/components/inventory/`.

#### InventoryList
```typescript
import { InventoryList } from '@/components/inventory/InventoryList';

<InventoryList
  items={inventoryItems}
  onEdit={handleEdit}
  onDelete={handleDelete}
/>
```

#### StockAdjustmentModal
```typescript
import { StockAdjustmentModal } from '@/components/inventory/StockAdjustmentModal';

<StockAdjustmentModal
  inventoryId={inventoryId}
  currentStock={currentStock}
  onSubmit={handleAdjust}
  onClose={() => setOpen(false)}
/>
```

### Investment Components

Located in `src/components/investment/`.

#### InvestmentList
```typescript
import { InvestmentList } from '@/components/investment/InvestmentList';

<InvestmentList
  investments={investments}
  onViewDetails={handleView}
/>
```

#### DistributionManagement
```typescript
import { DistributionManagement } from '@/components/investment/DistributionManagement';

<DistributionManagement
  investmentId={investmentId}
  distributions={distributions}
  onApprove={handleApprove}
  onMarkPaid={handleMarkPaid}
/>
```

### Sales Components

Located in `src/components/sales/`.

#### ProductSearch
```typescript
import { ProductSearch } from '@/components/sales/ProductSearch';

<ProductSearch
  onSelectProduct={handleSelectProduct}
/>
```

#### ShoppingCart
```typescript
import { ShoppingCart } from '@/components/sales/ShoppingCart';

<ShoppingCart
  items={cartItems}
  onUpdateQuantity={handleUpdateQuantity}
  onRemoveItem={handleRemoveItem}
  onCheckout={handleCheckout}
/>
```

### Charts Components

Located in `src/components/charts/`.

All chart components use **Recharts** library.

#### LineChart
```typescript
import { LineChart } from '@/components/charts/LineChart';

<LineChart
  data={chartData}
  xKey="date"
  yKey="revenue"
  title="Revenue Trend"
/>
```

#### BarChart
```typescript
import { BarChart } from '@/components/charts/BarChart';

<BarChart
  data={chartData}
  xKey="product"
  yKey="sales"
  title="Top Products"
/>
```

#### PieChart
```typescript
import { PieChart } from '@/components/charts/PieChart';

<PieChart
  data={chartData}
  nameKey="category"
  valueKey="amount"
  title="Expense Categories"
/>
```

---

## Layout Components

Located in `src/components/layout/`.

### Layout
Main application layout with sidebar and navbar.

```typescript
import { Layout } from '@/components/layout/Layout';

<Layout>
  {/* Page content */}
</Layout>
```

### Navbar
Top navigation bar with user menu, notifications.

### Sidebar
Side navigation with role-based menu items.

---

## Utility Components

### LoadingSpinner
```typescript
import { LoadingSpinner } from '@/components/ui/loading-spinner';

<LoadingSpinner size="md" />
// Sizes: sm, md, lg
```

### CurrencySelector
```typescript
import { CurrencySelector } from '@/components/ui/currency-selector';

<CurrencySelector />
```

### LanguageSwitcher
```typescript
import { LanguageSwitcher } from '@/components/ui/LanguageSwitcher';

<LanguageSwitcher />
```

---

**Document Version**: 1.0
**Last Updated**: January 2025
