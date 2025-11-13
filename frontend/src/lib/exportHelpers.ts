/**
 * Client-side export utilities for CSV and PDF
 * No backend API required - generates files directly in the browser
 */

/**
 * Convert array of objects to CSV string
 */
export function convertToCSV(data: any[], headers?: string[]): string {
  if (!data || data.length === 0) {
    return ''
  }

  // Get headers from first object if not provided
  const csvHeaders = headers || Object.keys(data[0])
  
  // Create header row
  const headerRow = csvHeaders.join(',')
  
  // Create data rows
  const dataRows = data.map(item => {
    return csvHeaders.map(header => {
      let value = item[header]
      
      // Handle null/undefined
      if (value === null || value === undefined) {
        value = ''
      }
      
      // Convert to string and escape quotes
      value = String(value).replace(/"/g, '""')
      
      // Wrap in quotes if contains comma, newline, or quote
      if (value.includes(',') || value.includes('\n') || value.includes('"')) {
        value = `"${value}"`
      }
      
      return value
    }).join(',')
  }).join('\n')
  
  return `${headerRow}\n${dataRows}`
}

/**
 * Download CSV file
 */
export function downloadCSV(data: any[], filename: string, headers?: string[]): void {
  const csv = convertToCSV(data, headers)
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  link.setAttribute('href', url)
  link.setAttribute('download', filename)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * Export table to CSV from DOM table element
 */
export function exportTableToCSV(tableElement: HTMLTableElement, filename: string): void {
  const rows: string[][] = []
  
  // Get all rows
  const tableRows = tableElement.querySelectorAll('tr')
  
  tableRows.forEach(row => {
    const cells: string[] = []
    const tableCells = row.querySelectorAll('th, td')
    
    tableCells.forEach(cell => {
      let text = cell.textContent || ''
      text = text.trim()
      
      // Escape quotes and wrap if needed
      text = text.replace(/"/g, '""')
      if (text.includes(',') || text.includes('\n') || text.includes('"')) {
        text = `"${text}"`
      }
      
      cells.push(text)
    })
    
    if (cells.length > 0) {
      rows.push(cells)
    }
  })
  
  const csv = rows.map(row => row.join(',')).join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  link.setAttribute('href', url)
  link.setAttribute('download', filename)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * Generate PDF from HTML content using browser print
 * This opens the print dialog with PDF as an option
 */
export function exportToPDF(contentId: string, title: string): void {
  const content = document.getElementById(contentId)
  if (!content) {
    console.error(`Element with id "${contentId}" not found`)
    return
  }
  
  // Create a new window with only the content to print
  const printWindow = window.open('', '_blank', 'width=800,height=600')
  if (!printWindow) {
    console.error('Could not open print window')
    return
  }
  
  // Write content to new window with styling
  printWindow.document.write(`
    <!DOCTYPE html>
    <html>
      <head>
        <title>${title}</title>
        <style>
          body {
            font-family: Arial, sans-serif;
            padding: 20px;
            color: #000;
          }
          table {
            width: 100%;
            border-collapse: collapse;
            margin: 20px 0;
          }
          th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: left;
          }
          th {
            background-color: #f2f2f2;
            font-weight: bold;
          }
          h1 {
            color: #333;
            margin-bottom: 20px;
          }
          .no-print {
            display: none;
          }
          @media print {
            body {
              padding: 0;
            }
            .no-print {
              display: none !important;
            }
          }
        </style>
      </head>
      <body>
        <h1>${title}</h1>
        ${content.innerHTML}
      </body>
    </html>
  `)
  
  printWindow.document.close()
  
  // Wait for content to load then print
  printWindow.onload = () => {
    printWindow.focus()
    setTimeout(() => {
      printWindow.print()
      // Don't close automatically - let user close after saving PDF
    }, 250)
  }
}

/**
 * Advanced PDF export using canvas and jsPDF (requires jspdf package)
 * This is a more sophisticated approach but requires additional dependency
 */
export async function exportToPDFAdvanced(
  contentId: string, 
  filename: string,
  title?: string
): Promise<void> {
  try {
    // Check if html2canvas and jsPDF are available
    const html2canvas = (window as any).html2canvas
    const jsPDF = (window as any).jsPDF
    
    if (!html2canvas || !jsPDF) {
      console.warn('html2canvas or jsPDF not loaded, falling back to print dialog')
      exportToPDF(contentId, title || filename)
      return
    }
    
    const content = document.getElementById(contentId)
    if (!content) {
      console.error(`Element with id "${contentId}" not found`)
      return
    }
    
    // Convert HTML to canvas
    const canvas = await html2canvas(content, {
      scale: 2,
      logging: false,
      useCORS: true
    })
    
    // Convert canvas to PDF
    const imgWidth = 210 // A4 width in mm
    const pageHeight = 297 // A4 height in mm
    const imgHeight = (canvas.height * imgWidth) / canvas.width
    
    const pdf = new jsPDF('p', 'mm', 'a4')
    let heightLeft = imgHeight
    let position = 0
    
    // Add title if provided
    if (title) {
      pdf.setFontSize(16)
      pdf.text(title, 10, 10)
      position = 15
    }
    
    pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight
    
    // Add new pages if content is longer than one page
    while (heightLeft > 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(canvas.toDataURL('image/png'), 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }
    
    pdf.save(filename)
  } catch (error) {
    console.error('Error generating PDF:', error)
    // Fallback to print dialog
    exportToPDF(contentId, title || filename)
  }
}

/**
 * Format data for inventory export
 */
export function formatInventoryForExport(items: any[]): any[] {
  return items.map(item => ({
    'Product Name': item.productName || '',
    'SKU': item.productSku || '',
    'Current Stock': item.currentStock || 0,
    'Minimum Stock': item.minimumStock || 0,
    'Maximum Stock': item.maximumStock || 0,
    'Reorder Point': item.reorderPoint || 0,
    'Unit Cost': item.unitCost || 0,
    'Total Value': item.unitCost ? (item.currentStock * item.unitCost) : 0,
    'Status': item.status || '',
    'Location': item.location || '',
    'Last Updated': item.updatedAt ? new Date(item.updatedAt).toLocaleDateString() : ''
  }))
}

/**
 * Format data for low stock export
 */
export function formatLowStockForExport(items: any[]): any[] {
  return items.map(item => ({
    'Product Name': item.productName || '',
    'SKU': item.productSku || '',
    'Current Stock': item.currentStock || 0,
    'Minimum Stock': item.minimumStock || 0,
    'Reorder Point': item.reorderPoint || 0,
    'Suggested Reorder': item.maximumStock ? (item.maximumStock - item.currentStock) : (item.reorderPoint * 2),
    'Status': item.currentStock < item.minimumStock ? 'Critical' : 'Low Stock',
    'Unit Cost': item.unitCost || 0,
    'Total Value': item.unitCost ? (item.currentStock * item.unitCost) : 0
  }))
}

/**
 * Format data for expiring items export
 */
export function formatExpiringItemsForExport(items: any[]): any[] {
  return items.map(item => {
    const daysLeft = item.expiryDate 
      ? Math.floor((new Date(item.expiryDate).getTime() - Date.now()) / (1000 * 60 * 60 * 24))
      : null
    const status = item.isExpiringSoon ? 'Expiring Soon' : 'Normal'
    return {
      'Product Name': item.productName || '',
      'SKU': item.productSku || '',
      'Batch Number': item.batchNumber || '',
      'Quantity': item.currentStock || 0,
      'Expiry Date': item.expiryDate ? new Date(item.expiryDate).toLocaleDateString() : '',
      'Days Until Expiry': item.isExpired ? 'Expired' : daysLeft,
      'Status': item.isExpired ? 'Expired' : status,
      'Unit Cost': item.unitCost || 0,
      'Value at Risk': item.unitCost ? (item.currentStock * item.unitCost) : 0
    }
  })
}
