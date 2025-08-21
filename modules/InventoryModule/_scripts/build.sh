#!/bin/bash

SCRIPT_DIR="$(dirname "$(dirname "$(realpath "$0")")")"

# Loop over all directories in the modules directory
for dir in "$SCRIPT_DIR"/*/; do
    BUILD_SCRIPT="$dir/_scripts/build.sh"
    
    if [ -f "$BUILD_SCRIPT" ]; then
        echo "Running build script in: $dir"
        bash "$BUILD_SCRIPT"
    else
        echo "No build script found in: $dir"
    fi
done
