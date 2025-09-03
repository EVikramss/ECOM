#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
cd $SCRIPT_DIR
cd ..

jobName="ScheduleOrder"
ECRREPO=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECRREPO'].OutputValue" --output text)

# docker build image & push to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin "${ECRREPO}"
docker build -t "${jobName}" -f Dockerfile .
docker tag "${jobName}" "${ECRREPO}:${jobName}"
docker push "${ECRREPO}:${jobName}"