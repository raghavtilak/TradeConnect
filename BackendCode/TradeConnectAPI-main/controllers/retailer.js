const { mailer } = require("../mailer");
const Batch = require("../models/Batch");
const Stock = require("../models/Stock");
const Order = require("../models/Order");
const Product = require("../models/Product");
const Retailer = require("../models/Retailer");
const Sell = require('../models/Sell');


const { SKUGenerator } = require("../utils");
const { createBatch } = require("./product");

const PDFDocument = require("pdfkit-table");
const { updateInventories } = require("./common");
const User = require("../models/User");
const { sendPushNotifications } = require("../firebaseNotifications");

exports.getUser = async (req, res) => {
  try {
    let { phone, email } = req.query
    if (!phone && !email) {
      res.status(400).json({ "error": "please provide either phone or email" })
    }
    let user = null;
    if (phone) {
      user = await User.findOne({ phone: phone }).exec();
    }
    if (email) {
      user = await User.findOne({ email: email }).exec();
    }
    console.log(phone);
    console.log(user);
    if (!user)
      return res.status(400).json({ "error": "User not found" });
    else
      return res.status(200).json(user);
  } catch (error) {
    console.log(error);
  }
}

exports.updateRegistrationToken = async (req, res) => {
  try {
    const { registrationToken } = req.body;
//     console.log("token"+req.user);
// console.log("rtoken"+registrationToken);
    // let user = await Retailer.findOneAndUpdate({ _id: req.user }, { registrationToken: registrationToken });
let user = await User.findOne({_id: req.user});
console.log(user);
user.registrationToken = registrationToken;
await user.save();
console.log(user);
    if (!user) {
      return res.status(401).json({
        "message": "Some Error Occurred"
      })
    }
    return res.status(200).json({});
  } catch (error) {
    console.log(error);
  }
}

exports.signup = async (req, res) => {
  try {
    const { name, email, phone, address, password, businessName, businessType, registrationToken } = req.body;
    if (!phone || !name || !email || !address || !businessName || !registrationToken) {
      return res.status(400).json({
        error: "All fields are required",
        success: false
      })
    }
    const user = await Retailer.findOne({ email: email }).exec();
    if (user) {
      return res.status(400).json({
        error: "Account Already Exists Please Sign in"
      })
    }
    else {
      createdUser = await Retailer.create({
        name, email, phone, address, password, businessName, businessType, registrationToken
      })

      mailOptions = {
        from: process.env.MAIL_USER,
        to: email,
        subject: 'Account Created',
        html: `<h1>Welcome ${name} to our App!!!</h1>
                       <p>You can use following features to start your journey on the app</p>
                       <ul>
                       <li>My store: here you can add your stock details view batches Information</li>
                       <li>My Orders: here you can manage orders created by you and orders created for you</li>
                       <li>My Sells: here you can manage your sells</li>
                       <li>My Connections: here you can manage all of your connections to whom you do business</li>
                       </ul>
                       <p>Regards,</p>
                       <p>Team TradeConnect app</p>
                       `
      };
      mailer(mailOptions)
      res.status(200).json(createdUser.getJwtToken());
    }

  } catch (error) {
    console.log(error);
  }
}

exports.signin = async (req, res) => {
  try {
    let { phone, email } = req.body
    if (!phone && !email) {
      res.status(400).json({ "error": "please provide either phone or email" })
    }
    let user = null;
    if (phone) {
      user = await User.findOne({ phone: phone }).exec();
    }
    if (email) {
      user = await User.findOne({ email: email }).exec();
    }
    if (!user) {
      return res.status(400).json({ "error": "No User found" });
    }
    return res.status(200).json(user.getJwtToken());

  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Server error' });
  }
};

