const mongoose = require('mongoose');

const batchSchema = new mongoose.Schema({
  batchNo:{
    type: String,
    required: true,
    unique: true,
  },
  product: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Product',
    required: true,
  },
  MRP: {
    type: Number,
    required: true
  },
  mfgDate: {
    type: Date,
    required: true
  },
  expiryDate: {
    type: Date
  },
  createdDate: {
    type: Date,
    default: Date.now
  },
  createdBy: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Retailer',
    required: true
  }
});

module.exports = mongoose.model('Batch', batchSchema);
