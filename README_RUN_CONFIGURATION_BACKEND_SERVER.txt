**** STEPS TO RUN NODE SERVER ****

STEP 1: Install latest version of node.
STEP 2: Copy the TradeConnectBackend folder to your system.
STEP 3: Open VScode or any IDE, go to the location where you copie the folder, open terminal and run npm install.
STEP 4: Once the packages are installed create a .env file if you are running in your local machine and set environment variables of server if you are deploying it on cloud.
STEP 5: Set following environment variables using in backend JWT_SECRET=college_project JWT_EXPIRY=30d DB_URL=mongodb://127.0.0.1:27017/Khatabook MAIL_USER=mukul805809@gmail.com MAIL_PASSWORD=your_password.
STEP 6: Open a terminal in your IDE and run npm start if you are running on local and run server on deployment cloud if you are using cloud.
