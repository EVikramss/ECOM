#!/bin/bash

# initalize jenkins
sudo python -m JenkinsInitializer

# disable jenkins user
sudo cp ../files/config_disableUserLogin.xml /var/lib/jenkins/config.xml

# copy jobs
sudo cp -rf ../files/jobs/* /var/lib/jenkins/jobs/
sudo chmod -R 777 /var/lib/jenkins/jobs/

# restart jenkins service
sudo systemctl restart jenkins.service

#install plugins
./installAdditionalPlugins.sh