#!bin/sh

set -e

if ! hash docker 2>/dev/null; then
  echo "\n======>>> Installing Docker...\n"
	sleep 1
  wget https://get.docker.com/ -O install_docker.sh
  /bin/sh install_docker.sh
  rm install_docker.sh
  sudo usermod -aG docker $USER
  echo "\n======>>> In order to be able to use docker, you'll have to log in and out again, please do so and run this script again...\n"
  sleep 1
  exit 0
fi

if ! hash gcloud 2>/dev/null; then
  echo "\n======>>> Installing gcloud...\n"
  sleep 1
  curl https://sdk.cloud.google.com | bash
  echo "\n======>>> Please restart the terminal session and run this script again...\n"
  exit 0
fi

echo "\n======>>> Generating & pushing web image to Google registry...\n"
sleep 1

cd ./api/web
sh generate_image.sh

echo "\n======>>> Configuring Google cloud...\n"
sleep 1

cd ..
sh config_gcloud.sh

gcloud compute forwarding-rules list
echo "\n======>>> Success! If all went good, you should see the external IP above!\n"


# echo "\n======>>> Creating nginx-proxy network for Docker"
# sleep 1
# docker network create -d bridge nginx-proxy

# echo "\n======>>> Starting up NGINX & LETSENCRYPT proxy"
# sleep 1

# cd ./nginx-proxy
# docker-compose stop
# docker-compose up -d

# echo "\n======>>> Starting up NGINX & LETSENCRYPT proxy"

# cd ../api
# sh runserver.sh --deploy
