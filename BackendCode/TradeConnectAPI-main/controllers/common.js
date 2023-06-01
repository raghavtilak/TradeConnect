const { default: mongoose } = require("mongoose");
const Batch = require("../models/Batch");
const BusinessType = require("../models/BusinessType");
const Stock = require("../models/Stock");
const Retailer = require("../models/Retailer");
const Sales = require("../models/Sell");
const Connection = require("../models/Connection");
const Order = require("../models/Order");

exports.createBusinessTypes = async (req, res) => {
  try {
    const businessTypes = req.body.businessTypes;
    
    const savedBusinessTypes = await BusinessType.insertMany(businessTypes);

    res.status(201).json({
      message: 'Business types created successfully!',
      businessTypes: savedBusinessTypes
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Server error' });
  }
};

exports.getAllBusinessTypes = async (req, res) => {
  try {
    let businessTypes = await BusinessType.find();
    businessTypes = businessTypes.map(btype=>{
      return{
        _id: btype.id,
        name: btype.name,
        description: btype.description
      }
    })
    res.status(200).json(businessTypes);
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Server error' });
  }
};


exports.updateInventories = async(fromRetailerId, toRetailerId, batches)=> {
  try {
    const updatePromises = batches.map(async (batch) => {
      const { batchId, quantity, price } = batch;
      await Stock.updateOne(
        { batch: batchId, retailer: fromRetailerId },
        { $inc: { quantity: -quantity } }
      );
      await Stock.updateOne(
        { batch: batchId, retailer: toRetailerId, buyingPrice: price },
        { $inc: { quantity: quantity } },
        { upsert: true }
      );
    });
    await Promise.all(updatePromises);

  } catch (error) {
    console.error(error);
    throw new Error('Failed to update inventories');
  }
}

exports.getBatchByBatchNo = async(req,res)=>{
 try {
  const {batchNo} = req.params;
  let batch = await Batch.findOne({batchNo: batchNo}).populate('product').exec();
  if(!batch){
    return res.status(401).json({
      message: "Batch does not exists with this batch no"
    })
  }
  batch = {
    _id: batch.id,
    batchNo: batch.batchNo,
    productName: batch.product.name,
    MRP: batch.MRP,
    mfg: batch.mfgDate,
    expiry: batch.expiryDate,
    sellingPrice: 0,
    buyingPrice: 0
  }
  return res.status(200).json(batch)
 } catch (error) {
  
 }
}

exports.myProfile = async(req,res)=>{
  await updateTotalSalesOfRetailer(req.user);
  const noOfConnections = await countConnections(req.user);
  const createdOrders = await Order.countDocuments({ creator: req.user }).exec();
  const receivedOrders = await Order.countDocuments({ receiver: req.user }).exec();

  const retailer = await Retailer.findOne({_id: req.user}).populate('businessType').exec();

  return res.status(200).json({
    _id: retailer.id,
    name: retailer.name,
    businessName: retailer.businessName,
    businessType: retailer.businessType.name,
    email: retailer.email,
    address: retailer.address,
    phone: retailer.phone,
    totalSales: retailer.totalSales,
    totalConnections: noOfConnections,
    createdOrders,
    receivedOrders
  });
}

exports.getLastNdaysOrders = async(req,res)=>{
  const {startingFrom, type, noOfDays, isCreatedByUser} = req.query;
  const retailer = req.user;
  let matchObject;
  if(isCreatedByUser == true || isCreatedByUser == "true"){
    matchObject = {creator: mongoose.Types.ObjectId(retailer)}
  }
  else{
    matchObject = {receiver: mongoose.Types.ObjectId(retailer)}
  }

  let orders;
  let ordersResponse = [];
  if(type == "week"){
    let limit =6- new Date(startingFrom).getDay();
    if(limit>=1){
      for(let i = new Date(startingFrom).getDay()+1;i<=6;i++){
        ordersResponse.push({
          key: i,
          count: 0
        })
      }
    }
    for(let i = 0;i<=new Date(startingFrom).getDay();i++){
      ordersResponse.push({
        key: i,
        count: 0
      })
    }
    orders = await getCategorizedModelLastNDays("Order", noOfDays, matchObject, startingFrom,  { $dayOfWeek: '$createdAt' }, { $sum: 1 })
  }
  else if (type == "month"){
    let limit = 6 - (new Date(startingFrom).getMonth() + 1);
    if (limit>=0){
      for(let i = 12-limit+1;i<=12;i++){
        ordersResponse.push({
          key: i,
          count: 0
        })
      }
      for(let i = 1;i<=new Date(startingFrom).getMonth()+1;i++){
        ordersResponse.push({
          key: i,
          count: 0
        })
      }

    }
    else{
      for(let i = new Date(startingFrom).getMonth() + 1;i> new Date(startingFrom).getMonth()-5;i--){
        ordersResponse.push({
          key: i,
          count: 0
        })
      }
    }
    orders = await getCategorizedModelLastNDays("Order", noOfDays, matchObject, startingFrom,  { $month: '$createdAt' }, { $sum: 1 })
  }
  else if (type == "year"){
    for(let i = new Date(startingFrom).getFullYear()-2;i<=new Date(startingFrom).getFullYear();i++){
      ordersResponse.push({
        key: i,
        count: 0
      })
    }
    orders = await getCategorizedModelLastNDays("Order", noOfDays, matchObject, startingFrom,  { $year: '$createdAt' }, { $sum: 1 })
  }
  else 
    return res.status(401).json({
      "message": "type is not valid"
    })
    orders.forEach(order=>{
      for (i = 0; i < ordersResponse.length; i++) {
        if (ordersResponse[i].key === order.key) {
          ordersResponse[i] = order
        }
    }
    })
  return res.status(200).json(ordersResponse);
}

exports.getLastNdaysSales = async(req,res)=>{
  const {startingFrom, type, noOfDays, isCreatedByUser} = req.query;
  const retailer = req.user;

  let matchObject;
  if(isCreatedByUser == true || isCreatedByUser == "true"){
    matchObject = {fromRetailerId: mongoose.Types.ObjectId(retailer)}
  }
  else{
    matchObject = {toRetailerId: mongoose.Types.ObjectId(retailer)}
  }

  let sales;
  let salesResponse = [];
  if(type == "week"){
    let limit =6- new Date(startingFrom).getDay();
    if(limit>=1){
      for(let i = new Date(startingFrom).getDay()+1;i<=6;i++){
        salesResponse.push({
          key: i,
          count: 0
        })
      }
    }
    for(let i = 0;i<=new Date(startingFrom).getDay();i++){
      salesResponse.push({
        key: i,
        count: 0
      })
    }
    sales = await getCategorizedModelLastNDays("Sales", noOfDays, matchObject, startingFrom,  { $dayOfWeek: '$createdAt' }, { $sum: "$totalPrice" })
  }
  else if (type == "month"){
    let limit = 6 - (new Date(startingFrom).getMonth() + 1);
    if (limit>=0){
      for(let i = 12-limit+1;i<=12;i++){
        salesResponse.push({
          key: i,
          count: 0
        })
      }
      for(let i = 1;i<=new Date(startingFrom).getMonth()+1;i++){
        salesResponse.push({
          key: i,
          count: 0
        })
      }

    }
    else{
      for(let i = new Date(startingFrom).getMonth() + 1;i> new Date(startingFrom).getMonth()-5;i--){
        salesResponse.push({
          key: i,
          count: 0
        })
      }
    }
    sales = await getCategorizedModelLastNDays("Sales", noOfDays, matchObject, startingFrom,  { $month: '$createdAt' }, { $sum: "$totalPrice" })
  }
  else if (type == "year"){
    for(let i = new Date(startingFrom).getFullYear()-2;i<=new Date(startingFrom).getFullYear();i++){
      salesResponse.push({
        key: i,
        count: 0
      })
    }
    sales = await getCategorizedModelLastNDays("Sales", noOfDays, matchObject, startingFrom,  { $year: '$createdAt' }, { $sum: "$totalPrice"  })
  }
  else 
    return res.status(401).json({
      "message": "type is not valid"
    })
  sales.forEach(sale=>{
    for (i = 0; i < salesResponse.length; i++) {
      if (salesResponse[i].key === sale.key) {
          salesResponse[i] = sale
      }
  }
  })
  return res.status(200).json(salesResponse);
}

const getCategorizedModelLastNDays = async (modelName, n, matchObject, startDate, groupBy, aggregator) => {
  const Model = mongoose.model(modelName);

  let dateNdaysBefore = new Date(startDate); 
  startDate = new Date(startDate)// Current date and time
  dateNdaysBefore.setDate(dateNdaysBefore.getDate() - n); // Subtract n days from the current date

  try {
    const models = await Model.aggregate([
      {
        $match: {
          $and:[
         {createdAt: { $gte: dateNdaysBefore }},
         {createdAt: { $lte: startDate }},
         matchObject
        //  {creator: mongoose.Types.ObjectId(retailer)}
           // Find orders where the createdAt timestamp is greater than or equal to the startDate
          ]

        }
      },
      {
        $group: {
          _id: groupBy,
          count: aggregator
          // _id: { $dayOfWeek: '$createdAt' }, // Group orders by day of the week
          // count: { $sum: 1 } // Count the number of orders for each day
        }
      },
      {
        $sort: { _id: 1 } // Sort the result by day of the week in ascending order
      }
    ]).exec();

    return models.map((model)=>{
      return {
        key: model._id,
        count: model.count
      }
    })
  } catch (error) {
    console.error('Error retrieving orders:', error);
    throw error;
  }
};

const updateTotalSalesOfRetailer = async (retailer) => {
  try {
    const result = await Sales.aggregate([
      {
        $match: { fromRetailerId: mongoose.Types.ObjectId(retailer) } // Filter sales by the retailerId
      },
      {
        $group: {
          _id: null,
          totalPrice: { $sum: '$totalPrice' } // Sum the 'totalPrice' field for all matching sales
        }
      }
    ]).exec();
    await Retailer.findOneAndUpdate({_id: retailer}, {totalSales: result[0]?.totalPrice});
  } catch (error) {
    console.error('Error retrieving total price:', error);
    throw error;
  }
};

const countConnections = async (retailer)=>{
  try {
    let result = await Connection.find({
      $or: [{ requester: retailer }, { recipient: retailer }],
    }).where({ status: 'accepted' })
    console.log(result);
    return result.length;
  } catch (error) {
    console.error('Error retrieving total price:', error);
    throw error;
  }
}
