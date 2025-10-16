#!/bin/bash

SCRIPT_DIR="$(dirname "$(dirname "$(realpath "$0")")")"

# Loop over all directories in the modules directory
for dir in "$SCRIPT_DIR"/*/; do
    PRE_DEPLOY_SCRIPT="$dir/_scripts/preDeploy.sh"
    
    if [ -f "$PRE_DEPLOY_SCRIPT" ]; then
        echo "Running preDeploy script in: $dir"
        bash "$PRE_DEPLOY_SCRIPT"
    else
        echo "No preDeploy script found in: $dir"
    fi
done