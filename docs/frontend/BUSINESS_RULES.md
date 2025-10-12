# Business Rules & Validation

**Version**: 1.0
**Last Updated**: January 2025

---

## Validation Schemas

All forms use **Yup** for validation with **React Hook Form**.

### Inventory Validation

```typescript
import * as yup from 'yup';

export const inventorySchema = yup.object({
  productId: yup.string().required('Product is required'),
  quantity: yup.number()
    .required('Quantity is required')
    .min(0, 'Quantity must be at least 0')
    .integer('Quantity must be a whole number'),
  reorderLevel: yup.number()
    .required('Reorder level is required')
    .min(0, 'Reorder level must be at least 0')
    .integer('Reorder level must be a whole number'),
  unitCost: yup.number()
    .required('Unit cost is required')
    .min(0, 'Unit cost must be positive'),
  location: yup.string().optional(),
  expiryDate: yup.date().optional()
    .min(new Date(), 'Expiry date must be in the future'),
});
```

### Investment Validation

```typescript
export const investmentSchema = yup.object({
  shopId: yup.string().required('Shop is required'),
  amount: yup.number()
    .required('Amount is required')
    .min(1000, 'Minimum investment is $1,000')
    .max(1000000, 'Maximum investment is $1,000,000'),
  investmentType: yup.string()
    .required('Investment type is required')
    .oneOf(['EQUITY', 'DEBT', 'REVENUE_SHARE']),
  profitSharePercentage: yup.number()
    .when('investmentType', {
      is: 'REVENUE_SHARE',
      then: (schema) => schema
        .required('Profit share percentage is required')
        .min(1, 'Minimum profit share is 1%')
        .max(50, 'Maximum profit share is 50%'),
    }),
  duration: yup.number()
    .required('Duration is required')
    .min(3, 'Minimum duration is 3 months')
    .max(60, 'Maximum duration is 60 months'),
});
```

### Expense Validation

```typescript
export const expenseSchema = yup.object({
  amount: yup.number()
    .required('Amount is required')
    .min(0.01, 'Amount must be greater than 0'),
  category: yup.string().required('Category is required'),
  description: yup.string()
    .required('Description is required')
    .min(10, 'Description must be at least 10 characters'),
  expenseDate: yup.date()
    .required('Expense date is required')
    .max(new Date(), 'Expense date cannot be in the future'),
  paymentMethod: yup.string().required('Payment method is required'),
  vendorName: yup.string().optional(),
  receiptReference: yup.string().optional(),
});
```

### Shop Validation

```typescript
export const shopSchema = yup.object({
  name: yup.string()
    .required('Shop name is required')
    .min(3, 'Shop name must be at least 3 characters')
    .max(100, 'Shop name must be at most 100 characters'),
  email: yup.string()
    .email('Invalid email format')
    .required('Email is required'),
  phoneNumber: yup.string()
    .matches(/^\+?[1-9]\d{1,14}$/, 'Invalid phone number format')
    .required('Phone number is required'),
  address: yup.string().required('Address is required'),
  city: yup.string().required('City is required'),
  state: yup.string().optional(),
  country: yup.string().required('Country is required'),
  postalCode: yup.string().optional(),
});
```

---

## Business Logic Rules

### Inventory Management

**Reorder Alerts**:
- Alert triggered when: `availableQuantity <= reorderLevel`
- Low stock threshold: 20% of reorder level
- Critical stock: 0 available quantity

**Expiry Management**:
- Warning threshold: 30 days before expiry
- Critical threshold: 7 days before expiry
- Auto-quarantine: Items past expiry date

**Stock Adjustment**:
- All adjustments must have a reason code
- Negative adjustments require manager approval if > 10% of stock
- System tracks adjustment history

### Investment Management

**Investment Constraints**:
- Minimum investment: $1,000
- Maximum single investment: $1,000,000
- Profit share range: 1% - 50%
- Duration range: 3 - 60 months

**Profit Distribution**:
- Calculated monthly based on actual profits
- Distribution = (Monthly Profit × Profit Share %)
- Requires shop owner approval before payment
- Payment within 15 days of approval

**ROI Calculation**:
```
ROI % = ((Total Returns - Initial Investment) / Initial Investment) × 100
```

### Expense Management

**Approval Workflow**:
- < $500: Auto-approved
- $500 - $5,000: Manager approval required
- > $5,000: Owner approval required

**Budget Tracking**:
- Monthly budget limits by category
- Alert at 80% of budget
- Hard stop at 100% (requires override)

### Product Returns

**Return Window**:
- Standard: 30 days from purchase
- Electronics: 14 days
- Perishables: 7 days

**Refund Policy**:
- New/unopened: 100% refund
- Opened/used: 80% refund or store credit
- Damaged: 50% refund or exchange only

**Restocking**:
- Returned items restocked only if condition = "NEW"
- Quality check required for all returns
- Damaged items sent to disposal

### Fraud Detection

**Alert Triggers**:
- Transaction > $10,000 (high-value alert)
- > 5 transactions in 1 hour from same user
- Multiple returns from same customer (> 3 in 7 days)
- Discount > 50% on single transaction
- After-hours transactions (shop closed)

**Risk Scoring**:
- Low Risk: 0-30 points
- Medium Risk: 31-60 points
- High Risk: 61-100 points

**Auto-Actions**:
- High risk transactions require manager approval
- Repeated high-risk patterns trigger account review

---

## Workflow State Machines

### Expense Approval Workflow

```
DRAFT → PENDING → APPROVED → PAID
              ↓
          REJECTED
```

### Investment Lifecycle

```
ACTIVE → MATURED → WITHDRAWN
   ↓
DEFAULTED
```

### Return Processing

```
INITIATED → REVIEWED → APPROVED → PROCESSED
                  ↓
              REJECTED
```

---

## Calculation Formulas

### Inventory Valuation

```
Total Value = Σ (Quantity × Unit Cost)
```

### Profit Margin

```
Profit Margin % = ((Selling Price - Cost Price) / Selling Price) × 100
```

### Expense Variance

```
Variance = Actual Spending - Budgeted Amount
Variance % = (Variance / Budgeted Amount) × 100
```

---

**Document Version**: 1.0
**Last Updated**: January 2025
