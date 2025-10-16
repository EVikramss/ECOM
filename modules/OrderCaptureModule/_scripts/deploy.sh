#!/bin/bash

export SkuListMEM=$1
export SkuListCPU=$2
export OrderHistoryReservedConcurrency=$3
export OrderHistoryProvisionedConcurrency=$4
export initialSkuCount=$5

# create avl db.
. ./createAvlDB.sh

# deploy stack
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR
. ./checkForExistingResources.sh

cd $SCRIPT_DIR/../../../cdks/EcomOrdCptCDK
cdk bootstrap
cdk deploy --require-approval never