const mongoose = require('mongoose');
const User = require('./User');

const customerSchema = new mongoose.Schema({});

const Customer = User.discriminator('Customer', customerSchema);

module.exports = Customer;
