const mongoose = require('mongoose');

const Schema = mongoose.Schema;

const requestSchema = new Schema({
    key: {type: String},
    value: {type: Array},
});

const Request = mongoose.model('Request',requestSchema);

module.exports = Request;