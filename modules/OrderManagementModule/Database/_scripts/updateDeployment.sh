#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd $SCRIPT_DIR

aws lambda invoke --function-name RunDDLQuery --payload file://updateQueries.json response.json
