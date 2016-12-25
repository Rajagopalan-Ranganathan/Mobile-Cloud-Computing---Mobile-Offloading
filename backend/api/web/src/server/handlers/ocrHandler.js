'use strict'

const userModel = require('../../db/models/user')
const logger = require('../../logger')
const jwt = require('jsonwebtoken')
const validator = require('validator')
const tesseract = require('node-tesseract')
const fs = require('fs')
const easyimg = require('easyimage')
const async = require('async')
const CronJob = require('cron').CronJob
const uuidV1 = require('uuid/v1');

// ------------------------------------------------

// 7 days
const SOURCE_IMAGE_LIFETIME = 7 * 24 * 60 * 60 * 100

// CronJob for every day at 10 am, delete the expired source images
new CronJob('0 10 * * *', () => {
    userModel.find({}, (err, users) => {
        if (err || !users) {
            return console.error('Cron Job error: ', err)
        }
        var newMeta = []
        const now = Date.now()
        var update = false
        users.forEach(user => {
            if (!user.meta) { return }

            newMeta = []
            update = false

            user.meta.forEach(metaDict => {
                if (now - metaDict.creationTime >= SOURCE_IMAGE_LIFETIME) {
                    update = true
                    metaDict.sources = null
                }
                newMeta.push(metaDict)
            })

            if (update == true) {
                userModel.findOneAndUpdate({ email: user.email }, { meta: newMeta }, { new: true }, (err, user) => { })
            }
        })
    })
}, null, false, 'America/Los_Angeles').start()

function getHistory(req, res, next) {
    userModel.findOne({ email: req.user.email }, (err, user) => {
        if (err) { return next(err) }
        if (!user) { return next(new Error('No user found!')) }
        var metaArray = user.meta
        metaArray.forEach(dict => {
            if (dict.thumbnails && dict.thumbnails.length > 0) {
                let array = []
                dict.thumbnails.forEach(path => {
                    let base64 = base64FromFile(path)
                    if (base64) {
                        array.push(base64)
                    }
                })
                dict.thumbnails = array
            }
            if (dict.sources && dict.sources.length > 0) {
                let array = []
                dict.sources.forEach(path => {
                    let base64 = base64FromFile(path)
                    if (base64) {
                        array.push(base64)
                    }
                })
                dict.sources = array
            }
        })
        res.status(200).send(metaArray)
    })
}

function processImages(req, res, next) {
    if (!req.user) { return next(new Error('User is null!')) }
    if (!Array.isArray(req.body.images) || req.body.images.length == 0) {
        return next(new Error('Invalid payload'))
    }

    for (let i = 0; i < req.body.images.length; i++) {
        if (req.body.images[i].match(/^data:image\/([a-z]+);base64,(.+)$/) == null) {
            return next(new Error('Images are not base64!'))
        }
    }

    handleImages(req.body.images, (err, images) => {
        if (err) { return next(err) }

        const benchmarks = images.map(img => { return img.benchmark })
        const thumbnails = images.map(img => { return img.thumbnail })
        const thumnails64 = images.map(img => { return img.thumbnail64 })

        const ocr = images.map(img => { return img.text })
        const sources64 = images.map(img => { return img.base64 })
        const sources = images.map(img => { return img.path })
        const creationTime = Date.now()

        const dict = { thumbnails, sources, ocr, creationTime, benchmarks }

        updateUserHistory(req.user.email, dict, err => {
            if (err) { return next(err) }
            const response = { thumbnails : thumnails64, benchmarks, creationTime, ocr }
            res.status(200).send(response)
        })
    })
}

const routes = {
    GET: { '/history': getHistory },
    POST: { '/text': processImages }
}

module.exports = { routes }


// ------------------------------------------------

function updateUserHistory(email, dict, completion) {

    userModel.findOne({ email }, (err, user) => {
        if (err) { return completion(err) }
        else if (!user) {
            return completion(new Error('No user found!'))
        }

        var meta = user.meta || []
        meta.push(dict)

        userModel.findOneAndUpdate({ email }, { meta }, { new: true }, (err, user) => {
            completion(err)
        })
    })
}

function handleImages(images, completion) {

    var tasks = []

    images.forEach(img => {
        let base64, extension, match
        base64 = img.replace(/^data:image\/([a-z]+);base64,/, '')
        extension = 'png'
        match = img.match(/^data:image\/([a-z]+)/)
        if (match && match.length > 1) {
            extension = match[1]
        }

        tasks.push(function (callback) {
            let now = Date.now()
            persistImageToDisk({ base64, extension }, (err, imgDict) => {
                if (err) { return callback(err, null) }
                generateThumbnail(imgDict, thumbnailDict => {
                    if (!thumbnailDict) { return callback(new Error('No Thumbnail!'), null) }
                    imgDict.thumbnail = thumbnailDict.path
                    imgDict.thumbnail64 = thumbnailDict.base64
                    tesseract.process(imgDict.path, (err, text) => {
                        if (err) { return callback(err, null) }
                        let diff = Date.now() - now
                        imgDict.benchmark = diff
                        imgDict.text = text
                        callback(null, imgDict)
                    })
                })
            })
        })
    })

    async.parallel(tasks, completion)
}

function tesseractOcr(images, completion) {
    var tasks = []
    images.forEach(imgDict => {
        tasks.push(function (completion) {
            tesseract.process(imgDict.path, completion)
        })
    })
    async.parallel(tasks, completion)
}

function persistImageToDisk(imgDict, completion) {

    if (!imgDict.base64 && !imgDict.extension) {
        console.error('\n\nERROR IMGDICT IS NOT VALID\n', imgDict)
        return
    }

    const imgPath = `${__dirname}/image_${uuidV1()}.${imgDict.extension}`

    fs.writeFile(imgPath, imgDict.base64, 'base64', err => {
        if (err) {
            console.error('Error while writing file: ', err);
            completion(err, null)
        } else {
            imgDict['path'] = imgPath
            completion(null, imgDict)
        }
    })
}

function deleteImageFromDisk(imgPath, completion) {
    fs.unlink(imgPath, err => {
        if (completion) { completion(err) }
    })
}

function generateThumbnail(imgDict, completion) {

    const dest = `${__dirname}/thumbnail_${uuidV1()}.${imgDict.extension}`

    easyimg.thumbnail({
        src: imgDict.path, dst: dest,
        width: 128, height: 128,
        x: 0, y: 0
    }).then((file, reject) => {
        if (reject) { return completion(null) }
        fs.readFile(file.path, (err, data) => {
            if (err || !data) {
                console.error('Error while reading thumbnail: ', err)
                completion(null)
            } else {
                completion({base64: new Buffer(data).toString('base64'), path: file.path})
            }
        })
    })
}

function base64FromFile(path) {
    if (fs.existsSync(path)) {
        const data = fs.readFileSync(path)
        if (data) {
            return new Buffer(data).toString('base64')
        }
    }
    return null
}
