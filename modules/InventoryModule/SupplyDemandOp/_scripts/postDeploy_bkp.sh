#!/bin/bash

SSM_EP_ID=$(aws cloudformation describe-stacks --stack-name ECOMINVStack --query "Stacks[0].Outputs[?OutputKey=='SSMEPID'].OutputValue" --output text)
SSM_EP_URL=$(aws ec2 describe-vpc-endpoints --vpc-endpoint-ids "$SSM_EP_ID" --query "VpcEndpoints[0].DnsEntries[0].DnsName" --output text)

FUNCTION_NAME='SupplyDemandOp'

KEY_NAME="SM_EP_URL"
KEY_VALUE="$SM_EP_URL"

# Fetch existing environment variables
EXISTING_VARS=$(aws lambda get-function-configuration --function-name "$FUNCTION_NAME" --query 'Environment.Variables' --output json)

# Merge new variable with existing ones
UPDATED_VARS=$(echo "$EXISTING_VARS" | jq --arg key "$KEY_NAME" --arg value "$KEY_VALUE" '. + {($key): $value}')
FORMATTED_VARS=$(echo $UPDATED_VARS | jq -r 'to_entries | map("\(.key)=\(.value)") | join(", ") | "{\(.)}"')

# Update Lambda function configuration
aws lambda update-function-configuration --function-name "$FUNCTION_NAME" --environment "Variables=$FORMATTED_VARS"
