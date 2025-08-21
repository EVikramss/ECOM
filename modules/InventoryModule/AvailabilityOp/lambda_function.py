import json
import os
import urllib.request
import psycopg2
import datetime
import random
import boto3
import botocore
from GetAvailability import lambda_handler as getAvailability
from ReserveAvailability import lambda_handler as reserveAvailability

aws_session_token = os.environ.get('AWS_SESSION_TOKEN')


def lambda_handler(event, context):
    
    # create result object and parse payload
    payload = parseEvent(event)
    output = None
    
    # fetch payload attributes and validate
    try:
        # get operation
        operation = payload["OP"]
        if operation == "GetAvailability":
            dbConn = getExstDBConnection()
            output = getAvailability(event, context, dbConn)            
        elif operation == "ReserveAvailability":
            dbConn = getExstDBConnection()
            output = reserveAvailability(event, context, dbConn)            
        else:
            raise Exception("OP should be either GetAvailability, ReserveAvailability")
    except Exception as e:
        output = {'statusCode': 400, 'body': json.dumps({"Error": str(e)})}
    
    return output

def getExstDBConnection():
    global dbConn
    if dbConn is None:
        dbConn = getDBConnection()
    return dbConn

def parseEvent(event):
    if "body" in event:
        return json.loads(event["body"])
    else:
        return event
        
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