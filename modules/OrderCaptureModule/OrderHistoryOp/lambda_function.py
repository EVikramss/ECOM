import json
import os
import boto3
import zlib
import base64
from boto3.dynamodb.types import TypeDeserializer, TypeSerializer

def lambda_handler(event, context):
    dynamo = boto3.client('dynamodb')
    table_name = os.environ['TABLE_NAME']
    deserializer = TypeDeserializer()
    serializer = TypeSerializer()

    # Extract SNS message
    message = json.loads(event['Records'][0]['Sns']['Message'])
    sub = message['customerContact']['sub']
    order_no = message['orderNo']
    item_data = message['itemData']

    # Prepare new entry
    new_entry = {
        order_no: [{'sku': item['sku'], 'qty': item['qty'], 'status': item['status']} for item in item_data]
    }

    key = {
        'userSub': serializer.serialize(sub),
        'infoType': serializer.serialize('history')
    }

    response = dynamo.get_item(TableName=table_name, Key=key)

    if 'Item' not in response:
        # No existing record, insert new
        compressed_data = compress(new_entry)
        item = {
            'userSub': serializer.serialize(sub),
            'infoType': serializer.serialize('history'),
            'data': serializer.serialize(compressed_data)
        }
        dynamo.put_item(TableName=table_name, Item=item)
    else:
        # Existing record found
        existing_data = decompress(deserializer.deserialize(response['Item']['data']))
        existing_data.update(new_entry)
        compressed_data = compress(existing_data)

        dynamo.update_item(
            TableName=table_name,
            Key=key,
            UpdateExpression='SET #d = :val',
            ExpressionAttributeNames={'#d': 'data'},
            ExpressionAttributeValues={':val': serializer.serialize(compressed_data)}
        )

    return {
        'statusCode': 200,
        'body': json.dumps('Processed successfully')
    }

def compress(jsonData):
    json_bytes = json.dumps(jsonData).encode('utf-8')
    compressed = zlib.compress(json_bytes)
    base64_str = base64.b64encode(compressed).decode('utf-8')
    return base64_str

def decompress(base64_str):
    compressed = base64.b64decode(base64_str)
    decompressed_bytes = zlib.decompress(compressed)
    return json.loads(decompressed_bytes.decode('utf-8'))