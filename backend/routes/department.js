const router = require('express').Router();
let Department = require('../models/department.model');

router.route('/add').post((req,res) => {
    const name = req.body.name;
    const venues = req.body.venues;
    const assets = req.body.assets;

    const newDepartment = new Department({
        name,
        venues,
        assets
    });

    newDepartment.save()
        .then(() => res.send({message: "1"}))
        .catch(err => res.status(400).json('Error: '+ err));
});

  

router.route('/update').post((req,res) => {
    const name = req.body.name;
    const venues = req.body.venues;
    const assets = req.body.assets;

    var myquery = { _id: req.body._id };
    var newvalues = {$set: {name: name, venues: venues, assets: assets}}
    Department.updateOne(myquery,newvalues,function(err, res1) {
        if(res1){
          res.send({message:"1"})
        }
      })
});

router.route('/delete').post((req,res) => {
  Department.remove({_id: req.body._id},function(err){
      if (!err){
        res.send({message:"1"})
      }
  });
});


router.route('/get').get((req,res) => {
  const query = req.query

  Department.find(query)
      .then(departmentData => res.json(departmentData))
      .catch(err => res.status(400).json('Error: '+ err));
});


module.exports = router;