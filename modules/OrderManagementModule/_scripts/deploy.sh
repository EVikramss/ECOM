#!/bin/bash

export CreateOrderMEM=$1
export CreateOrderCPU=$2
export ScheduleOrderMEM=$3
export ScheduleOrderCPU=$4
export ShipOrderMEM=$5
export ShipOrderCPU=$6
export GetDataMEM=$7
export GetDataCPU=$8

# deploy stack
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR
. ./checkForExistingResources.sh

cd $SCRIPT_DIR/../../../cdks/EcomOrdMgmCDK
cdk bootstrap
cdk deploy --require-approval never