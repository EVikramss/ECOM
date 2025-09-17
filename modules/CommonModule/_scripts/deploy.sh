#!/bin/bash

# deploy stack
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR
. ./checkForExistingResources.sh

cd $SCRIPT_DIR/../../../cdks/CommonCDK
cdk bootstrap
cdk deploy --require-approval never