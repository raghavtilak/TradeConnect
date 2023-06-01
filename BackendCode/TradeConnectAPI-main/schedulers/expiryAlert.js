const cron = require('node-cron');
const moment = require('moment');
const PDFDocument = require("pdfkit-table");
const Stock = require('../models/Stock');
const Batch = require('../models/Batch');
const { mailer } = require('../mailer');
const { sendMultiplePushNotifications } = require('../firebaseNotifications');

const task = async function() {
    // Calculate the date that is 2 months from now
    console.log("Task Started");
  const twoMonthsFromNow = moment().add(2, 'months').toDate();

  // Find batches that will expire before or on the calculated date
  let expiringBatches = await Batch.find({
    expiryDate: { $lte: twoMonthsFromNow }
  }).select('id');
//   expiringBatches = expiringBatches.map(exp=>exp._id)
  const Stocks = await Stock.find({
    batch: { $in: expiringBatches }
  }).populate('retailer batch');
  let usersToBeNotified = Stocks.map(stock=> stock.retailer.registrationToken).filter(token=> token != null);
  const usersToBeNotifiedSet = new Set(usersToBeNotified);
  var notificationPayload = {
        title: "Product Expiry alert",
        body: "You have one or more products expiring in upcoming 2 months"
  };
  let mailData = new Map();
  Stocks.forEach(stock=>{
    if(mailData.has(stock.retailer.email)){
        let arr = mailData.get(stock.retailer.email);
        arr.push(stock.batch);
        mailData.set(stock.retailer.email, arr);
    }
    else{
        let arr = [];
        arr.push(stock.batch);
        mailData.set(stock.retailer.email, arr);
    }
  }) 
  sendMultiplePushNotifications(notificationPayload, Array.from(usersToBeNotifiedSet))
  for (let rec of mailData.keys()) {
    const pdfBuffer =  await createOrderPDF(mailData.get(rec));
    mailOptions = {
        from: process.env.MAIL_USER,
        to: rec,
        //TODO: add order Receiver's mail
        subject: `Expiry Alerts for Digital payments book inventory`,
        text: 'Hello, Please find attached a list of products that are expiring in upcoming 2 months',
        attachments: [{
            filename: `Expiry.pdf`,
            content: pdfBuffer
          }]
    }
    mailer(mailOptions)
  }

}
let scheduler = cron.schedule('0 0 * * 0', task , {
  scheduled: true,
  timezone: "Asia/Kolkata" // replace with your timezone
});

async function createOrderPDF(expiryData) {

    const doc = new PDFDocument();
    doc.font('Helvetica-Bold');
    doc.fontSize(16);
    doc.text('Expiry Details', { align: 'center' });
  
    doc.moveDown();
    doc.font('Helvetica');
    doc.fontSize(12);
  
    const tableData = {
      headers: [ 'Batch No', 'Expiry'],
      rows: []
    };
  
    expiryData.forEach(batch => {
      tableData.rows.push([batch.batchNo,moment(batch.expiryDate).format('MMMM Do YYYY')]);
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

exports.startExpiryAlerts = ()=>{
    console.log("Expiry Alerts running");
    scheduler.start();
}