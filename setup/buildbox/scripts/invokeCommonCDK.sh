#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR/../../../modules/CommonModule/_scripts
echo "Building Common Module"
./build.sh
echo "Deploy Common Module"
./deploy.sh