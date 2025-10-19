#!/bin/bash

SCRIPT_DIR="$(dirname "$(realpath "$0")")"

ECOMBKTNAME=$(aws cloudformation describe-stacks --stack-name COMMON --query "Stacks[0].Outputs[?OutputKey=='ECOMBKTNAME'].OutputValue" --output text)

if aws dynamodb describe-table --table-name ItemInfo --region us-east-1 > /dev/null 2>&1; then
    echo "Table ItemInfo exists."
else
	echo "Generating info for item count '$initialSkuCount'"
	cd "${SCRIPT_DIR}/../../../utils/IndexBuild"
	mvn install
	mvn exec:java -Dexec.mainClass="com.data.generate.GenerateItemData" -Dexec.args="100"
	mvn exec:java -Dexec.mainClass="com.data.generate.GenerateItemAvailability"
	mvn exec:java -Dexec.mainClass="com.index.create.CreateItemDataIndex"
	
	cd index
	zip -r ../index.zip ./*
	cd ..
	
	aws s3 cp itemInfo.gz s3://"$ECOMBKTNAME"/items/info/itemInfo.gz
	aws s3 cp itemSply.gz s3://"$ECOMBKTNAME"/items/supply/itemSply.gz
	aws s3 cp index.zip s3://"$ECOMBKTNAME"/items/index/index.zip
	
    echo "Creating ItemInfo for item count '$initialSkuCount'"
	
	aws dynamodb import-table --s3-bucket-source S3Bucket="${ECOMBKTNAME}",S3KeyPrefix="/items/info/" --input-format CSV --table-creation-parameters '{"TableName":"ItemInfo","KeySchema": [{"AttributeName":"itemID","KeyType":"HASH"},{"AttributeName":"infoType","KeyType":"RANGE"}],"AttributeDefinitions":[{"AttributeName":"itemID","AttributeType":"S"},{"AttributeName":"infoType","AttributeType":"S"}],"BillingMode":"PAY_PER_REQUEST"}' --input-format-options '{"Csv": {"HeaderList": ["itemID", "infoType", "data", "mov"], "Delimiter": ","}}' --input-compression-type GZIP
	
	cd "$SCRIPT_DIR"
fi

