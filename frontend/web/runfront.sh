#!/bin/bash

# Get node setup file
curl -sL https://deb.nodesource.com/setup_4.x | sudo -E bash -
wait
# Install node and build tools
sudo apt-get install -y nodejs build-essential
wait
# Install http-server
sudo npm install -g http-server webpack
wait
# Install dependencies
sudo npm install
wait
# Build production version with webpack
webpack -p
wait
# Run http-server to launch web frontend
sudo http-server -p 80