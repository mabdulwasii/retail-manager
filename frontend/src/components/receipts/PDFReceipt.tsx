import React from 'react'
import {
  Document,
  Page,
  Text,
  View,
  StyleSheet,
  Font,
} from '@react-pdf/renderer'
import { SalesTransaction } from '@/hooks/useSales'
import { format } from 'date-fns'

// Register fonts (optional - using default for now)
// Font.register({
//   family: 'Inter',
//   src: 'https://fonts.gstatic.com/s/inter/v12/UcCO3FwrK3iLTeHuS_fvQtMwCp50KnMw2boKoduKmMEVuLyfAZ9hiA.woff2',
// })

const styles = StyleSheet.create({
  page: {
    padding: 40,
    fontSize: 10,
    fontFamily: 'Helvetica',
    backgroundColor: '#ffffff',
  },
  
  // Header Section
  header: {
    marginBottom: 20,
    borderBottom: 2,
    borderBottomColor: '#2563eb',
    paddingBottom: 15,
  },
  shopName: {
    fontSize: 24,
    fontFamily: 'Helvetica-Bold',
    color: '#1e40af',
    marginBottom: 5,
  },
  shopInfo: {
    fontSize: 9,
    color: '#64748b',
    marginBottom: 2,
  },
  
  // Receipt Title
  receiptTitle: {
    textAlign: 'center',
    fontSize: 16,
    fontFamily: 'Helvetica-Bold',
    color: '#0f172a',
    marginVertical: 15,
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  
  // Info Section
  infoSection: {
    marginBottom: 20,
    backgroundColor: '#f8fafc',
    padding: 12,
    borderRadius: 4,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 6,
  },
  infoLabel: {
    fontSize: 9,
    color: '#64748b',
    fontFamily: 'Helvetica',
  },
  infoValue: {
    fontSize: 9,
    color: '#0f172a',
    fontFamily: 'Helvetica-Bold',
  },
  
  // Customer Section
  customerSection: {
    marginBottom: 20,
    padding: 10,
    backgroundColor: '#f1f5f9',
    borderRadius: 4,
  },
  customerTitle: {
    fontSize: 10,
    fontFamily: 'Helvetica-Bold',
    color: '#475569',
    marginBottom: 6,
  },
  customerText: {
    fontSize: 9,
    color: '#334155',
    marginBottom: 3,
  },
  
  // Table
  table: {
    marginBottom: 20,
  },
  tableHeader: {
    flexDirection: 'row',
    backgroundColor: '#1e40af',
    padding: 8,
    borderTopLeftRadius: 4,
    borderTopRightRadius: 4,
  },
  tableHeaderText: {
    fontSize: 9,
    fontFamily: 'Helvetica-Bold',
    color: '#ffffff',
  },
  tableRow: {
    flexDirection: 'row',
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
    padding: 8,
    alignItems: 'center',
  },
  tableRowAlt: {
    backgroundColor: '#f8fafc',
  },
  
  // Table Columns
  colItem: {
    width: '45%',
  },
  colQty: {
    width: '15%',
    textAlign: 'center',
  },
  colPrice: {
    width: '20%',
    textAlign: 'right',
  },
  colTotal: {
    width: '20%',
    textAlign: 'right',
  },
  
  tableText: {
    fontSize: 9,
    color: '#334155',
  },
  
  // Summary Section
  summarySection: {
    marginTop: 10,
    paddingTop: 15,
    borderTopWidth: 2,
    borderTopColor: '#cbd5e1',
  },
  summaryRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
    paddingHorizontal: 10,
  },
  summaryLabel: {
    fontSize: 10,
    color: '#475569',
  },
  summaryValue: {
    fontSize: 10,
    color: '#0f172a',
    fontFamily: 'Helvetica-Bold',
  },
  totalRow: {
    backgroundColor: '#1e40af',
    padding: 12,
    marginTop: 10,
    borderRadius: 4,
  },
  totalLabel: {
    fontSize: 12,
    color: '#ffffff',
    fontFamily: 'Helvetica-Bold',
  },
  totalValue: {
    fontSize: 14,
    color: '#ffffff',
    fontFamily: 'Helvetica-Bold',
  },
  
  // Payment Section
  paymentSection: {
    marginTop: 20,
    padding: 12,
    backgroundColor: '#ecfdf5',
    borderRadius: 4,
    borderWidth: 1,
    borderColor: '#6ee7b7',
  },
  paymentLabel: {
    fontSize: 9,
    color: '#065f46',
    marginBottom: 4,
  },
  paymentValue: {
    fontSize: 11,
    color: '#047857',
    fontFamily: 'Helvetica-Bold',
  },
  
  // Footer
  footer: {
    marginTop: 30,
    paddingTop: 15,
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
  },
  footerText: {
    fontSize: 8,
    color: '#64748b',
    textAlign: 'center',
    marginBottom: 4,
  },
  footerBold: {
    fontSize: 9,
    fontFamily: 'Helvetica-Bold',
    color: '#475569',
    textAlign: 'center',
    marginTop: 8,
  },
  
  // Notes Section
  notesSection: {
    marginTop: 15,
    padding: 10,
    backgroundColor: '#fef3c7',
    borderRadius: 4,
    borderWidth: 1,
    borderColor: '#fbbf24',
  },
  notesLabel: {
    fontSize: 9,
    fontFamily: 'Helvetica-Bold',
    color: '#78350f',
    marginBottom: 4,
  },
  notesText: {
    fontSize: 8,
    color: '#92400e',
    lineHeight: 1.4,
  },
})

