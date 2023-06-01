const mongoose = require('mongoose');

// Define the schema for the Inventory collection
const stockSchema = new mongoose.Schema({
  retailer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Retailer',
    required: true
  },
  batch: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Batch',
    required: true
  },
  buyingPrice: {
    type: Number,
    required: true
  },
  sellingPrice: {
    type: Number
  },
  quantity: {
    type: Number,
    required: true
  }
});

// Create the Inventory model
const Stock = mongoose.model('Stock', stockSchema);

// Export the model
module.exports = Stock;
