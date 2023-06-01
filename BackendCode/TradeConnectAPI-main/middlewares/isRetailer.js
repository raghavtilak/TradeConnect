const User = require('../models/User');
exports.isRetailer = async (req,res,next)=>{
    const user = await User.findOne({phone: req.body.phone || req.query.phone});
    console.log(user);
    if(user && (user.role == "Retailer" || user.role == "retailer")){
        req.user = user;
        next();
    }
    else{
        return res.status(401).json({
            error:"You are not authorized as a Retailer",
            success:false
        })
    }
}