interface PDFReceiptProps {
  transaction: SalesTransaction
  shopAddress?: string
  shopPhone?: string
  shopEmail?: string
}

export const PDFReceipt = ({
  transaction,
  shopAddress = '',
  shopPhone = '',
  shopEmail = '',
}: PDFReceiptProps) => {
  const formatCurrency = (amount: number) => {
    return `₦${amount.toLocaleString('en-NG', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`
  }

  const formatDate = (dateString: string) => {
    try {
      return format(new Date(dateString), 'MMM dd, yyyy h:mm a')
    } catch {
      return dateString
    }
  }

  return (
    <Document>
      <Page size="A4" style={styles.page}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.shopName}>{transaction.shopName || 'Retail Manager'}</Text>
          {shopAddress && <Text style={styles.shopInfo}>{shopAddress}</Text>}
          {shopPhone && <Text style={styles.shopInfo}>Tel: {shopPhone}</Text>}
          {shopEmail && <Text style={styles.shopInfo}>Email: {shopEmail}</Text>}
        </View>

        {/* Receipt Title */}
        <Text style={styles.receiptTitle}>Sales Receipt</Text>

        {/* Transaction Info */}
        <View style={styles.infoSection}>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Receipt Number:</Text>
            <Text style={styles.infoValue}>
              {transaction.receiptNumber || transaction.transactionNumber}
            </Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Transaction Number:</Text>
            <Text style={styles.infoValue}>{transaction.transactionNumber}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Date & Time:</Text>
            <Text style={styles.infoValue}>{formatDate(transaction.transactionDate)}</Text>
          </View>
          <View style={styles.infoRow}>
            <Text style={styles.infoLabel}>Cashier:</Text>
            <Text style={styles.infoValue}>{transaction.cashierName || 'N/A'}</Text>
          </View>
        </View>

        {/* Customer Info */}
        {(transaction.customerName || transaction.customerPhone) && (
          <View style={styles.customerSection}>
            <Text style={styles.customerTitle}>Customer Information</Text>
            {transaction.customerName && (
              <Text style={styles.customerText}>Name: {transaction.customerName}</Text>
            )}
            {transaction.customerPhone && (
              <Text style={styles.customerText}>Phone: {transaction.customerPhone}</Text>
            )}
            {transaction.customerEmail && (
              <Text style={styles.customerText}>Email: {transaction.customerEmail}</Text>
            )}
          </View>
        )}

        {/* Items Table */}
        <View style={styles.table}>
          {/* Table Header */}
          <View style={styles.tableHeader}>
            <Text style={[styles.tableHeaderText, styles.colItem]}>Item</Text>
            <Text style={[styles.tableHeaderText, styles.colQty]}>Qty</Text>
            <Text style={[styles.tableHeaderText, styles.colPrice]}>Price</Text>
            <Text style={[styles.tableHeaderText, styles.colTotal]}>Total</Text>
          </View>

          {/* Table Rows */}
          {transaction.lineItems.map((item, index) => (
            <View
              key={item.id}
              style={[styles.tableRow, index % 2 === 1 && styles.tableRowAlt] as any}
            >
              <Text style={[styles.tableText, styles.colItem]}>{item.productName}</Text>
              <Text style={[styles.tableText, styles.colQty]}>{item.quantity}</Text>
              <Text style={[styles.tableText, styles.colPrice]}>
                {formatCurrency(item.unitPrice)}
              </Text>
              <Text style={[styles.tableText, styles.colTotal]}>
                {formatCurrency(item.lineTotal)}
              </Text>
            </View>
          ))}
        </View>

        {/* Summary */}
        <View style={styles.summarySection}>
          <View style={styles.summaryRow}>
            <Text style={styles.summaryLabel}>Subtotal:</Text>
            <Text style={styles.summaryValue}>{formatCurrency(transaction.subtotal)}</Text>
          </View>

          {transaction.discountAmount > 0 && (
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Discount:</Text>
              <Text style={styles.summaryValue}>
                -{formatCurrency(transaction.discountAmount)}
              </Text>
            </View>
          )}

          {transaction.taxAmount > 0 && (
            <View style={styles.summaryRow}>
              <Text style={styles.summaryLabel}>Tax:</Text>
              <Text style={styles.summaryValue}>{formatCurrency(transaction.taxAmount)}</Text>
            </View>
          )}

          <View style={[styles.summaryRow, styles.totalRow]}>
            <Text style={styles.totalLabel}>TOTAL:</Text>
            <Text style={styles.totalValue}>{formatCurrency(transaction.totalAmount)}</Text>
          </View>
        </View>

        {/* Payment Method */}
        <View style={styles.paymentSection}>
          <Text style={styles.paymentLabel}>Payment Method</Text>
          <Text style={styles.paymentValue}>{transaction.paymentMethod}</Text>
        </View>

        {/* Notes */}
        {transaction.notes && (
          <View style={styles.notesSection}>
            <Text style={styles.notesLabel}>Notes:</Text>
            <Text style={styles.notesText}>{transaction.notes}</Text>
          </View>
        )}

        {/* Footer */}
        <View style={styles.footer}>
          <Text style={styles.footerText}>Thank you for your business!</Text>
          <Text style={styles.footerText}>
            This is a computer-generated receipt and is valid without signature.
          </Text>
          <Text style={styles.footerBold}>Please retain this receipt for your records.</Text>
        </View>
      </Page>
    </Document>
  )
}
