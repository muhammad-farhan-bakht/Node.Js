const admin = require('firebase-admin');
const functions = require('firebase-functions');
const express = require('express');
admin.initializeApp();
const defaultDatabase = admin.database();
const app = express();
app.use(express.json());

app.post('/api/getData', (req, res) => {
    defaultDatabase.ref('/users/').child(req.body.uid).once('value').then(snapshot => {
        var username = (snapshot.val() && snapshot.val().name) || 'Anonymous';
        var key = (snapshot.val() && snapshot.val().key) || '4321';
        var uid = (snapshot.val() && snapshot.val().uid) || '1234';
        res.status(200).send([{
            username, key, uid
        }]);
        return null;
    }).catch(error => {
        console.error(error);
        res.error(500);
    });
});

exports.app = functions.https.onRequest(app);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
