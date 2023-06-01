const mongoose = require('mongoose');

// Define the schema for the Sales collection
const orderSchema = new mongoose.Schema({
  creator: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Retailer',
    required: true
  },
  receiver: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'Retailer',
    required: true
  },
  status: {
    type: String,
    default: 'active',
    enum: ['inactive', 'active', 'cancelled','declined']
  },
  batches: [{
    batchId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: 'Batch'
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
  }
  ],
  totalAmount:{
    type: Number,
    required: true
  },
  createdAt: {
    type: Date,
    default: Date.now
  },
});

// Create the Sales model
const Order = mongoose.model('Order', orderSchema);

// Export the model
module.exports = Order;
