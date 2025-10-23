#!/bin/bash

orderHistoryARN=aws lambda get-function --function-name ECOMORDCPTOrderHistoryOp --query 'Configuration.FunctionArn' --output text
orderHistoryRoleARN=$(aws lambda get-function --function-name ECOMORDCPTOrderHistoryOp --query 'Configuration.Role' --output text)

userInfoTableARN=$(aws dynamodb describe-table --table-name UserInfo --query 'Table.TableArn' --output text)

sed -i "s#orderHistoryRoleARN#${orderHistoryRoleARN}#g" policy.json
sed -i "s#userInfoTableARN#${userInfoTableARN}#g" policy.json

aws dynamodb put-resource-policy --resource-arn $userInfoTableARN --policy file://policy.json