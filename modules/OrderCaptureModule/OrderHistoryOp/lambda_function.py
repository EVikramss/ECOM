import json
import os
import boto3
import zlib
import base64

dynamodb = boto3.resource('dynamodb')
table_name = os.environ['TABLE_NAME']
table = dynamodb.Table(table_name)

def lambda_handler(event, context):
    # Extract SNS message
    message = event['Records'][0]['Sns']['Message']
    sub = message['sub']
    order_no = message['orderNo']
    item_data = message['itemData']
    
    # Prepare new entry
    new_entry = {
        order_no: [{'sku': item['sku'], 'qty': item['qty'], 'status': item['status']} for item in item_data]
    }
    
    # Query DynamoDB
    key = {'userSub': sub, 'infoType': 'history'}
    response = table.get_item(Key=key)
    
    if 'Item' not in response:
        # No existing record, insert new
        compressed_data = compress(new_entry)
        table.put_item(Item={
            'userSub': sub,
            'infoType': 'history',
            'data': compressed_data
        })
    else:
        # Existing record found
        existing_data = decompress(response['Item']['data'])

        existing_data.update(new_entry)
        compressed_data = compress(existing_data)
        table.update_item(
            Key=key,
            UpdateExpression='SET #d = :val',
            ExpressionAttributeNames={'#d': 'data'},
            ExpressionAttributeValues={':val': compressed_data}
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