exports.addStockInRetailerInventory = async (req, res) => {
  try {
    let { productName, batchNo, buyingPrice, sellingPrice, quantity } = req.body;
    const retailerId = req.user;

    if (!batchNo || !buyingPrice || !sellingPrice || !quantity) {
      return res
        .status(400)
        .json({ success: false, error: 'Please provide all required fields.' });
    }
    if (isNaN(buyingPrice) || isNaN(sellingPrice) || isNaN(quantity)) {
      return res
        .status(400)
        .json({ success: false, error: 'Please provide valid numbers for buyingPrice, sellingPrice, and quantity.' });
    }

    let batch = await Batch.findOne({ batchNo: batchNo }).populate('product').exec();

    if (!batch) {
      batch = await createBatch(req, res);
    }
    else if (batch.product.name.replace(/\s/g, '').toLowerCase() != productName.replace(/\s/g, '').toLowerCase()) {
      return res.status(409).json({ "error": "Batch no exists with another product" })
    }

    let stock = await Stock.findOne({
      batch: batch._id,
      retailer: retailerId,
    }).exec();

    if (!stock) {
      stock = await Stock.create({
        retailer: retailerId,
        batch: batch._id,
        buyingPrice: buyingPrice,
        sellingPrice: sellingPrice,
        quantity: quantity,
      });
    } else {
      // inventory.batchId = batch._id;
      if (stock.buyingPrice == buyingPrice && stock.sellingPrice == sellingPrice) {
        quantity = typeof quantity === "string"? parseInt(quantity): quantity
        stock.quantity = typeof stock.quantity === "string"? parseInt(stock.quantity): stock.quantity
        stock.quantity = quantity + stock.quantity;
        await stock.save();
      }
      else {
        return res.status(405).json({
          error: "Same stock exists in your inventory with different buying and selling prices..."
        })
      }
    }

    return res.status(200).json({
      message: "Stock added successfully"
    });
  } catch (error) {
    console.log(error);
    res.status(500).json({ error: "Some error occurred" });
  }
};


exports.addStocksToRetailerInventory = async (req, res) => {
  try {
    const { batches } = req.body;
    const retailerId = req.user;
    // if(!batches.length > 0){
    //   return res.status(400).json({
    //     error: "Some error occurred"
    //   })
    // }
    // Loop through each batch in the request body
    for (const batch of batches) {
      // Validate the batch data
      const { productName, batchNo, productDescription, MRP, mfg, expiry, buyingPrice, sellingPrice, quantity } = batch;
      if (!productName || !batchNo || !MRP || !mfg || !expiry || !buyingPrice || !sellingPrice || !quantity) {
        throw new Error('Invalid batch data');
      }

      // Check if the product already exists


      // Check if the batch already exists for this product
      let existingBatch = await Batch.findOne({
        batchNo: batchNo
      });
      if (!existingBatch) {

        let product = await Product.findOne({ name: productName });
        if (!product) {
          // If not, create a new product
          product = new Product({
            name: productName,
            description: productDescription,
            sku: SKUGenerator(productName),
            createdBy: retailerId
          });
          await product.save();
        }

        // If not, create a new batch
        existingBatch = new Batch({
          product: product._id,
          batchNo: batchNo,
          MRP: MRP,
          mfgDate: mfg,
          expiryDate: expiry,
          createdBy: retailerId
        });
        await existingBatch.save();
      }
      // Check if the inventory already exists for this batch and retailer
      let stock = await Stock.findOne({ batch: existingBatch._id, retailer: retailerId });
      console.log(stock);
      if (!stock) {
        // If not, create a new inventory record
        stock = new Stock({
          retailer: retailerId,
          batch: existingBatch._id,
          buyingPrice: buyingPrice,
          sellingPrice: sellingPrice,
          quantity: quantity
        });
        console.log(stock);
        await stock.save();
      } else {
        if (stock.buyingPrice == buyingPrice && stock.sellingPrice == sellingPrice) {
          stock.quantity = quantity + stock.quantity;
          await stock.save();
        }
      }
    }

    res.status(200).json({ message: 'Batches added to inventory successfully' });
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: 'Failed to add batches to inventory' });
  }
};


exports.myInventory = async (req, res) => {
  try {
    const retailerId = req.user;

    // Find all inventories for the retailer
    const stocks = await Stock.find({ retailer: retailerId }).populate({
      path: 'batch',
      populate: {
        path: 'product',
        model: 'Product'
      }
    }).exec();

    // Extract product information from each inventory
    console.log("STOCK"+stocks);
    const products = [];
    for (const stock of stocks) {
      console.log(stock);
      const { batch, quantity } = stock;
      const { name: productName, sku } = batch.product;
      const { batchNo } = batch;

      // Check if product already exists in the products list
      const productIndex = products.findIndex(product => product.productName == productName);
      if (productIndex === -1) {
        // If product doesn't exist, add it to the list
        products.push({ batchIds: [batch._id], sku, productName, quantity });
      } else {
        // If product exists, update its quantity and MRP
        products[productIndex].quantity += quantity;

        if (!products[productIndex].batchIds.includes(batch._id)) {
          products[productIndex].batchIds.push(batch._id);
        }
      }
    }

    // Return the list of unique products
    console.log(products);
    return res.status(200).json(products);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ message: 'Internal server error' });
  }
};

