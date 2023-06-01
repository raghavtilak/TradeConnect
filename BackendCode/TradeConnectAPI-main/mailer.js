const nodemailer = require('nodemailer');

exports.mailer = (mailOptions)=>{
    // create reusable transporter object using the default SMTP transport
let transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
      user: process.env.MAIL_USER, // your email address
      pass: process.env.MAIL_PASSWORD // your email password or application-specific password
    }
  });
  
  // send mail with defined transport object
  transporter.sendMail(mailOptions, (error, info) => {
    if (error) {
      console.log(error);
    } else {
      console.log('Email sent: ' + info.response);
    }
  });
}
