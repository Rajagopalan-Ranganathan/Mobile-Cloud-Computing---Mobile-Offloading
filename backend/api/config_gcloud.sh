#!bin/sh

set -e

gcloud components install kubectl

# first, create the cluster
echo "\n======>>> Creating the cluster...\n"
sleep 1
gcloud container \
  --project "mcc-2016-g10-p2" \
  clusters create "mcc-cluster" \
  --zone "europe-west1-c" \
  --machine-type "n1-standard-1" \
  --num-nodes "2" \
  --network "default"

# create the persistent disk to store mongoDB data
echo "\n======>>> Creating persistent disk for mongoDB...\n"
sleep 1
gcloud compute disks create \
  --project "mcc-2016-g10-p2" \
  --zone "europe-west1-c" \
  --size 1GB \
  mongo-disk

gcloud config set compute/zone europe-west1-c
gcloud config set container/use_client_certificate True
gcloud container clusters get-credentials mcc-cluster

echo "\n======>>> Creating controller & service for mongoDB replicas...\n"
sleep 1

cd ./db_replication
make add-replica DISK_SIZE=200GB ZONE=europe-west1-c
make add-replica DISK_SIZE=200GB ZONE=europe-west1-c
make add-replica DISK_SIZE=200GB ZONE=europe-west1-c

cd ..


# echo "\n======>>> Creating controller & service for mongoDB...\n"
# sleep 1
# kubectl create -f db_controller.yml
# kubectl create -f db_service.yml

echo "\n======>>> Waiting for the replicas to initiate...\n"
sleep 120

echo "\n======>>> Creating controller & service for the Node.js app...\n"
sleep 1
kubectl create -f web_controller.yml
kubectl create -f web_service.yml
