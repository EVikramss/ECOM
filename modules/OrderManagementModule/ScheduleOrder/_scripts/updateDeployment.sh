#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
cd $SCRIPT_DIR

. ./preDeploy.sh

aws ecs update-service --cluster ECOMECSCluster --service ScheduleOrderService --force-new-deployment