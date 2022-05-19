const router = require('express').Router();
let Complaint = require('../models/complaint.model');

function getCurrentDate(){
    const separator = "-";
    let newDate = new Date();
    let date = newDate.getDate();
    let month = newDate.getMonth() + 1;
    let year = newDate.getFullYear();

    return `${year}${separator}${month<10?`0${month}`:`${month}`}${separator}${date}`
    }
router.route('/add').post((req,res) => {
    const roll = req.body.roll;
    const detail = req.body.detail;
    const department = req.body.department;
    const venue = req.body.venue;
    const asset = req.body.asset;
    const image = req.body.image;

    const newComplaint = new Complaint({
        resolved_student: false,
        resolved_department: false,
        roll,
        department,
        asset,
        venue,
        detail,
        image,
        status: "Not Started",
        date: getCurrentDate(),
        votes: 1,
        votelist: [roll]
    });

    newComplaint.save((err,result) => {
        if (err){
            console.log(err);
        }
        else{
            res.send({message:"1"});
        }
    });
});


router.route('/update').post((req,res) => {
    const id = req.body._id;
    const resolved_student = req.body.resolved_student;
    const resolved_department = req.body.resolved_department;

    console.log(req.body)
    var myquery = { _id: id };
    var newvalues = {$set: req.body}
    Complaint.updateOne(myquery,newvalues,function(err, res1) {
        if(res1){
          res.send({message:"1"})
        }
      })
});

router.route('/delete').post((req,res) => {
   
    Complaint.remove({_id: req.body._id},function(err){
        if (!err){
          res.send({message:"1"})
        }
    });
});


router.route('/get').get((req,res) => {
    const query = req.query
    const options = { sort: [['group.name', 'asc' ]] };


    Complaint.find(query)
        .sort({votes:-1})
        .then(complaintData => res.json(complaintData))
        .catch(err => res.status(400).json('Error: '+ err));
});


module.exports = router;