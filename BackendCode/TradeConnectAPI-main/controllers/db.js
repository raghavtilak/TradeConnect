const mongoose =require('mongoose');

const Connection=()=>{
    mongoose.connect(process.env.DB_URL,{
        useNewUrlParser:true,
        useUnifiedTopology:true
    }).then(console.log('DB Connected'))
    .catch(err=>{console.log(err)
    process.exit(1)})
}

module.exports=Connection;