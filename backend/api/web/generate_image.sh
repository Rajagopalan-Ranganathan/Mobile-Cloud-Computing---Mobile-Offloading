#!/bin/bash

set -e

gcloud auth login
gcloud components update
gcloud config set project mcc-2016-g10-p2

docker build --build-arg SECRET=N2XN4fhMG%ofVPn+D#+uxw2b**CD^kf877.kG]7dB/eG3T9UG7 --build-arg KUBERNETES=true -t teodor/mcc2:latest .
docker tag teodor/mcc2:latest gcr.io/mcc-2016-g10-p2/teodor/mcc2:latest
gcloud docker push gcr.io/mcc-2016-g10-p2/teodor/mcc2:latest