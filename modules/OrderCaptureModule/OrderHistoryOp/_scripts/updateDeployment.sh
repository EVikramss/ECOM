#!/bin/bash

FUNCTION_NAME="ECOMORDCPTOrderHistoryOp"

# get current script path
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
	
# update function
aws lambda update-function-code --function-name "$FUNCTION_NAME" --zip-file "fileb://$SCRIPT_DIR/../../$FUNCTION_NAME.zip"

# update concurrency
reservedConcurrency=$1
provisionedConcurrency=$2
suffix="Alias"

if [[ "$reservedConcurrency" == 0 ]]; then
    aws lambda delete-function-concurrency --function-name "$FUNCTION_NAME"
else
    aws lambda put-function-concurrency --function-name "$FUNCTION_NAME" --reserved-concurrent-executions "$reservedConcurrency"
fi

if (( provisionedConcurrency > 0 )); then
	aws lambda put-provisioned-concurrency-config --function-name "$FUNCTION_NAME" --qualifier "${FUNCTION_NAME}${suffix}" --provisioned-concurrent-executions "$provisionedConcurrency"
fi
