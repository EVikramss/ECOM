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
ECREPSGID=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECREPSGID'].OutputValue" --output text)
ECSASGROLE=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECSASGROLE'].OutputValue" --output text)
ECSASGSG=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECSASGSG'].OutputValue" --output text)
ECSNMSPARN=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECSNMSPARN'].OutputValue" --output text)
ECSNMSPID=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECSNMSPID'].OutputValue" --output text)
ALBARN=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ALBARN'].OutputValue" --output text)
ALBSG=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ALBSG'].OutputValue" --output text)
INVDBSG=$(aws cloudformation describe-stacks --stack-name ECOMINV --query "Stacks[0].Outputs[?OutputKey=='DBPRXYSGID'].OutputValue" --output text)
INVDBEP=$(aws cloudformation describe-stacks --stack-name ECOMINV --query "Stacks[0].Outputs[?OutputKey=='DBPRXYEP'].OutputValue" --output text)
INVDBSECARN=$(aws cloudformation describe-stacks --stack-name ECOMINV --query "Stacks[0].Outputs[?OutputKey=='DBSECARN'].OutputValue" --output text)

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
export ECREPSGID
export ECSASGROLE
export INVAVLURL
export ECSASGSG
export ECSNMSPARN
export ECSNMSPID
export ALBARN
export ALBSG
export INVDBSG
export INVDBEP
export INVDBSECARN