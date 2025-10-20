#!/bin/bash

if aws cloudformation describe-stacks --stack-name ECOMORDCPT --query "Stacks[0].Outputs[?OutputKey=='ITEMINFOURL'].OutputValue" --output text > /dev/null 2>&1; then
    echo "ItemInfo exists."
	exit 1
else
    echo "ItemInfo does not exist."
	exit 0
fi