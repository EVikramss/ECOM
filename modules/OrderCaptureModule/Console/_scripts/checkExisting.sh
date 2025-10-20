#!/bin/bash

if aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='CLOUDFRONTID'].OutputValue" --output text > /dev/null 2>&1; then
    echo "Capture Console exists."
	exit 1
else
    echo "Capture Console does not exist."
	exit 0
fi
