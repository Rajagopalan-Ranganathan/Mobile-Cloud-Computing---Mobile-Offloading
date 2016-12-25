# mcc-2016-g10-p2

Assignment 2 was done by completing also all of the challenges (Android, offline operations and Facebook 3rd party login)

Web application is available at: https://ocr.teodorpatras.com

## Backend

`cd backend/`

### Deployment

To deploy you should run `sh deploy.sh`. This script will:

- install Docker
- install GCloud SDK
- install kubectl
- generate the web application Docker image and push it to Google Cloud registry
- configure the replication nodes for MongoDB
- configure the appropriate controllers and services for the web app
- provide you with the public IP of the cluster in the end

*Important*
However, this script will stop at certain steps giving you instructions what to do next and then to re-run the same script again. This is needed because:
- in order to run `docker build`, the current user needs to be added to the docker group and changes won't take effect unless you log out and in again
- in order for gcloud command line tool to work you need to restart the terminal session

After following the instructions provided by the deploy script, you will have a functioning Kubernetes cluster deployed on Google cloud :)

### DB replication

To create DB replicas, cd into `/db_replication` and run:

`make add-replica DISK_SIZE=200GB ZONE=europe-west1-c`

For the sake of this project, three replicas have been created using the above command: `mongo-1`, `mongo-2` and `mongo-3`. 
These replicas are then used by the `mongoAdapter.js` to connect to the url: `mongodb://mongo-1,mongo-2,mongo-3/usersDB`.

### Development

**posting a rolling update on Kubernetes**
`sh build_image.sh` 
`kubectl rolling-update web-controller --image=gcr.io/mcc-2016-g10-p2/teodor/mcc2:latest --image-pull-policy=Always`

**get external IP**
`gcloud compute forwarding-rules list`

### Other commands

Run locally: 
`sh runserver.sh`

Stop/remove all Docker containers:

`docker stop $(docker ps -a -q)`
`docker rm $(docker ps -a -q)`

Delete all images
`docker rmi $(docker images -q)`

Set up the external network:
`docker network create -d bridge nginx-proxy`

### API Doc

```
POST /api/users/auth
{
	"email" : "...",
	"password" : "..."
}

Response:
Status 200, body:

{ 
	"token" : "..."
}

PUT /api/users/register
{
	"email" : "...",
	"name"  : "....",
	"password" : "..."
}

POST /api/users/facebook
{
	"email" : "...",
	"fbtoken" : "...",
    "name" : "..."
}

Response:
Status 200, body:

{ 
	"token" : "..."
}

Response:
Status 201, body:

{ 
	"message" : "Success!"
}

GET /api/ocr/history
Required header:	Authorization : Bearer token_from_auth

Response:
Status 200, body:
[
    {
      "thumbnails": ["base64 string"],
      "sources" : [ array of base64 strings of source images ],
      "ocr": ["ocr_text"],
      "creationTime": date_in_milliseconds,
      "benchmarks": [milliseconds]
    }
    ...
]

POST /api/ocr/text
Required header:	Authorization : Bearer token_from_auth

{ 
    "images" : [base64_strings]
}

Response:
Status 200, body:

{
    "thumbnails": ["base64 string"],
    "sources": ["base64 string"],
     "ocr": ["ocr_text"],
     "benchmarks": [milliseconds],
     "creationTime": date_in_milliseconds
}
```

## Frontend

Frontend of the project is implemented as a responsive website, but also Android application was made.

### Android application

To build APK files run buildapks.sh in frontend/android directory.

`sh buildapks.sh`

After the build you can find the APK files at:
mobileOffloading at: frontend/android/Project2/app/build/outputs/apk/app-debug.apk


### Web application

Web application supports also Facebook login and offline operations.

To build and run web application, run runfront.sh in frontend/web directory.

`sh runfront.sh`

The build script will install all the required dependencies and launch a localhost server.

The web application is also available at: https://ocr.teodorpatras.com
