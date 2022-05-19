const router = require('express').Router();
let ComplaintData = require('../models/complaint.model');

router.route('/add').get((req,res) => {
    const mess = req.query.mess
    ComplaintData.find({mess: "B", resolved: false})
        .then(complaintData => res.json(complaintData))
        .catch(err => res.status(400).json('Error: '+ err));
});


router.route('/update').post((req,res) => {
    const id =req.body.id;
    var myquery = { _id: id};
    var newvalues = {$set: {resolved: true}};

    ComplaintData.updateOne(myquery,newvalues,function(e, res1) {
        if (e) throw e;
        if(res1){
        res.send({message:"1"})
        }
    });
    
});

module.exports = router;