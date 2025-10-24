#!/bin/bash

# To be removed once s3 inventory load is conplete
aws lambda invoke --function-name ECOMINVSupplyDemandOp --payload '{"OP":"UpdateSupply","ItemID":"sku0","Node":"node1","Qty":10,"SupplyType":"Current","SupplyDate":"2025-10-24","Override":"Y"}' --cli-binary-format raw-in-base64-out response.json 

aws lambda invoke --function-name ECOMINVSupplyDemandOp --payload '{"OP":"UpdateSupply","ItemID":"sku1","Node":"node1","Qty":10,"SupplyType":"Current","SupplyDate":"2025-10-24","Override":"Y"}' --cli-binary-format raw-in-base64-out response.json 

aws lambda invoke --function-name ECOMINVSupplyDemandOp --payload '{"OP":"UpdateSupply","ItemID":"sku2","Node":"node1","Qty":10,"SupplyType":"Current","SupplyDate":"2025-10-24","Override":"Y"}' --cli-binary-format raw-in-base64-out response.json