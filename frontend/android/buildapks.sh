#!/bin/bash

read -p "Install JDK and Android SDK? (y/n)" yn
case $yn in
	[Yy]* ) 
		# install jdk
		sudo apt-get install default-jdk

		# download android sdk
		wget https://dl.google.com/android/android-sdk_r24.4.1-linux.tgz

		tar -xvf android-sdk_r24.4.1-linux.tgz -C ~ 

		# install sdk packages
		# ~/android-sdk-linux/tools/android update sdk -u -a -t 7

		# needed to accept license
		mkdir -p ~/android-sdk-linux/licenses
		sudo chmod 777 ~/android-sdk-linux/licenses
		echo '8933bad161af4178b1185d1a37fbf41ea5269c55' > ~/android-sdk-linux/licenses/android-sdk-license
		;;
	[Nn]* ) echo NO;;
	* ) echo "Please answer yes or no.";;
esac

# set path
export ANDROID_HOME=$HOME/android-sdk-linux


cd Project2
./gradlew assembleDebug

echo "APKs built"
echo "mobileOffloading at: frontend/android/Project2/app/build/outputs/apk/app-debug.apk"

