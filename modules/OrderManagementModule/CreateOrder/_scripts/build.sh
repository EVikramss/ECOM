#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
cd $SCRIPT_DIR

cp -rf ../../../_lib/psycopg2 ../
cp -rf ../../../_lib/psycopg2_binary.libs ../
cp -rf ../../../_lib/psycopg2_binary-2.9.9.dist-info ../

cd ../../CreateOrder
zip -r ../CreateOrder.zip . -x '*scripts*'