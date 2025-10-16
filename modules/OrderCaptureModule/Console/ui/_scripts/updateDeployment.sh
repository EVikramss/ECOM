#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
cd $SCRIPT_DIR
cd ..

# get urls
client_id=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='ORDCAPCLIENTID'].OutputValue" --output text)
client_pool_id=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='ORDCAPUSERPOOLID'].OutputValue" --output text)
CLOUDFRONTDOMAIN=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='CLOUDFRONTDOMAIN'].OutputValue" --output text)
updated_callback_url=$(echo "https://$CLOUDFRONTDOMAIN/" | awk '{print tolower($0)}')
USERINFOGRPHURL=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='USERINFOGRPHURL'].OutputValue" --output text)
ITEMINFOGRPHURL=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='ITEMINFOGRPHURL'].OutputValue" --output text)
SNSAPIURL=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='SNSAPIURL'].OutputValue" --output text)
SKULISTEP=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='SKULISTEP'].OutputValue" --output text)
cognitoURL="https://cognito-idp.us-east-1.amazonaws.com/$client_pool_id"

# update url's in ui
sed -i "s#SKU_URL#${SKULISTEP}#g" src/common/config.json
sed -i "s#USER_INFO_URL#${USERINFOGRPHURL}#g" src/common/config.json
sed -i "s#AVL_INFO_URL#${ITEMINFOGRPHURL}#g" src/common/config.json
sed -i "s#ORDER_URL#${SNSAPIURL}#g" src/common/config.json
sed -i "s#COGNITO_URL#${cognitoURL}#g" src/common/config.json
sed -i "s#CLIENT_ID#${client_id}#g" src/common/config.json
sed -i "s#REDIRECT_URL#${updated_callback_url}#g" src/common/config.json

# build ui
npm install
npm run build

# cleanup
rm -rf node_modules/*

# copy built files to s3 bucket
s3bucket_arn=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='WEBBUCKET'].OutputValue" --output text)
s3bucket="${s3bucket_arn##*:}"
aws s3 rm "s3://$s3bucket/ordcpt/ui/" --recursive
aws s3 cp ./dist/ "s3://$s3bucket/ordcpt/ui/" --recursive

# clear cloudfront cache
# TBD