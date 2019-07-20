const express = require('express');
const braintree = require('braintree')
const app = express();
app.use(express.json());

var gateway = braintree.connect({
    environment: braintree.Environment.Sandbox,
    merchantId: "bzjnzdzvs2xcjrm6",
    publicKey: "ky5gg8qfsv3yfczm",
    privateKey: "0057f3a2f3ed13ecb43635e665ca9b2d"
});

app.get('/api/getClientToken', (req, res) => {
    gateway.clientToken.generate({}, (err, response) => {
        if (err) throw new Error(err);
        if (response.success) {
            res.status(200).send({ clientToken: response.clientToken });
        } else {
            console.error(result.message);
            console.error(err);
            res.status(404).send('Something went wrong');
        }
    });
});

app.post("/api/checkout", function (req, res) {
    const nonceFromTheClient = req.body.nonce;
    const amount = req.body.amount;
    console.log('req ' + req);
    console.log('nonceFromTheClient ' + nonceFromTheClient);
    console.log('amount ' + amount);

    gateway.transaction.sale({
        amount: amount,
        paymentMethodNonce: nonceFromTheClient,
        options: {
            // This option requests the funds from the transaction
            // once it has been authorized successfully
            submitForSettlement: false
          }
    }, function (error, result) {
        if (result) {
            if (result.success) {
                console.log('result '+result.success+": "+result);
                console.log('result.transaction.status '+result.success+": "+result.transaction.status);
                res.status(200).send({ message: "Your transaction has been successfully processed." });
            } else {
                console.log('result '+result.success+": "+result.message );
                res.status(200).send({ message: result.message });
            }
        } else {
            console.log('error '+error);
            console.log('result.errors.deepErrors() '+result.errors.deepErrors());
            res.status(500).send({ message: result.errors.deepErrors() });
        }
    });
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Listening on port ${port}....`));