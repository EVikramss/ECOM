import json
import os
import boto3

dynamodb = boto3.resource('dynamodb')
table_name = os.environ['TABLE_NAME']
table = dynamodb.Table(table_name)

def lambda_handler(event, context):
    # Extract SNS message
    sns_message = event['Records'][0]['Sns']['Message']
    message = json.loads(sns_message)

    key = message.get('key')
    info = message.get('info')

    if not key or not info:
        return {
            'statusCode': 400,
            'body': 'Missing key or info in SNS message'
        }

    # Read item from DynamoDB
    response = table.get_item(Key={'key': key})
    item = response.get('Item')

    if not item:
        return {
            'statusCode': 404,
            'body': f'Item with key {key} not found'
        }

    # Modify the 'data' attribute
    data = item.get('data', '')
    modified_data = f"{data} | updated with info: {info}"

    # Write back to DynamoDB
    table.update_item(
        Key={'key': key},
        UpdateExpression='SET #d = :val1',
        ExpressionAttributeNames={'#d': 'data'},
        ExpressionAttributeValues={':val1': modified_data}
    )

    return {
        'statusCode': 200,
        'body': f'Item with key {key} updated successfully'
    }