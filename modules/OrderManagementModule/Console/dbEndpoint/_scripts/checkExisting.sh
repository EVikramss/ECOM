#!/bin/bash

FUNCTION_NAME="ECOMORDMGMdbEndpoint"

if aws lambda get-function --region us-east-1 --function-name "$FUNCTION_NAME" > /dev/null 2>&1; then
    echo "Lambda function '$FUNCTION_NAME' exists."
	exit 1
else
    echo "Lambda function '$FUNCTION_NAME' does not exist."
	exit 0
fi