exports.getBatchesByIds = async (req, res) => {
  try {
    const retailerId = req.user;
    console.log(retailerId);
    const batchIds = req.query.ids // Get the batch IDs from the request parameters
    let batches = await Stock.find({ batch: batchIds, retailer: retailerId }).populate({
      path: 'batch',
      populate: {
        path: 'product',
        model: 'Product'
      }
    }).exec();
    batchesRes = new Map();
    batches.forEach(obj => {
      batchesRes.set(obj.batch.id, {
        _id: obj.batch.id,
        batchNo: obj.batch.batchNo,
        MRP: obj.batch.MRP,
        mfg: obj.batch.mfgDate,
        expiry: obj.batch.expiryDate,
        productName: obj.batch.product.name,
        quantity: obj.quantity,
        buyingPrice: obj.buyingPrice,
        sellingPrice: obj.sellingPrice,
        isUpdateAllowed: obj.createdBy == retailerId
      })
    })
    console.log([...batchesRes.values()]);
    res.status(200).json([...batchesRes.values()]);
  } catch (error) {
    console.error(error);
    res.status(500).json({
      success: false,
      error: 'Server Error'
    });
  }
};


exports.updateBatch = async (req, res) => {
  try {
    // TODO: Only the quantity can be updated if user is not creator
    const { batchId } = req.query;
    const retailer = req.user;
    const { MRP, mfg, expiry, quantity,buyingPrice, sellingPrice} = req.body;

    const obj = await Stock.findOne({ batch: batchId, retailer: retailer}).populate('batch')
    console.log(obj);
    if (!obj) {
      return res.status(404).json({ error: 'Batch not found' });
    }
    if (obj.batch.createdBy == req.user) {
      obj.batch.MRP = MRP
      obj.batch.mfgDate = mfg
      obj.batch.expiryDate = expiry
    }
    obj.quantity = quantity
    obj.buyingPrice = buyingPrice
    obj.sellingPrice = sellingPrice
    await obj.save();
    await obj.batch.save();
    return res.status(200).json({message: "Batch updated successfully"});
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: 'Server error' });
  }
};

exports.createOrder = async (req, res) => {
  try {
    const { receiverId, batches, totalAmount } = req.body;
console.log(totalAmount);
    if (!receiverId) {
      return res.status(400).json({ error: 'Receiver ID is required.' });
    }

    if (!batches || !Array.isArray(batches) || batches.length === 0) {
      return res.status(400).json({ error: 'At least one product is required.' });
    }

    for (const batch of batches) {
      if (!batch.batchNo) {
        return res.status(400).json({ error: 'Batch number is required for all products.' });
      }
      else {
        batch.batchId = await Batch.findOne({ batchNo: batch.batchNo }).select('id');
        batch.batchId = batch.batchId._id
      }

      if (!batch.quantity || batch.quantity <= 0) {
        return res.status(400).json({ error: 'Quantity is required and should be greater than 0.' });
      }
    }


    const order = await Order.create({
      creator: req.user,
      receiver: receiverId,
      totalAmount: totalAmount,
      batches: batches,
    })

    const orderDetails = await Order.findById(order._id).populate('receiver').populate('batches.batchId').populate({
      path: 'batches.batchId',
      populate: {
        path: 'product',
        model: 'Product'
      }
    })
      .exec();

    console.log(orderDetails.batches);

    const creatorDetails = await Retailer.findById(req.user);
    const receiverDetails = await Retailer.findById(receiverId);
    // Generate a PDF of the order data
    const pdfBuffer = await createOrderPDF(orderDetails);

    mailOptions = {
      from: process.env.MAIL_USER,
      to: receiverDetails.email,
      //TODO: add order Receiver's mail
      subject: `Order received for your business ${receiverDetails.businessName}`,
      text: `You have received an order from ${creatorDetails.businessName}, Please see the attached PDF for details of the order.`,
      attachments: [{
        filename: `Order_${order._id}.pdf`,
        content: pdfBuffer
      }]
    };

    mailer(mailOptions)
    // console.log(orderDetails.products[0].batchId.product);
    let notificationPayload = {
      notification: {
          title: "Order received",
          body: `You have received a order from ${creatorDetails.name}, please check mail for more details.`
      }
    };
    registrationToken = []
    registrationToken.push(receiverDetails.registrationToken);
    sendPushNotifications(notificationPayload,registrationToken)


    return res.status(201).json({
      message: "Order created successfully"
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: 'Something went wrong.' });
  }
};

