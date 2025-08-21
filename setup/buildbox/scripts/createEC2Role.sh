#!/bin/bash

sudo yum install -y python
sudo yum install -y python3-pip
pip install wonk

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR
mkdir managed

# create role
aws iam create-role --role-name ec2BuildBoxRole --assume-role-policy-document file://ec2-trust-policy.json

# get all needed policy docs
wonk fetch --name AmazonRDSFullAccess > managed/AmazonRDSFullAccess.json
wonk fetch --name AmazonVPCFullAccess > managed/AmazonVPCFullAccess.json
wonk fetch --name AWSLambda_FullAccess > managed/AWSLambda_FullAccess.json
wonk fetch --name AmazonAPIGatewayAdministrator > managed/AmazonAPIGatewayAdministrator.json
wonk fetch --name AmazonECS_FullAccess > managed/AmazonECS_FullAccess.json
wonk fetch --name SecretsManagerReadWrite > managed/SecretsManagerReadWrite.json
wonk fetch --name AmazonS3FullAccess > managed/AmazonS3FullAccess.json
wonk fetch --name AmazonEventBridgeFullAccess > managed/AmazonEventBridgeFullAccess.json
wonk fetch --name CloudWatchFullAccessV2 > managed/CloudWatchFullAccessV2.json
wonk fetch --name AWSCloudFormationFullAccess > managed/AWSCloudFormationFullAccess.json
wonk fetch --name AmazonSSMFullAccess > managed/AmazonSSMFullAccess.json
wonk build --all

POLICY_DIR="./combined"
for file in "$POLICY_DIR"/*.json; do
  POLICY_NAME=$(basename "$file" .json)
  echo "Attaching policy: $POLICY_NAME from $file"
  aws iam put-role-policy --role-name ec2BuildBoxRole --policy-name "$POLICY_NAME" --policy-document "file://$file"
done