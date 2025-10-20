#!/bin/bash

jobName="skulist"

if aws ecr describe-images --repository-name ecomrepo --image-ids imageTag="$jobName" --region us-east-1 > /dev/null 2>&1; then
    echo "Image '$jobName' exists."
	exit 1
else
    echo "Image '$jobName' does not exist."
	exit 0
fi