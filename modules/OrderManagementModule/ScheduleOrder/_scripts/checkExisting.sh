#!/bin/bash

jobName="ScheduleOrder"
ECRREPO=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECRREPO'].OutputValue" --output text)

if aws ecr describe-images --repository-name ecomrepo --image-ids imageTag="$jobName" --region us-east-1 > /dev/null 2>&1; then
    echo "Image '$jobName' exists."
	exit 1
else
    echo "Image '$jobName' does not exist."
	exit 0
fi