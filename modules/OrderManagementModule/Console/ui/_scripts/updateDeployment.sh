#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
cd $SCRIPT_DIR
cd ..

# get amplify url
AMP_APP_ID=$(aws cloudformation describe-stacks --stack-name ECOMORDMGM --query "Stacks[0].Outputs[?OutputKey=='AMPEP'].OutputValue" --output text)
AMP_DMN_NAME=$(aws amplify get-app --app-id "$AMP_APP_ID" --query "app.defaultDomain" --output text)
AMP_BRANCH_URL="https://main.$AMP_DMN_NAME/"

# set amplify url as redirect url in cognito
client_id=$(aws cloudformation describe-stacks --stack-name ECOMORDMGM --query "Stacks[0].Outputs[?OutputKey=='ORDMGMCLIENTID'].OutputValue" --output text)
client_pool_id=$(aws cloudformation describe-stacks --stack-name ECOMORDMGM --query "Stacks[0].Outputs[?OutputKey=='ORDMGMUSERPOOLID'].OutputValue" --output text)
updated_callback_url=$(echo "$AMP_BRANCH_URL" | awk '{print tolower($0)}')
cognitoURL="https://cognito-idp.us-east-1.amazonaws.com/$client_pool_id"

# get app sync url
httpApiEP=$(aws cloudformation describe-stacks --stack-name ECOMORDMGM --query "Stacks[0].Outputs[?OutputKey=='APIEP'].OutputValue" --output text)

# update url's in ui
sed -i "s#API_EP#${httpApiEP}#g" src/common/config.json
sed -i "s#COGNITO_URL#${cognitoURL}#g" src/common/config.json
sed -i "s#CLIENT_ID#${client_id}#g" src/common/config.json
sed -i "s#REDIRECT_URL#${updated_callback_url}#g" src/common/config.json

# build ui
npm install
npm run build

# cleanup
rm -rf node_modules/*

# copy built files to s3 bucket
s3bucket_arn=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECOMBKTARN'].OutputValue" --output text)
s3bucket="${s3bucket_arn##*:}"
aws s3 rm "s3://$s3bucket/ordmgm/ui/" --recursive
aws s3 cp ./dist/ "s3://$s3bucket/ordmgm/ui/" --recursive

# deploy to amplify
aws amplify start-deployment --app-id "$AMP_APP_ID" --branch-name main --source-url "s3://$s3bucket/ordmgm/ui/" --source-url-type BUCKET_PREFIX