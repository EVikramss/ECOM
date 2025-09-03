#!/bin/bash

# check for existing VPC ID
VPCID=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='VPCID'].OutputValue" --output text)
SMEPID=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='SMEPID'].OutputValue" --output text)
SMSGID=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='SMSGID'].OutputValue" --output text)
ECOMBKTARN=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECOMBKTARN'].OutputValue" --output text)
RunDDLFUNCID=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='RunDDLFUNCID'].OutputValue" --output text)
RunDDLSGID=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='RunDDLSGID'].OutputValue" --output text)
RunDDLFUNCRLID=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='RunDDLFUNCRLID'].OutputValue" --output text)
ECSARN=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECSARN'].OutputValue" --output text)
ECSROLE=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECSROLE'].OutputValue" --output text)
ECRREPO=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECRREPO'].OutputValue" --output text)

export VPCID
export SMEPID
export SMSGID
export ECOMBKTARN
export RunDDLFUNCID
export RunDDLSGID
export RunDDLFUNCRLID
export ECSARN
export ECSROLE
export ECRREPO