const cron = require('node-cron');
const moment = require('moment');
const PDFDocument = require("pdfkit-table");
const Stock = require('../models/Stock');
const Batch = require('../models/Batch');
const { mailer } = require('../mailer');
const Sale = require('../models/Sell');
const { sendMultiplePushNotifications } = require('../firebaseNotifications');

const task = async function() {
    let dueSells = await Sale.find({ due: { $gt: 0 } }).populate('fromRetailerId toRetailerId');


    let mailData = new Map();
    let usersToBeNotified = dueSells.map(due=> due.toRetailerId?.registrationToken).filter(token=> token != null);
    const usersToBeNotifiedSet = new Set(usersToBeNotified);
    var notificationPayload = {
          title: "Payment due alert",
          body: "You have one or more payments pending of your orders, please check mail for more details"
    };
    dueSells.forEach(due=>{
        if(mailData.has(due.toRetailerId?.email||due.customerEmail)){
            let arr = mailData.get(due.toRetailerId?.email||due.customerEmail);
            arr.push(due);
            mailData.set(due.toRetailerId?.email||due.customerEmail, arr);
        }
        else{
            let arr = [];
            arr.push(due);
            mailData.set(due.toRetailerId?.email||due.customerEmail, arr);
        }
    })
  sendMultiplePushNotifications(notificationPayload, Array.from(usersToBeNotifiedSet));
  for (let rec of mailData.keys()) {
    const pdfBuffer =  await createOrderPDF(mailData.get(rec));

    mailOptions = {
        from: process.env.MAIL_USER,
        to: rec,
        //TODO: add order Receiver's mail
        subject: `You have due payments on your Digital payments book account`,
        text: 'Hello, Please find attached a list of Transactions that are due in from your side',
        attachments: [{
            filename: `Due.pdf`,
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


async function createOrderPDF(dueData) {

    const doc = new PDFDocument();
    doc.font('Helvetica-Bold');
    doc.fontSize(16);
    doc.text('Expiry Details', { align: 'center' });
  
    doc.moveDown();
    doc.font('Helvetica');
    doc.fontSize(12);
  
    const tableData = {
      headers: [ 'Sell Id', 'Due From','Total Amount', 'Paid', 'Due', 'Created On'],
      rows: []
    };
  
    dueData.forEach(due => {
      tableData.rows.push([due.id, due.fromRetailerId.businessName, due.totalPrice, due.paid , due.due , moment(due.createdAt).format('MMMM Do YYYY')]);
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
        2: 150,
        3: 150,
        4: 150,
        5: 150 
      },
      headerAlignment: 'left',
      rowAlignment: 'left'
    }
    await doc.table(table, { 
      columnsSize: [ 200, 100, 100, 100, 100, 100 ],
    });
    return new Promise((resolve, reject) => {
      const chunks = [];
      doc.on('data', (chunk) => chunks.push(chunk));
      doc.on('end', () => resolve(Buffer.concat(chunks)));
      doc.on('error', reject);
      doc.end();
    });
  }


exports.startPaymentAlerts = ()=>{
    console.log("Payment Alerts running");
    scheduler.start();
    task();
}