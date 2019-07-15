const Joi = require('joi');
const express = require('express');
const app = express();
app.use(express.json());

const courses = [
    { id: 1, name: 'course 1' },
    { id: 2, name: 'course 2' },
    { id: 3, name: 'course 3' },
]
///////////// HTTP GET ///////////
app.get('/', (req, res) => {
    res.send('My very first node.js Express Server');
});

app.get('/api/courses', (req, res) => {
    res.send(courses);
});

// Param in Route
app.get('/api/courses/:id', (req, res) => {
    const course = courses.find(c => c.id === parseInt(req.params.id));
    if (!course) return res.status(404).send('The course with the given ID was not found');
    res.send(course);
});

// Multi Params in Route
app.get('/api/posts/:year/:month', (req, res) => {
    res.send(req.params);
});

// To read Query String Parameter eg: ?sortBy=name
app.get('/api/query/param', (req, res) => {
    res.send(req.query);
});


///////////// HTTP POST ///////////
app.post('/api/courses', (req, res) => {
    const {error} = validateCourse(req.body);
    if (error) {
        res.status(400).send(error.details[0].message);
        return;
    };

    const course = {
        id: courses.length + 1,
        name: req.body.name
    };
    courses.push(course);
    res.send(course);
});

///////////// HTTP UPDATE (PUT) ///////////

app.put('/api/courses/:id',(req, res) => {

    const course = courses.find(c => c.id === parseInt(req.params.id));
    if (!course) return res.status(404).send('The course with the given ID was not found');

    const {error} = validateCourse(req.body);
    if (error) {
        res.status(400).send(error.details[0].message);
        return;
    };

    course.name = req.body.name;
    res.send(course);

});

///////////// HTTP DELETE ///////////
app.delete('/api/courses/:id', (req, res) => {

    const course = courses.find(c => c.id === parseInt(req.params.id));
    if (!course) return res.status(404).send('The course with the given ID was not found');

    const index = courses.indexOf(course);
    courses.splice(index,1);

    res.send(course);
});

const port = process.env.PORT || 3000;
app.listen(port, () => console.log(`Listening on port ${port}....`));

function validateCourse(course){
    const schema = {
        name: Joi.string().min(3).required()
    };
    return Joi.validate(course, schema);
}