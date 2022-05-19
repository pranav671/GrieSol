const mongoose = require('mongoose');

const Schema = mongoose.Schema;

const complaintSchema = new Schema({
    resolved_student: {type: Boolean},
    resolved_department: {type: Boolean},
    status: {type: String},
    roll: {type: String},
    date: {type: String},
    department: {type: String},
    venue: {type: String},
    asset: {type: String},
    detail: {type: String},
    image: {type: String},
    votes: {type: Number},
    votelist: {type: Array}
});

const Complaint = mongoose.model('Complaint',complaintSchema);

module.exports = Complaint;