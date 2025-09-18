#!/bin/bash

# get client details from stack
client_id=$(aws cloudformation describe-stacks --stack-name BuildBoxStack --query "Stacks[0].Outputs[?OutputKey=='BUILDBOXCLIENTID'].OutputValue" --output text)
client_pool_id=$(aws cloudformation describe-stacks --stack-name BuildBoxStack --query "Stacks[0].Outputs[?OutputKey=='BUILDBOXUSERPOOLID'].OutputValue" --output text)
updated_callback_url=$(aws cognito-idp describe-user-pool-client --user-pool-id "$client_pool_id" --client-id "$client_id" --query "UserPoolClient.CallbackURLs" --output text | awk '{print tolower($0)}')

# update call back url to lower case
aws cognito-idp update-user-pool-client --user-pool-id "$client_pool_id" --client-id "$client_id" --callback-urls "[\"$updated_callback_url\"]" --supported-identity-providers "[\"COGNITO\"]" --allowed-o-auth-flows "[\"code\"]" --allowed-o-auth-scopes "[\"openid\"]" --allowed-o-auth-flows-user-pool-client --output text

# create users
aws cognito-idp admin-create-user --user-pool-id "$client_pool_id" --username admin --temporary-password "Password@123" --user-attributes Name=email,Value=test@test.com --message-action SUPPRESS

