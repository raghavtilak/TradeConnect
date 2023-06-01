const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const businessTypeSchema = new Schema({
  name: {
    type: String,
    required: true,
    unique: true
  },
  description: {
    type: String,
    required: true
  }
});

module.exports = mongoose.model('BusinessType', businessTypeSchema);
