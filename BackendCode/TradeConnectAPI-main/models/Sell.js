const mongoose = require('mongoose');

// Define the schema for the Sales collection
const salesSchema = new mongoose.Schema({
  fromRetailerId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Retailer',
    required: true
  },
  toRetailerId: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Retailer',
  },
  isCustomerSale: {
    type: Boolean,
    default: false
  },
  customerEmail:{
    type: String,
  },
  customerName:{
    type: String
  },
  batches: [{
    batchId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Batch',
      required: true
    },
    batchNo: {
      type: String,
      required: true
    },
    quantity: {
      type: Number,
      required: true
    },
    price: {
      type: Number,
      required: true
    }
  }],
  totalPrice: {
    type: Number,
    required: true
  },
  paid: {
    type: Number,
    required: true
  },
  due: {
    type: Number,
    default: 0
  },
  createdAt: {
    type: Date,
    default: Date.now
  },
});

// Create the Sales model
const Sales = mongoose.model('Sales', salesSchema);

// Export the model
module.exports = Sales;