exports.updateOrder = async (req, res) => {
  try {
    const order = await Order.findById(req.params.id).populate('creator receiver').populate('batches.batchId');

    if (!order) {
      return res.status(404).json({ error: 'Order not found' });
    }
    console.log(order);
    // Update order properties
    let notificationPayload;
    if (req.body.status) {
      order.status = req.body.status;
      if(req.body.status == 'declined'){
        notificationPayload = {
          notification: {
              title: "Order Declined",
              body: `Your order from ${order.creator.businessName} has been declined`
          }
        };
      }
    }
    if (req.body.status == 'inactive') {
      let flag = false;
      order.batches.forEach(async batch=>{
        console.log(batch);
        let stock = await Stock.findOne({batch: batch.batchId, retailer: req.user});
        stock.quantity = typeof stock.quantity === "string" ? parseInt(stock.quantity): stock.quantity
        if(stock.quantity < batch.quantity){
          flag = true;
        }
      })
      if (flag) {
        return res.status(400).json({
          error:"You don't have enough stock to accept this order"
        })
      }
      notificationPayload = {
        notification: {
            title: "Order Accepted",
            body: `Your order from ${order.receiver.businessName} has been accepted, please check mail for details`
        }
      };

      const sale = await Sell.create({
        fromRetailerId: order.receiver,
        toRetailerId: order.creator,
        batches: order.batches,
        totalPrice: order.totalAmount,
        paid: 0,
        due: order.totalAmount,
        // TODO: Update if we add an option to pay when order created
      });

      await updateInventories(order.receiver, order.creator, sale.batches)

      let mailOptions = {
        from: process.env.MAIL_USER,
        to: order.creator.email,
        //TODO: add order creator's mail
        subject: `Your Order has accepted`,
        text: `Your Order(OrderId: ${order.id}) from ${order.receiver.businessName} has accepted. Please update your latest products in your inventory`,
      };
      mailer(mailOptions)
    }
    
    // Save updated order to database
    const updatedOrder = await order.save();

    registrationToken = []
    registrationToken.push(order.creator.registrationToken);
    console.log(registrationToken);
    sendPushNotifications(notificationPayload,registrationToken)

    return res.status(201).json({ message: 'Order updated successfully' });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Server error' });
  }
};

async function createOrderPDF(orderData) {

  const doc = new PDFDocument();
  doc.font('Helvetica-Bold');
  doc.fontSize(16);
  doc.text('Order Details', { align: 'center' });

  doc.moveDown();
  doc.font('Helvetica');
  doc.fontSize(12);

  const tableData = {
    headers: ['Product', 'Batch No', 'Quantity Required'],
    rows: []
  };

  orderData.batches.forEach(product => {
    tableData.rows.push([product.batchId.product.name, product.batchNo, product.quantity]);
  });

  const table = {
    headers: tableData.headers,
    rows: tableData.rows,
    x: 50,
    y: 150,
    width: 500,
    columnWidths: {
      0: 200,
      1: 150,
      2: 150
    },
    headerAlignment: 'left',
    rowAlignment: 'left'
  }
  await doc.table(table, {
    columnsSize: [200, 100, 100],
  });
  return new Promise((resolve, reject) => {
    const chunks = [];
    doc.on('data', (chunk) => chunks.push(chunk));
    doc.on('end', () => resolve(Buffer.concat(chunks)));
    doc.on('error', reject);
    doc.end();
  });
}

exports.myOrders = async (req, res) => {
  try {
    const { status } = req.query;
    // let orderQuery = {creator : req.user}
    // if(isCreatedByUser != null && (isCreatedByUser == 'false' || isCreatedByUser == false)){
    //  orderQuery = {receiver : req.user}
    //  console.log(orderQuery);
    // }
    const orders = await Order.find({
      $or: [{ creator: req.user }, { receiver: req.user }],
    }).where({ status: status })
      .populate('creator', 'name businessName email')
      .populate('receiver', 'name businessName email')
      .populate({
        path: 'batches.batchId',
        populate: {
          path: 'product',
          model: 'Product'
        },
      })
      .sort({ createdAt: 'desc' })
      .exec();

    let orderDetails = [];
    orders.forEach(order => {
      var detail = {
        _id: order.id,
        status: order.status,
        createdAt: order.createdAt,
        total: order.totalAmount,
        batches: []
      }
      console.log(req.user);
      console.log(order.creator);
      detail.isCreatedByUser = order.creator.id == req.user;
      detail.user = order.creator.id == req.user ? order.receiver : order.creator
      order.batches.forEach(prod => {
        detail.batches.push({
          batchNo: prod.batchNo,
          quantity: prod.quantity,
          productName: prod.batchId?.product.name,
          MRP: prod.batchId?.MRP,
          sellingPrice: prod.price
        })
      })
      orderDetails.push(detail);
    })

    return res.status(200).json(orderDetails);
  } catch (error) {
    console.error(error);
    return res.status(500).json({ error: 'Something went wrong.' });
  }
};






