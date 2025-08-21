#!/bin/bash

# get client details from stack
albURL=$(aws cloudformation describe-stacks --stack-name BuildBoxStack --query "Stacks[0].Outputs[?OutputKey=='ALBURL'].OutputValue" --output text)

echo "Login to:"
echo "$albURL"
echo "userName: admin"
echo "password: Password@123"
