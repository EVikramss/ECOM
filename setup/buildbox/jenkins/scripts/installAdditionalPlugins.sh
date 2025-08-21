#!/bin/bash

# download cli jar
wget http://localhost:8080/jnlpJars/jenkins-cli.jar

# install plugins
java -jar jenkins-cli.jar -s http://localhost:8080 install-plugin uno-choice -deploy
java -jar jenkins-cli.jar -s http://localhost:8080 install-plugin schedule-build -deploy

# restart jenkins service
sudo systemctl restart jenkins.service