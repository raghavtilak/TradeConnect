require('dotenv').config();
const CookieParser = require('cookie-parser');
const express = require('express');
const cors = require('cors')
const Connection = require('./controllers/db');
const app = express();
const customerRoute = require('./routes/customerRoute');
const retailerRoute = require('./routes/retailerRoute');
const alertRoute = require('./routes/alertRoute')
const adminRoute = require('./routes/adminRoute');
const User = require('./models/User');
const { startExpiryAlerts } = require('./schedulers/expiryAlert');
const { startPaymentAlerts } = require('./schedulers/paymentAlert');
const { sendPushNotifications } = require('./firebaseNotifications');
var admin = require('firebase-admin');
var serviceAccount = require('./firebaseCred.json');
admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
});
app.use(express.json());
app.use(express.urlencoded({extended:true}))
app.use(CookieParser());
app.use(cors())
// app.use('/api/v1',alertRoute)
app.use('/api/v1',customerRoute);
app.use('/api/v1',adminRoute)
app.use('/api/v1',retailerRoute);
Connection();


startExpiryAlerts();
startPaymentAlerts();
app.listen(5000, () => {
    console.log("server is running at port 5000");
})

