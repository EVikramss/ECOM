#!/bin/bash

export CreateOrderResvConcurrency=$1
export CreateOrderProvConcurrency=$2

# deploy stack
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR
. ./checkForExistingResources.sh

cd $SCRIPT_DIR/../../../cdks/EcomOrdMgmCDK
cdk bootstrap
cdk deploy --require-approval never