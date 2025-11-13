// API Response Types
export interface ApiResponse<T> {
  data: T
  message?: string
  success: boolean
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}

// User and Authentication Types
export interface User {
  id: string
  tenantId: string
  keycloakId: string
  username: string
  email: string
  firstName?: string
  lastName?: string
  phoneNumber?: string
  status: UserStatus
  roles: Role[]
  isInvestor: boolean
}

export enum UserStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  DELETED = 'DELETED'
}

export interface Role {
  id: string
  name: string
  description?: string
  permissions: Permission[]
}

export interface Permission {
  id: string
  name: string
  resource: string
  action: string
}

// Tenant and Shop Types
export interface Tenant {
  id: string
  name: string
  description?: string
  companyRegistration?: string
  taxId?: string
  contactEmail: string
  contactUser?: User
  contactPhone?: string
  primaryAddress?: string
  city?: string
  state?: string
  country?: string
  postalCode?: string
  status: TenantStatus
  createdDate: string
  shops: Shop[]
  users: User[]
}

export enum TenantStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  TERMINATED = 'TERMINATED'
}

export interface Shop {
  id: string
  name: string
  tenant: Tenant
  description?: string
  address?: string
  city?: string
  state?: string
  country?: string
  postalCode?: string
  phoneNumber?: string
  email?: string
  taxId?: string
  status: ShopStatus
  openingDate: string
  customization?: ShopCustomization
}

export enum ShopStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  CLOSED = 'CLOSED'
}

export interface ShopCustomization {
  id: string
  shopId: string
  logoUrl?: string
  primaryColor?: string
  secondaryColor?: string
  fontFamily?: string
  theme?: string
}

// Product and Inventory Types
export interface Product {
  id: string
  name: string
  description?: string
  categoryId: string
  category?: string // Category name for display
  categoryName?: string // Alternative field name from API
  shopId?: string
  shopName?: string
  price: number
  costPrice?: number
  profitMargin?: number
  profitMarginPercentage?: number
  barcode?: string
  sku: string
  unit?: string
  weightInGrams?: number
  location?: string
  dimensions?: string
  supplierName?: string
  supplierContact?: string
  imageUrl?: string
  isTaxable?: boolean
  taxable?: boolean // Alternative field name from API
  taxRate?:number
  isDiscountable?: boolean
  discountable?: boolean // Alternative field name from API
  status: ProductStatus
  totalStock?: number
  availableStock: number
  reservedStock?: number
  inventoryCount?: number
  hasLowStock?: boolean
  hasExpiredBatches?: boolean
  tenant?: Tenant
  createdAt: string
  updatedAt: string
  createdBy?: string
  updatedBy?: string
}

export enum ProductStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  DISCONTINUED = 'DISCONTINUED'
}

export interface ProductCreateRequest {
  name: string
  description?: string | undefined
  sku?: string | undefined
  barcode?: string | undefined
  shopId: string
  categoryId: string
  price: number
  costPrice?: number | undefined
  unit?: string | undefined
  weightInGrams?: number | undefined
  location?: string | undefined
  dimensions?: string | undefined
  supplierName?: string | undefined
  supplierContact?: string | undefined
  imageUrl?: string | undefined
  isTaxable?: boolean | undefined
  isDiscountable?: boolean | undefined
  metadata?: Record<string, any> | undefined
}

export interface ProductUpdateRequest {
  name?: string | undefined
  description?: string | undefined
  categoryId?: string | undefined
  price?: number | undefined
  costPrice?: number | undefined
  barcode?: string | undefined
  sku?: string | undefined
  unit?: string | undefined
  weightInGrams?: number | undefined
  location?: string | undefined
  dimensions?: string | undefined
  supplierName?: string | undefined
  supplierContact?: string | undefined
  imageUrl?: string | undefined
  isTaxable?: boolean | undefined
  isDiscountable?: boolean | undefined
  status?: ProductStatus | undefined
}

export interface Inventory {
  id: string
  product: Product
  shop: Shop
  quantity: number
  reservedQuantity: number
  availableQuantity: number
  reorderLevel: number
  maxStockLevel: number
  lastRestocked: string
  status: InventoryStatus
}

export enum InventoryStatus {
  IN_STOCK = 'IN_STOCK',
  LOW_STOCK = 'LOW_STOCK',
  OUT_OF_STOCK = 'OUT_OF_STOCK',
  OVERSTOCKED = 'OVERSTOCKED'
}

// Sales and Receipt Types
export interface SalesTransaction {
  id: string
  shop: Shop
  totalAmount: number
  discountAmount?: number
  taxAmount?: number
  paymentMethod: string
  status: TransactionStatus
  createdAt: string
  lineItems: LineItem[]
  receipt?: Receipt
}

export enum TransactionStatus {
  PENDING = 'PENDING',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export interface LineItem {
  id: string
  product: Product
  quantity: number
  unitPrice: number
  totalPrice: number
  discountAmount?: number
}

export interface Receipt {
  id: string
  transactionId: string
  receiptNumber: string
  generatedAt: string
  printedAt?: string
  emailedAt?: string
  status: ReceiptStatus
  pdfPath?: string
}

export enum ReceiptStatus {
  GENERATED = 'GENERATED',
  PRINTED = 'PRINTED',
  EMAILED = 'EMAILED',
  FAILED = 'FAILED'
}

// Investment Types
export interface Investment {
  id: string
  investor: User
  shop: Shop
  amount: number
  investmentType: InvestmentType
  investmentDate: string
  maturityDate?: string
  expectedReturn?: number
  actualReturn?: number
  status: InvestmentStatus
  riskLevel: RiskLevel
}

export enum InvestmentType {
  EQUITY = 'EQUITY',
  DEBT = 'DEBT',
  REVENUE_SHARE = 'REVENUE_SHARE'
}

export enum InvestmentStatus {
  ACTIVE = 'ACTIVE',
  MATURED = 'MATURED',
  WITHDRAWN = 'WITHDRAWN',
  DEFAULTED = 'DEFAULTED'
}

export enum RiskLevel {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH'
}

// Analytics Types
export interface SalesSummary {
  totalRevenue: number
  totalTransactions: number
  averageOrderValue: number
  topProducts: ProductSales[]
  revenueByDay: RevenueData[]
}

export interface ProductSales {
  product: Product
  quantity: number
  revenue: number
}

export interface RevenueData {
  date: string
  revenue: number
  transactions: number
}

export interface InvestmentROI {
  totalInvestments: number
  totalReturns: number
  roiPercentage: number
  investmentsByType: InvestmentTypeData[]
  returnsByMonth: ReturnData[]
}

export interface InvestmentTypeData {
  type: InvestmentType
  amount: number
  count: number
}

export interface ReturnData {
  month: string
  returns: number
  investments: number
}

// Audit Types
export interface AuditLog {
  id: string
  entityType: string
  entityId: string
  action: AuditAction
  userId?: string
  userName?: string
  description: string
  metadata?: Record<string, any>
  timestamp: string
  success: boolean
  shop?: Shop
}

export enum AuditAction {
  CREATE = 'CREATE',
  UPDATE = 'UPDATE',
  DELETE = 'DELETE',
  LOGIN = 'LOGIN',
  LOGOUT = 'LOGOUT',
  PERMISSION_GRANTED = 'PERMISSION_GRANTED',
  PERMISSION_DENIED = 'PERMISSION_DENIED',
  SECURITY_EVENT = 'SECURITY_EVENT'
}