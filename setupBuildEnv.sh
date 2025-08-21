#!/bin/bash

export MAIN_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $MAIN_SCRIPT_DIR/setup/buildbox/scripts

# setup env
. ./setupACMCerts.sh
#. ./createEC2Role.sh

# invoke cdk
. ./invokeBuildBoxCDK.sh

cd $MAIN_SCRIPT_DIR/setup/buildbox/scripts
. ./invokeCommonCDK.sh

# cognito actions
cd $MAIN_SCRIPT_DIR/setup/buildbox/scripts
. ./cognitoPostProcess.sh

# finish setup
. ./finishBuildBoxSetup.sh
