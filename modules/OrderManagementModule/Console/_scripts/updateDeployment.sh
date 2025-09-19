#!/bin/bash

SCRIPT_DIR="$(dirname "$(dirname "$(realpath "$0")")")"

# Loop over all directories in the modules directory
for dir in "$SCRIPT_DIR"/*/; do
    UPDATE_SCRIPT="$dir/_scripts/updateDeployment.sh"
    
    if [ -f "$UPDATE_SCRIPT" ]; then
        echo "Running updateDeployment script in: $dir"
        bash "$UPDATE_SCRIPT"
    else
        echo "No updateDeployment script found in: $dir"
    fi
done