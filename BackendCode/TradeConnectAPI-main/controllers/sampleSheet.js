const XLSX = require('xlsx');

exports.downloadSampleSheet = (req, res) => {
    // Create a new workbook
    const workbook = XLSX.utils.book_new();
    console.log("Excel sent");
    // Add a worksheet to the workbook
    const worksheet = XLSX.utils.aoa_to_sheet([
      ['productName', 'batchNo', 'MRP', 'mfg', 'expiry', 'buyingPrice', 'sellingPrice', 'quantity'],
      ['Iphone 11', 'A12','50000','2023-01-01', '2023-02-01', '40000', '45000', '100']
    ]);
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet 1');
  
    // Generate a buffer from the workbook
    const buffer = XLSX.write(workbook, { type: 'buffer', bookType: 'xlsx' });
  
    // Set the appropriate headers for the response
    res.setHeader('Content-Type', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet');
    res.setHeader('Content-Disposition', 'attachment; filename=sample_bulk_stock_upload.xlsx');
  console.log("Excel sent");
    // Send the buffer as the response
    res.send(buffer);
  }