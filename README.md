
# TradeConnect 

![xQhhE8f01UZN_1024_500](https://github.com/raghavtilak/TradeConnect/assets/74963954/3f8c7103-9ac4-4cd4-ac82-ee553dae9df9)

## About

TradeConnect enables Retailers to manage the supply chain seamlessly through their mobile phones. it also enables them to manage their store, have a record of their sells, orders and connections. And guess what everything synced on cloud in realtime.

[Download APK](https://github.com/raghavtilak/TradeConnect/blob/master/app/release/app-release.apk)

## Documentation

- [Abstract](https://github.com/raghavtilak/TradeConnect/blob/main/Project%20Documentation/AbstractTradeConnect.pdf)
- [SRS](https://github.com/raghavtilak/TradeConnect/blob/main/Project%20Documentation/SRSTradeConnect.pdf)
- [Pproject Report PDF](https://github.com/raghavtilak/TradeConnect/blob/main/Project%20Documentation/ProjectReportTradeConnect.pdf)
- [Pproject Report Latex](https://github.com/raghavtilak/TradeConnect/blob/main/Project%20Documentation/ProjectReportTradeConnect.zip)
- [Form1](https://github.com/raghavtilak/TradeConnect/blob/main/Project%20Documentation/Form1.pdf)
- [Form2](https://github.com/raghavtilak/TradeConnect/blob/main/Project%20Documentation/Form2.pdf)
- [Form3](https://github.com/raghavtilak/TradeConnect/blob/main/Project%20Documentation/Form3.pdf)

## Demo

| SignUp |  SignIn |
|---     |---      |
|![signup](https://github.com/raghavtilak/TradeConnect/assets/74963954/b175c152-621f-4edf-ada5-ac216f4cef81)|![signin](https://github.com/raghavtilak/TradeConnect/assets/74963954/bc740353-dc12-4c47-80cc-682057b12356)|

| Create Account |  Home Screen |
|---     |---      |
|![createretailer](https://github.com/raghavtilak/TradeConnect/assets/74963954/70c580e5-d502-49a6-ad57-696539c8fa13) | ![homescreen](https://github.com/raghavtilak/TradeConnect/assets/74963954/e09f67e6-812f-4771-a058-bc375292d1e4) |


|My Orders | My Connections|
|---     |---      |
|![myorders](https://github.com/raghavtilak/TradeConnect/assets/74963954/12e272e1-3e06-452c-a380-a9e199308df9)|![myconnections](https://github.com/raghavtilak/TradeConnect/assets/74963954/ae7f5866-ebd6-45bf-9a85-f7984f84791b)|


|My Sells | View Store |
|---     |---      |
|![mysellbatchdetail](https://github.com/raghavtilak/TradeConnect/assets/74963954/1e3836b4-2eef-4900-b80b-0ee48b5dbfe4)|![mystore](https://github.com/raghavtilak/TradeConnect/assets/74963954/2f84f07b-a874-4f89-920c-833a5742042f)|


|My Invitations | Pending Orders |
|---     |---      |
|![myinvitations](https://github.com/raghavtilak/TradeConnect/assets/74963954/866bd3e0-0432-4d07-ae45-9dd7a58a0599)|![pendingorders](https://github.com/raghavtilak/TradeConnect/assets/74963954/5ee0b434-6d09-4d4d-9fdb-80beaeac905f)|


|Profile View | Analytics |
|---     |---                  |
|![myprofile](https://github.com/raghavtilak/TradeConnect/assets/74963954/e9ed1de9-9d2d-44d5-b806-8c1a613c4fc0)|![analyticsorders](https://github.com/raghavtilak/TradeConnect/assets/74963954/aa52cab9-5a95-462e-b06b-88a701dd193c)|

# Run Configurations
## Frontend
### THROUGH APK

- STEP 1: Copy the apk file from path app/release/app-release.apk to your android device(>=8.0)
- STEP 2: Install the apk file in your device.

### THROUGH CODE

- STEP 1: Install latest version of Android Studio.
- STEP 2: Copy the TradeConnectApp folder to your system.
- STEP 3: Open AndroidStudio, go to the location where you copie the folder, and open it as AndroidProject.
- STEP 4: Once the initial build is done, connect your phone to the system through USB cable.
- STEP 5: Allow USB debugging, in your phone.
- STEP 6: Click on the Run button in the AndroidStudio, selecting your device.


## Backend
- STEP 1: Install latest version of node.
- STEP 2: Copy the TradeConnectBackend folder to your system.
- STEP 3: Open VScode or any IDE, go to the location where you copie the folder, open terminal and run npm install.
- STEP 4: Once the packages are installed create a .env file if you are running in your local machine and set environment variables of server if you are deploying it on cloud.
- STEP 5: Set following environment variables using in backend JWT_SECRET=college_project JWT_EXPIRY=30d DB_URL=mongodb://127.0.0.1:27017/Khatabook MAIL_USER=mukul805809@gmail.com MAIL_PASSWORD=your_password.
- STEP 6: Open a terminal in your IDE and run npm start if you are running on local and run server on deployment cloud if you are using cloud.


# Team Details
## Member 1
- Name: Raghav Sharma
- Roll: 19ESKIT073
- Branch: IT-B (G1)
- Role: Mobile App Developer (Android,Kotlin) 

## Member 2
- Name: Mukul Jangid
- Roll: 19ESKIT058
- Branch: IT-B (G1)
- Role: Backend Developer (NodeJS,Express,MongoDB) 
