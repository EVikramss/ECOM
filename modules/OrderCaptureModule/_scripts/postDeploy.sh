#!/bin/bash

aws ecs update-service --cluster ECOMECSCluster --service SkuListService --desired-count 1