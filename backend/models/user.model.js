const mongoose = require('mongoose');

const Schema = mongoose.Schema;

const userSchema = new Schema({
    name: {type: String},
    roll: {type: String},
    email: {type: String},
    password: {type: String},
    auth: {type: String}
    
});

const UserData = mongoose.model('UserData',userSchema);

module.exports = UserData;