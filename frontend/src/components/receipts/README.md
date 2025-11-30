# PDF Receipt Component

This directory contains the PDF receipt generation components and utilities using `@react-pdf/renderer`.

## Components

### PDFReceipt

A clean, professional PDF receipt component that renders sales transaction data in a beautiful format.

**Features:**
- Professional header with shop branding
- Transaction and customer information
- Itemized product list with quantities and prices
- Subtotal, tax, discount, and total calculations
- Payment method display
- Optional notes section
- Clean footer with thank you message

**Usage:**

```tsx
import { PDFReceipt } from '@/components/receipts/PDFReceipt'
import { pdf } from '@react-pdf/renderer'

// Generate PDF blob
const blob = await pdf(
  PDFReceipt({
    transaction: salesTransaction,
    shopAddress: '123 Main Street, Lagos',
    shopPhone: '+234 123 456 7890',
    shopEmail: 'shop@example.com',
  })
).toBlob()
```

## Hooks

### usePDFReceipt

A utility hook that provides methods for generating, downloading, printing, and previewing PDF receipts.

**Available Methods:**

1. **downloadReceipt(transaction, options)** - Download PDF receipt
2. **printReceipt(transaction, options)** - Open print dialog for receipt
3. **previewReceipt(transaction, options)** - Preview receipt in new tab
4. **downloadReceiptByTransactionId(transactionId, options)** - Fetch transaction and download
5. **printReceiptByTransactionId(transactionId, options)** - Fetch transaction and print
6. **previewReceiptByTransactionId(transactionId, options)** - Fetch transaction and preview
7. **generateReceiptBlob(transaction, options)** - Generate blob for custom usage
8. **fetchTransaction(transactionId)** - Fetch transaction data from API

**Usage Example:**

```tsx
import { usePDFReceipt } from '@/hooks/usePDFReceipt'

const MyComponent = () => {
  const { 
    downloadReceiptByTransactionId, 
    printReceiptByTransactionId 
  } = usePDFReceipt()

  const handlePrint = async () => {
    await printReceiptByTransactionId('transaction-id-123', {
      shopAddress: '123 Main Street, Lagos',
      shopPhone: '+234 123 456 7890',
      shopEmail: 'shop@example.com',
    })
  }

  return <button onClick={handlePrint}>Print Receipt</button>
}
```

## Receipt Options

The receipt options object supports the following properties:

```typescript
interface ReceiptOptions {
  shopAddress?: string  // Shop physical address
  shopPhone?: string    // Shop contact phone
  shopEmail?: string    // Shop email address
}
```

## Styling

The receipt uses a professional color scheme:
- **Primary Blue**: `#1e40af` - Headers and highlights
- **Background**: Alternating row colors for readability
- **Text**: Dark gray on white for optimal contrast
- **Success Green**: Payment confirmation section
- **Warning Yellow**: Notes section

## Future Enhancements

- [ ] Add shop logo support
- [ ] Add QR code for receipt verification
- [ ] Support multiple languages (i18n)
- [ ] Add barcode for product tracking
- [ ] Custom branding colors from shop settings
- [ ] Email integration for sending receipts
- [ ] Receipt templates (compact, detailed, thermal printer)

## Dependencies

- `@react-pdf/renderer` - PDF generation library
- `date-fns` - Date formatting

## Notes

- The receipt is optimized for A4 paper size
- All currency values use Nigerian Naira (₦) formatting
- Dates are formatted in a human-readable format
- The PDF is generated client-side for privacy and speed
