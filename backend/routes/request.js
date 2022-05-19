const router = require('express').Router();
let Request = require('../models/request.model');

router.route('/add').post((req,res) => {
    const key = req.body.key;
    const value = req.body.value;

    const newRequest = new Request({
        key,
        value
    });

    newRequest.save()
        .then(() => res.send({message: "1"}))
        .catch(err => res.status(400).json('Error: '+ err));
});


router.route('/delete').post((req,res) => {
  Request.remove({_id: req.body._id},function(err){
      if (!err){
        res.send({message:"1"})
      }
  });
});


router.route('/get').get((req,res) => {
  const query = req.query

  Request.find(query)
      .then(requestData => res.json(requestData))
      .catch(err => res.status(400).json('Error: '+ err));
});


module.exports = router;