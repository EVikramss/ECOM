import json
import os
import urllib.request
import psycopg2
import datetime
import random
import boto3
import botocore

aws_session_token = os.environ.get('AWS_SESSION_TOKEN')

# get global db connection shared across invocations
dbConn = None


def lambda_handler(event, context):
    
    global dbConn
    if dbConn is None:
        dbConn = getDBConnection()
    cursor = dbConn.cursor()
    
    parentTypeName = event['parentTypeName']
    fieldName = event['fieldName']
    selectionSetList = event['selectionSetList']
    args = {}
    output = None

    # read input value
    if 'args' in event:
        args = event['args']
    elif 'variables' in event:
        vars = event['variables']
        inputVal = vars['input']
        args['orderNo'] = inputVal

    selectionSetList = [l for l in selectionSetList if not (l.startswith('__'))]
    
    if parentTypeName == 'Query':
        if fieldName == 'getOrder' and len(args) > 0:
            selectFields = ','.join(selectionSetList)
            filter = ' and '.join(f"{k}='{args[k]}'" for k in args)
            query = "select " + selectFields + " from OrderData where " + filter
                
            cursor.execute(query)
            result = cursor.fetchall()
            if len(result) > 0:
                formatted_row = []
                for value in result[0]:
                    if isinstance(value, datetime.datetime) or isinstance(value, datetime.date):
                        formatted_row.append(value.strftime('%Y-%m-%d %H:%M:%S'))
                        print(formatted_row)
                    else:
                        formatted_row.append(value)
                        
                output = {k: v for k, v in zip(selectionSetList, formatted_row)}
    
    return output
    
def getDBConnection():
    
    (userName, password, url, port) = getDBCreds()
    
    # connect to rds proxy
    connection = psycopg2.connect(user=userName, password=password, host=os.environ['DBPRX_EP'], port=port)
    return connection

def getDBCreds():    
    session = boto3.session.Session()
    client = session.client(
        service_name='secretsmanager',
        endpoint_url="https://secretsmanager.us-east-1.amazonaws.com",
        region_name='us-east-1'
    )
    response = client.get_secret_value(SecretId=os.environ['SECRET_ARN'])
    value = json.loads(response["SecretString"])
    
    userName = value["username"]
    password = value["password"]
    url = value["host"]
    port = value["port"]
    
    return (userName, password, url, port)

# get global db connection shared across invocations
dbConn = getDBConnection()