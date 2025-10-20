#!/bin/bash

if aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='USERINFOGRPHURL'].OutputValue" --output text > /dev/null 2>&1; then
    echo "UserInfo exists."
	exit 1
else
    echo "UserInfo does not exist."
	exit 0
fi