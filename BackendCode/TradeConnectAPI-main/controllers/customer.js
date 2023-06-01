const { mailer } = require("../mailer");
const Customer = require("../models/Customer");
const Sell = require("../models/Sell");


exports.signup = async (req, res) => {
    try {
        const {name, email, phone, address, registrationToken } = req.body;
        if (!phone || !name || !email || !address || !registrationToken ) {
            return res.status(400).json({
                error: "All fields are required"
            })
        }
        const user = await Customer.findOne({email: email}).exec();
        if (user) {
            return res.status(400).json({
                error: "Account Already Exists Please Sign in"
            })
        }
        else {
              createdUser = await Customer.create({
                name, email, phone, address, role: 'Customer', registrationToken
              })

            mailOptions = {
                from: process.env.MAIL_USER,
                to: email,
                subject: 'Account Created',
                html: `<h1>Welcome ${name} to our App!!!</h1>
                       <p>You can view transactions made at the shops you have visited through the app created for you.</p>
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

exports.getMyRetailers = async (req,res)=>{
  try {
    const {customerEmail} = req.query;
    let fromRetailers = await Sell.find({ customerEmail })
      .populate({
        path: 'fromRetailerId',
        populate: {
          path: 'businessType',
          model: 'BusinessType',
          select: 'name',
        },
      }).exec();
      let response = new Map();
      
      fromRetailers.forEach(ret=>{
        response.set(ret.fromRetailerId.id,{
          _id: ret.fromRetailerId.id,
          name: ret.fromRetailerId.name,
          businessName: ret.fromRetailerId.businessName,
          businessType: ret.fromRetailerId.businessType.name,
          email: ret.fromRetailerId.email,
          address: ret.fromRetailerId.address,
          phone: ret.fromRetailerId.phone
        })
      })

    return res.status(201).json([...response.values()])
  } catch (error) {
    // Handle error
    console.error('Error finding fromRetailers:', error);
    throw error;
  }
}

exports.customerTransactions = async (req, res)=>{
    try {
      let customerId = req.user;
    customer = await Customer.findOne({_id: customerId});
    if(!customer || customer.role != 'Customer'){
        return res.status(401).json({
            "error": "You are not a customer"
        })
    }
    let transactions = await Sell.find({
        customerEmail: customer.email
      })
      .populate('fromRetailerId', 'name businessName email phone address')
      .populate({
        path: 'fromRetailerId',
        populate: {
          path: 'businessType',
          model: 'BusinessType',
          select: 'name' // specify the fields you want to include/exclude from the businessType document
        }
      })
      .populate({
        path: 'batches.batchId',
        populate: {
          path: 'product',
          model: 'Product',
          select: '-createdAt -updatedAt -__v' // exclude unwanted fields from the product document
        }
      })
      .sort({ date: -1 })
      .select('-__v')
      .exec();

      const transactionsJson = [];
      transactions.forEach(sale => {
        let obj = {
          _id: sale._id,
          fromRetailer: {
            name: sale.fromRetailerId.name,
            businessType: sale.fromRetailerId.businessType.name,
            businessName: sale.fromRetailerId.businessName,
            phone: sale.fromRetailerId.phone,
            email: sale.fromRetailerId.email,
            address: sale.fromRetailerId.address,
            totalSales: 0
            //Hack to save android from crash
          },
          totalPrice: sale.totalPrice,
          paid: sale.paid,
          due: sale.due,
          date: sale.createdAt || sale.date,
          batches: []
        }
        sale.batches.forEach(batch=>{
          let obj2 = {
            batchNo: batch.batchNo,
            quantity: batch.quantity,
            sellingPrice: batch.price,
            MRP: batch.batchId.MRP,
            productName: batch.batchId.product.name
          }
          obj.batches.push(obj2);
        })
        transactionsJson.push(obj)
      })
      console.log(transactionsJson);
      return res.status(200).json(transactionsJson)
    } catch (error) {
      console.log(error);
    }

}
