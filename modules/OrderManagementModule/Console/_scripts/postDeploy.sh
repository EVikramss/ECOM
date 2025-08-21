#!/bin/bash

SCRIPT_DIR="$(dirname "$(dirname "$(realpath "$0")")")"

# Loop over all directories in the modules directory
for dir in "$SCRIPT_DIR"/*/; do
    POST_DEPLOY_SCRIPT="$dir/_scripts/postDeploy.sh"
    
    if [ -f "$POST_DEPLOY_SCRIPT" ]; then
        echo "Running postDeploy script in: $dir"
        bash "$POST_DEPLOY_SCRIPT"
    else
        echo "No postDeploy script found in: $dir"
    fi
done