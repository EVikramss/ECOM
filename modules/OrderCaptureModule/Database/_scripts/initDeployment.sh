#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR

aws dynamodb put-item --region us-east-1 --table-name UserInfo --item '{"userSub":{"S": "orderNo"}, "infoType":{"S": "seq"}, "data": {"S": "10000"}}'
