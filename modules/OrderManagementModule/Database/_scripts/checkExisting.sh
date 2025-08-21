#!/bin/bash

DB_CLUSTER_NAME="ecomordmgmdb"

if aws rds describe-db-clusters --db-cluster-identifier "$DB_CLUSTER_NAME" --query "DBClusters[0].DBClusterIdentifier" --output text > /dev/null 2>&1; then
    echo "DB Cluster '$DB_CLUSTER_NAME' exists."
	exit 1
else
    echo "DB Cluster '$DB_CLUSTER_NAME' does not exist."
	exit 0
fi