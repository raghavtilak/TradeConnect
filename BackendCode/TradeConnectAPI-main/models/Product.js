const mongoose = require('mongoose');

const productSchema = new mongoose.Schema({
  name: {
    type: String,
    required: true,
    unique: true
  },
  description: {
    type: String
  },
  sku: {
    type: String,
    unique: true,
    required: true
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

const Product = mongoose.model('Product', productSchema);
module.exports = Product;
