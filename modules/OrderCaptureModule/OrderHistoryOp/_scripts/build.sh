#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
cd $SCRIPT_DIR

cd ../../OrderHistoryOp
zip -r ../OrderHistoryOp.zip . -x '*scripts*'