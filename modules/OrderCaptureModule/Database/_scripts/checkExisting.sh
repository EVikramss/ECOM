#!/bin/bash

if aws dynamodb describe-table --table-name UserInfo --region us-east-1 > /dev/null 2>&1; then
    echo "UserInfo dynamodb exists."
	exit 1
else
    echo "UserInfo dynamodb does not exist."
	exit 0
fi