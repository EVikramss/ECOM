#!/bin/bash

AMP_APP_ID=$(aws cloudformation describe-stacks --stack-name ECOMORDMGM --query "Stacks[0].Outputs[?OutputKey=='AMPEP'].OutputValue" --output text)
AMP_JOB_ARN=$(aws amplify list-jobs --app-id "$AMP_APP_ID" --branch-name main --query "jobSummaries[0].jobArn" --output text)

if [ "$AMP_JOB_ARN" != "None" ]; then
    echo "Console exists."
	exit 1
else
    echo "Console does not exist."
	exit 0
fi
