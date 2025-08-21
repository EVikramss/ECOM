#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR

DBSECARN=$(aws cloudformation describe-stacks --stack-name ECOMORDMGM --query "Stacks[0].Outputs[?OutputKey=='DBSECARN'].OutputValue" --output text)
sed -i "s#SECRET_ARN#${DBSECARN}#g" initQueries.json

aws lambda invoke --function-name RunDDLQuery --payload fileb://initQueries.json response.json
