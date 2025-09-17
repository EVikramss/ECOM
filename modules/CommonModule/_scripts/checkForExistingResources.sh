#!/bin/bash

# check for existing VPC ID
USERVPCID=$(aws cloudformation describe-stacks --stack-name BuildBoxStack --query "Stacks[0].Outputs[?OutputKey=='BUILDBOXVPCID'].OutputValue" --output text)

export USERVPCID
