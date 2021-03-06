'use strict'

const Server = require('./server/server')
const logger = require('./logger')
const mongoAdapter = require('./db/mongoAdapter')
const fs = require('fs')

const User = require('./db/models/user')

const server = new Server(process.env.HTTP_PORT)

server.start().then((port) => {
    console.log(` =========> Web app listening on port ${port}`)
    return mongoAdapter.connect()
}).then((address) => {
    console.log(` =========> DB connected to ${address}`)
    const userdict = { email: 'test@test.com', name: 'John Test', password: 'test123' }
    User.find({ email: 'test@test.com' }, (err, results) => {
        if (results && results.length == 0) {
            console.log('Creating test user...')
            new User(userdict).save((err) => {
                if (err) { return console.log('Err while saving test user: ' + err) }
                console.log('Test user saved successfully!')
            })
        } else {
            console.log('Test user found!')
        }
        // for the sake of the demo, start from scratch
        whipeMeta()
    })
}).catch((err) => {
    console.error(` =========> Error on DB connect: ${err}`)
})

function whipeMeta() {
    console.log('\n\nWHIPING METADATA...\n\n')
    User.find({}, (err, results) => {
        if (results && results.length > 0) {
            results.forEach(user => {
                const email = user.email
                const metaArray = user.meta

                if (email) {
                    metaArray.forEach(meta => {
                        if (meta.thumbnails && meta.thumbnails.length > 0) {
                            meta.thumbnails.forEach(file => {
                                if (fs.existsSync(file)) {
                                    fs.unlinkSync(file)
                                }
                            })
                        }
                        
                        if (meta.sources && meta.sources.length > 0) {
                            meta.sources.forEach(file => {
                                if (fs.existsSync(file)) {
                                    fs.unlinkSync(file)
                                }
                            })
                        }
                    })
                    User.findOneAndUpdate({ email: email }, { meta: [] }, { new: true }, (err, user) => {
                        const message = err ? 'Error' : 'Success'
                        console.log(`${message} while deleting meta for: ${email}`)
                    })
                }
            })
        }
    })
}