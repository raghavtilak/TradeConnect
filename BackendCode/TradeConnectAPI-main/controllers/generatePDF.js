const PDFDocument = require("pdfkit-table");

exports.createSalePDF = async(saleData)=>{

    const doc = new PDFDocument();
    doc.font('Helvetica-Bold');
    doc.fontSize(16);
    doc.text('Sale Details', { align: 'center' });
  
    doc.moveDown();
    doc.font('Helvetica');
    doc.fontSize(12);
  
    const tableData = {
      headers: ['Product', 'Batch No', 'Quantity Delivered'],
      rows: []
    };
  
    saleData.batches.forEach(product => {
      tableData.rows.push([product.batchId.product.name, product.batchNo, product.quantity]);
    });
  
    const table =  {
      headers: tableData.headers,
      rows: tableData.rows,
      x: 50,
      y: 150,
      width: 500,
      columnWidths: {
        0: 200,
        1: 150,
        2: 150
      },
      headerAlignment: 'left',
      rowAlignment: 'left'
    }
    await doc.table(table, { 
      columnsSize: [ 200, 100, 100 ],
    });
    return new Promise((resolve, reject) => {
      const chunks = [];
      doc.on('data', (chunk) => chunks.push(chunk));
      doc.on('end', () => resolve(Buffer.concat(chunks)));
      doc.on('error', reject);
      doc.end();
    });
}