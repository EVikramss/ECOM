#!/bin/bash

sudo yum install -y java-17-amazon-corretto-devel
sudo yum install -y docker
sudo yum install -y git
sudo yum install -y python
sudo yum install -y python3-pip
sudo service docker start
sudo usermod -a -G docker ec2-user
sudo amazon-linux-extras install -y epel
sudo yum install p7zip -y
sudo yum install zip -y
sudo ln -s /usr/bin/7za /usr/bin/7z

# install maven
#wget https://archive.apache.org/dist/maven/maven-3/3.9.4/binaries/apache-maven-3.9.4-bin.tar.gz
#tar -zxf apache-maven-3.9.4-bin.tar.gz
sudo yum install -y maven

# install npm for cdk
curl -sL https://rpm.nodesource.com/setup_18.x | sudo -E bash -
sudo yum install -y nodejs
sudo npm install -g aws-cdk

# install jenkins
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io.key
sudo yum update -y
sudo yum install -y jenkins --nogpgcheck			# Note: Do not use --nogpgcheck in prod environment
sudo service jenkins start

# configure jenkins
cd ../jenkins/scripts
./configureJenkins.sh

# temp 
# sudo yum -y install git-daemon.x86_64

# sudo mkdir /tmp/repo
# sudo mkdir /tmp/repo/ECOM
# sudo chmod -R 777 /tmp/repo/ECOM
# cd /tmp/repo/ECOM
# sudo git init --bare
# sudo nohup git daemon --verbose --export-all --enable=receive-pack --base-path=/tmp/repo /tmp/repo &

# git config --global user.name "Name"
# git config --global user.email "test@test.com"

# cd /tmp
# sleep 10
# git clone https://github.com/EVikramss/ECOM.git
# cd /tmp/ECOM
# sudo chmod -R 777 /tmp/ECOM
# cp -rf /home/ec2-user/ECOM/* .
# git add .
# git commit -m "added"
# git push