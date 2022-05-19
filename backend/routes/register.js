const router = require('express').Router();
let UserData = require('../models/user.model');

router.route('/').post((req,res) => {

    const name = req.body.name;
    const roll = req.body.roll;
    const email = req.body.email;
    const password = req.body.password;

    const newStudentData = new UserData({
        name,
        roll,
        email,
        password,
        auth : "student"
    });

    newStudentData.save()
        .then(() => res.send({message: "1"}))
        .catch(err => res.status(400).json('Error: '+ err));
});

module.exports = router;