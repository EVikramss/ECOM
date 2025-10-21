#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
cd $SCRIPT_DIR
cd ..

# set cloudfront url as redirect url in cognito
client_id=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='ORDCAPCLIENTID'].OutputValue" --output text)
client_pool_id=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='ORDCAPUSERPOOLID'].OutputValue" --output text)

CLOUDFRONTDOMAIN=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='CLOUDFRONTDOMAIN'].OutputValue" --output text)
updated_callback_url=$(echo "https://$CLOUDFRONTDOMAIN/" | awk '{print tolower($0)}')
static_content_url=$(echo "https://$CLOUDFRONTDOMAIN/images/" | awk '{print tolower($0)}')

aws cognito-idp update-user-pool-client --user-pool-id "$client_pool_id" --client-id "$client_id" --callback-urls "[\"$updated_callback_url\"]" --supported-identity-providers "[\"COGNITO\"]" --allowed-o-auth-flows "[\"code\"]" --allowed-o-auth-scopes "[\"phone\",\"openid\",\"email\"]" --allowed-o-auth-flows-user-pool-client --output text
cognitoURL="https://cognito-idp.us-east-1.amazonaws.com/$client_pool_id"

# get other urls
USERINFOGRPHURL=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='USERINFOGRPHURL'].OutputValue" --output text)
ITEMINFOURL=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='ITEMINFOURL'].OutputValue" --output text)
SNSAPIURL=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='SNSAPIURL'].OutputValue" --output text)
SKULISTEP=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='SKULISTEP'].OutputValue" --output text)

SKUCLDFRDMN=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='SKUCLDFRDMN'].OutputValue" --output text) 
CLDSKUURL=$(echo "https://$SKUCLDFRDMN" | awk '{print tolower($0)}')

# update url's in ui
sed -i "s#SKU_URL#${CLDSKUURL}#g" src/common/config.json
sed -i "s#USER_INFO_URL#${USERINFOGRPHURL}#g" src/common/config.json
sed -i "s#ITEM_INFO_URL#${ITEMINFOURL}#g" src/common/config.json
sed -i "s#ORDER_URL#${SNSAPIURL}#g" src/common/config.json
sed -i "s#COGNITO_URL#${cognitoURL}#g" src/common/config.json
sed -i "s#CLIENT_ID#${client_id}#g" src/common/config.json
sed -i "s#REDIRECT_URL#${updated_callback_url}#g" src/common/config.json
sed -i "s#STATIC_LOC#${static_content_url}#g" src/common/config.json

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

# copy image files to bucket
cd "$SCRIPT_DIR/../../"
mkdir /tmp/images
unzip -q images.zip -d /tmp/images
aws s3 cp /tmp/images/ "s3://$s3bucket/ordcpt/ui/images" --recursive

updated_callback_url=$(echo "https://$CLOUDFRONTDOMAIN" | awk '{print tolower($0)}')

# update api gateway allowed origin to cloudfront url
SKULISTID=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='SKULISTID'].OutputValue" --output text)
aws apigatewayv2 update-api --api-id "${SKULISTID}" --cors-configuration "{\"AllowOrigins\":[\"${updated_callback_url}\"]}"

# TBD as rest api uses apigateway and not v2 and below doesnt work
SNSAPIID=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='SNSAPIID'].OutputValue" --output text)
aws apigateway update-api --api-id "${SNSAPIID}" --cors-configuration "{\"AllowOrigins\":[\"${updated_callback_url}\"]}"


echo "Cloudfront url:"
echo "$updated_callback_url"