const express = require('express');
const { getAllBusinessTypes, createBusinessTypes } = require('../controllers/common');

const router= express.Router();

router.route('/admin/create_business_types').post(createBusinessTypes)
router.route('/business_types').get(getAllBusinessTypes);

module.exports = router;