'use strict'

const mongoose = require('mongoose')
const db = process.env.APP_ENV == 'test' ? process.env.DB_TEST_NAME : process.env.DB_NAME

const async = require('async')

var address = `mongodb://mongo/${db}`

if (process.env.KUBERNETES === 'true') {
    address = `mongodb://mongo-1,mongo-2,mongo-3/${db}`
}

var connected = false 

function connect() {
  return new Promise((resolve, reject) => {
    mongoose.connect(address, (err) => {
        if (err) {
            connected = false
            reject(err)
        } else {
            connected = true
            // List collections
            // for (let i in mongoose.connection.collections) {
            //     console.log(mongoose.connection.collections[i].collectionName);
            // }
            resolve(address)
        }
    })
  })
}

function disconnect() {
    return mongoose.disconnect()
}

function dropDatabase() {
    return new Promise((resolve, reject) => {
        if (connected == false) {
            reject(new Error('Mongoose not connected. Cannot drop db!'))
        } else {
            mongoose.connection.db.dropDatabase((err) => {
                if (err) { return reject(err) }
                resolve()
            })
        }
    })
}

function loadFixtures(fixtures) {
    return new Promise((resolve, reject) => {
        if (connected == false) {
            reject(new Error('Mongoose not connected. Cannot drop db!'))
        } else {
            const names = Object.keys(fixtures.collections)
            const db = mongoose.connection
            async.each(names, (name, cb) => {
                db.db.createCollection(name, (err, collection) => {
                    if (err) return cb(err)
                    collection.insert(fixtures.collections[name], cb)
                })
            }, (err) => {
                if (err) { return reject(err) }
                resolve()
            })
        }
    })
}

module.exports = {connect, disconnect, dropDatabase, loadFixtures}