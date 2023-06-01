const mongoose = require('mongoose');
const User = require('./User');

const retailerSchema = new mongoose.Schema({
  businessName: {
    type: String,
    required: true
  },
  businessType: { 
    type: mongoose.Schema.Types.ObjectId,
    ref: 'BusinessType'
  },
  totalSales: {
    type: String,
    default: 0
  }
});

const Retailer = User.discriminator('Retailer', retailerSchema);
module.exports = Retailer;
