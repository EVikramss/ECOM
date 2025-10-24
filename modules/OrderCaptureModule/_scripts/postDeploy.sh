#!/bin/bash

# increase task count here instead if in cdk
aws ecs update-service --cluster ECOMECSCluster --service SkuListService --desired-count 1

SNSAPIID=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='SNSAPIID'].OutputValue" --output text)
ITEMINFOAPIID=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='ITEMINFOAPIID'].OutputValue" --output text)
CLOUDFRONTDOMAIN=$(aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='CLOUDFRONTDOMAIN'].OutputValue" --output text)
domain_url=$(echo "https://$CLOUDFRONTDOMAIN" | awk '{print tolower($0)}')

# update allowed origin for both post and options
resourceID=$(aws apigateway get-resources --rest-api-id "$SNSAPIID" | jq -r '.items[] | select(.path == "/") | .id')
aws apigateway update-integration-response --rest-api-id "$SNSAPIID" --resource-id "$resourceID" --http-method POST --status-code 200 --patch-operations op=replace,path=/responseParameters/method.response.header.Access-Control-Allow-Origin,value="\"'$domain_url'\""
aws apigateway update-integration-response --rest-api-id "$SNSAPIID" --resource-id "$resourceID" --http-method OPTIONS --status-code 204 --patch-operations op=replace,path=/responseParameters/method.response.header.Access-Control-Allow-Origin,value="\"'$domain_url'\""

# update allowed origin for get
resourceID=$(aws apigateway get-resources --rest-api-id "$ITEMINFOAPIID" | jq -r '.items[] | select(.path == "/info") | .id')
aws apigateway update-integration-response --rest-api-id "$ITEMINFOAPIID" --resource-id "$resourceID" --http-method GET --status-code 200 --patch-operations op=replace,path=/responseParameters/method.response.header.Access-Control-Allow-Origin,value="\"'$domain_url'\""

# subscribe history lambda to status q
OSUTOPICARN=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='OSUTOPICARN'].OutputValue" --output text)
orderHistoryARN=$(aws lambda get-function --function-name ECOMORDCPTOrderHistoryOp --query 'Configuration.FunctionArn' --output text)

aws sns subscribe --topic-arn "$OSUTOPICARN" --protocol lambda --notification-endpoint "$orderHistoryARN"


sleep 10
# deploy changes
aws apigateway create-deployment --rest-api-id "$SNSAPIID" --stage-name prod
aws apigateway create-deployment --rest-api-id "$ITEMINFOAPIID" --stage-name prod