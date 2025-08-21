#!/bin/bash

export SupplyDemandOpResvConcurrency=$1
export SupplyDemandOpProvConcurrency=$2
export AvailabilityOpResvConcurrency=$3
export AvailabilityOpProvConcurrency=$4

# deploy stack
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR
. ./checkForExistingResources.sh

cd $SCRIPT_DIR/../../../cdks/EcomInvCDK
cdk bootstrap
cdk deploy --require-approval never