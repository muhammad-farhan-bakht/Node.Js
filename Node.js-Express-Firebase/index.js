const express = require('express');
var firebase = require("firebase");
require("firebase/database");

// Set the configuration for your app
var config = {
    apiKey: "AIzaSyAXM19kL8RhheoDfTNbNYjMiE2J1Emlhkc",
    authDomain: "myservers-aaa05.firebaseapp.com",
    databaseURL: "https://myservers-aaa05.firebaseio.com",
    storageBucket: "myservers-aaa05.appspot.com"
};

firebase.initializeApp(config);
const app = express();
app.use(express.json());

app.post('/api/getData', (req, res) => {

    console.log("Firebase Reference" + firebase.database().ref('/users/').child(req.body.uid));

    firebase.database().ref('/users/').child(req.body.uid).once('value').then(function (snapshot) {
        var username = (snapshot.val() && snapshot.val().name) || 'Anonymous';
        var key = (snapshot.val() && snapshot.val().key) || '4321';
        var uid = (snapshot.val() && snapshot.val().uid) || '1234';
        res.send([{
            username, key, uid

        }]);
    });


});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Listening on port ${port}....`));
