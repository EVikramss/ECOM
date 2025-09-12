#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"
cd $SCRIPT_DIR
cd ..

# clean target folder and run install to generate jar file
export PATH=/home/ec2-user/apache-maven-3.9.4/bin:$PATH
export MAVEN_OPTS="-Xmx512m"
mvn clean
mvn install