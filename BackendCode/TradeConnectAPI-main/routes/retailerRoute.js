const express = require('express');
const { signup,signin, addStockInRetailerInventory, addStocksToRetailerInventory, myInventory,getBatchesByIds, updateBatch, createOrder, myOrders, updateOrder, getUser, updateRegistrationToken} = require('../controllers/retailer');
const { addProducts, getAllProducts, searchProduct, getRetailerProducts } = require('../controllers/product');
const { verifyToken } = require('../middlewares/verifyToken');
const { createConnectionRequest, getMyConnections, updateConnectionStatus, searchRetailers } = require('../controllers/connection');
const { getSales, createSale, updateSale } = require('../controllers/sell');
const { getBatchByBatchNo, myProfile, getLastNdaysOrders, getLastNdaysSales } = require('../controllers/common');
const { downloadSampleSheet } = require('../controllers/sampleSheet');

const router= express.Router();
router.route('/retailer/signup').post(signup)
router.route('/retailer/signin').post(signin)
//Product routes
router.route('/retailer/product_list').get(getAllProducts);
router.route('/retailer/search_product').get(searchProduct);
// router.route('/retailer/add_products').post(verifyToken,addProducts);

// Retailer views his inventory or store
// He can view his inventory in which he will se each product name and its quantity there.
// He can search for a product and add in his inventory (Search API)
router.route('/retailer/my_inventory').get(verifyToken, myInventory);
router.route('/retailer/add_batch_to_inventory').post(verifyToken, addStockInRetailerInventory);
router.route('/retailer/add_batches_in_bulk').post(verifyToken,addStocksToRetailerInventory);
router.route('/retailer/get_batches_by_id').get(verifyToken, getBatchesByIds);//Returns product details of the inventory
router.route('/retailer/update_batch').put(verifyToken, updateBatch);

// Retailer creates order to other retailer
// in recipent list we will show my_connections APIs response parameter as recipent id
// then after selecting the recipent we will show response of product list api for that selected recipent
router.route('/retailer/create_order').post(verifyToken, createOrder)
router.route('/retailer/:retailerId/products').get(getRetailerProducts);
router.route('/retailer/my_orders').get(verifyToken, myOrders)
router.route('/retailer/update_order/:id').put(verifyToken, updateOrder)
// Retailer creates sell to other retailer and customer too
// in receiver list we will not restrict the Retailer for his connections/ same he can enter any email for customer 
// he can just enter the email and 
router.route('/retailer/my_products').get(verifyToken, getRetailerProducts);
router.route('/retailer/my_sells').get(verifyToken, getSales)
router.route('/retailer/record_a_sell').post(verifyToken, createSale)
router.route('/retailer/:saleId/update_sale').put(updateSale);

//My connections
router.route('/retailer/my_connections').get(verifyToken, getMyConnections)
router.route('/retailer/create_connection_req').post(verifyToken, createConnectionRequest)
router.route('/retailer/update_connection_req/:connectionId').put(verifyToken, updateConnectionStatus)
router.route('/retailer/search_retailers').get(verifyToken, searchRetailers);


router.route('/get_user').get(getUser);
router.route('/retailer/:batchNo/get_batch_by_no').get(verifyToken, getBatchByBatchNo)

router.route('/retailer/my_profile').get(verifyToken, myProfile)


//Analytics
router.route('/retailer/my_orders_analytics').get(verifyToken, getLastNdaysOrders);
router.route('/retailer/my_sales_analytics').get(verifyToken, getLastNdaysSales);

//Sample Sheet
router.route('/retailer/sample_sheet').get(downloadSampleSheet);

//update registrationToken
router.route('/retailer/update_token').put(verifyToken, updateRegistrationToken);

module.exports = router;