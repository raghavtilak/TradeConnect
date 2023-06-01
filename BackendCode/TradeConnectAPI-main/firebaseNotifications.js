var admin = require('firebase-admin');
var serviceAccount = require('./firebaseCred.json');

exports.sendPushNotifications = (payload, registrationToken)=>{
    var options = {
        priority: "high", 
        timeToLive: 60 * 60
    }
    //main function which sends messages.   
    admin.messaging().sendToDevice(registrationToken, payload, options)
        .then(function (response) {
            console.log("successfully sent message : ", response)
        }).catch( (error)=> {
            console.log(error);
            console.log("didn't work");
    });
}

exports.sendMultiplePushNotifications = (payload, registrationTokens)=>{
    console.log(payload);
    const message = {
        notification: payload,
        tokens: registrationTokens,
      };
      
      admin.messaging().sendMulticast(message)
        .then((response) => {
          if (response.failureCount > 0) {
            const failedTokens = [];
            response.responses.forEach((resp, idx) => {
              if (!resp.success) {
                failedTokens.push(registrationTokens[idx]);
              }
            });
            console.log('List of tokens that caused failures: ' + failedTokens);
          }
          else{
            console.log("Payment notifications pushed!!");
          }
        });
}