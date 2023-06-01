const { mailer } = require('../mailer');
const Batch = require('../models/Batch');
const Sell = require('../models/Sell');
const { updateInventories } = require('./common');
const { createSalePDF } = require('./generatePDF');

exports.createSale = async (req, res) => {
  try {
    const fromRetailerId = req.user;
    const { toRetailerId, isCustomerSale, customerEmail,customerName, batches, totalPrice, paid } = req.body;

    // Check if batches are empty
    if (batches.length === 0) {
      return res.status(400).json({ message: 'Batches cannot be empty' });
    }

    // Check if all required fields are present for each batch
    for (const batch of batches) {
      if (!batch.batchNo) {
        return res.status(400).json({ error: 'Batch number is required for all products.' });
      }
      else{
        batch.batchId = await Batch.findOne({batchNo: batch.batchNo}).select('id');
        batch.batchId = batch.batchId._id
      }

      if (!batch.quantity || batch.quantity <= 0) {
        return res.status(400).json({ error: 'Quantity is required and should be greater than 0.' });
      }
    }

    // Check if batch quantity is a positive integer
    const invalidQuantities = batches.filter(batch => !Number.isInteger(batch.quantity) || batch.quantity < 1);
    if (invalidQuantities.length > 0) {
      return res.status(400).json({ message: 'Batch quantity must be a positive integer' });
    }

    // Check if batch price is a positive number
    const invalidPrices = batches.filter(batch => batch.price <= 0);
    if (invalidPrices.length > 0) {
      return res.status(400).json({ message: 'Batch price must be a positive number' });
    }

    // Create the sale object
    const sale = new Sell({
      fromRetailerId,
      toRetailerId,
      isCustomerSale,
      customerName,
      customerEmail,
      batches,
      totalPrice,
      paid,
      due: totalPrice - paid,
    });

    // Save the sale object to the database
    const savedSale = await sale.save();

    await updateInventories(fromRetailerId, toRetailerId, sale.batches)
    const saleData = await Sell.findOne({_id: savedSale.id}).populate('fromRetailerId toRetailerId').populate('batches.batchId').populate({
      path: 'batches.batchId',
      populate: {
        path: 'product',
        model: 'Product'
      }
    })
    .exec();
    pdfBuffer = await createSalePDF(saleData);
    let receiver = saleData?.toRetailerId?.email
    let subject = `Sale created for your business ${saleData.toRetailerId?.businessName}`
    if(savedSale.isCustomerSale){
      receiver = savedSale.customerEmail
      subject = "Sale Invoice"
    }
    console.log(saleData);
    mailOptions = {
      from: process.env.MAIL_USER,
      to: receiver,
      //TODO: add order Receiver's mail
      subject: subject,
      text: `You have received an sale from ${saleData.fromRetailerId.businessName}, Please see the attached PDF for details of the products received.`,
      attachments: [{
        filename: `Sale_${savedSale._id}.pdf`,
        content: pdfBuffer
      }]
    };

    mailer(mailOptions)
    res.status(201).json({
      message:"Sale created successfully"
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Internal server error' });
  }
};
exports.updateSale = async (req,res)=>{
    try {
      const { saleId } = req.params; // Get the sale ID from the request parameters
      const { totalPrice, paid } = req.body; // Get the updated sale data from the request body
  
      // // Find the sale by its ID and update the specified fields
      // const updatedSale = await Sell.findByIdAndUpdate(
      //   saleId,
      //   { totalPrice, paid, due: totalPrice-paid },
      //   { new: true }
      // );
      const sell = await Sell.findById(saleId)
console.log(sell);
      if (paid > sell.due){
        return res.status(301).json({
          error: "paid amount should be less then due"
        })
      }
      sell.due = sell.due - paid;
      sell.paid = totalPrice - sell.due;
      const updatedSale =await sell.save();
      if (!updatedSale) {
        // If the sale is not found, return an error response
        return res.status(404).json({ error: 'Sale not found' });
      }
  
      // Return the updated sale as the response
      return res.json({message:"Sale Updated successfully"});
    } catch (error) {
      // Handle any errors that occur during the update process
      console.error(error);
      return res.status(500).json({ error: 'Internal server error' });
    }
  }
exports.getSales = async (req, res) => {
  try {
    const retailerId =req.user;
console.log(retailerId);
    const sales = await Sell.find({
      $or: [{ fromRetailerId: req.user }, { toRetailerId: req.user }],
    })
    .populate('toRetailerId', 'name businessName email')
    .populate('fromRetailerId', 'name businessName email')
    .populate({
      path: 'batches.batchId',
      populate: {
        path: 'product',
        model: 'Product',
        select: '-createdAt -updatedAt -__v' // exclude unwanted fields from the product document
      }
    })
    .sort({ date: -1 })
    .select('-__v')
    .exec();
    const salesJson = [];
    console.log(sales);
    sales.forEach(sale => {
      let obj = {
        _id: sale._id,
        toRetailer: sale.toRetailerId,
        isCustomerSale: sale.isCustomerSale,
        customerEmail: sale.customerEmail,
        isCreatedByUser: sale.fromRetailerId.id == req.user,
        customerName: sale.customerName,
        totalPrice: sale.totalPrice,
        paid: sale.paid,
        due: sale.due,
        date: sale.createdAt || sale.date,
        batches: []
      }
      sale.batches.forEach(batch=>{
        let obj2 = {
          batchNo: batch.batchNo,
          quantity: batch.quantity,
          sellingPrice: batch.price,
          MRP: batch.batchId.MRP,
          productName: batch.batchId.product.name
        }
        obj.batches.push(obj2);
      })
      salesJson.push(obj)
    })
    return res.status(200).json(salesJson);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: 'Server error' });
  }
};


