#!/bin/bash

#sudo npm install -g aws-cdk
sudo yum install -y maven

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR/../../../cdks/BuildBoxCDK
echo "Creating Buildbox Stack"
cdk bootstrap
cdk deploy --all --require-approval never
echo "Buildbox Stack created"