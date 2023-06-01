const express = require('express');
const { signup, customerTransactions, getMyRetailers } = require('../controllers/customer');
const { signin, updateRegistrationToken } = require('../controllers/retailer');
const { verifyToken } = require('../middlewares/verifyToken');
const router= express.Router();

router.route('/customer/signup').post(signup)
router.route('/customer/signin').post(signin)
router.route('/customer/my_retailers').get(getMyRetailers);
router.route('/customer/my_transactions').get(verifyToken, customerTransactions)

router.route('/customer/update_token').put(verifyToken, updateRegistrationToken);
module.exports = router;