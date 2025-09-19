#!/bin/bash

aws ecs update-service --cluster ECOMECSCluster --service CreateOrderService --desired-count 1  

aws ecs update-service --cluster ECOMECSCluster --service ShipOrderService --desired-count 1 

aws ecs update-service --cluster ECOMECSCluster --service GetDataService --desired-count 1

aws ecs update-service --cluster ECOMECSCluster --service ScheduleOrderService --desired-count 1