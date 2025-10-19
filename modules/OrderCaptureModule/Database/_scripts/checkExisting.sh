#!/bin/bash

if aws dynamodb describe-table --table-name ItemInfo --region us-east-1 > /dev/null 2>&1; then
    echo "ItemInfo dynamodb exists."
	exit 1
else
    echo "ItemInfo dynamodb does not exist."
	exit 0
fi