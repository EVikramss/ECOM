import json
import os
import urllib.request
import psycopg2
import datetime
import random
import boto3
import botocore
from Order import Order

aws_session_token = os.environ.get('AWS_SESSION_TOKEN')


def lambda_handler(event, context):
    
    # create result object and parse payload
    invResult = InvocationResult()
    payload = parseEvent(event)
    isPayloadValid = True
    order = None
    errArr = None
    
    # fetch payload attributes and validate
    try:
        order = Order(payload)
        isPayloadValid, errArr = order.isValid()
    except Exception as e:
        isPayloadValid = False
        invResult.recordError(str(e))
    
    # if valid process request
    if isPayloadValid:
        # get database connection & cursor
        global dbConn
        if dbConn is None:
            dbConn = getDBConnection()
        cursor = dbConn.cursor()
        
        try:
            runQueries(cursor, order, invResult)
        finally:
            # close the cursor
            cursor.close()
            
            # commit or roll back the connection
            if invResult.hasError():
                dbConn.rollback()
            else:
                dbConn.commit()
    else:
        if errArr is not None:
            invResult.recordError(''.join(f"{e} " for e in errArr))
    
    return generateResponse(invResult)

    
def generateResponse(invResult):
    
    responseBody = None
    
    if invResult.hasError():
        responseBody = {"Error": invResult.getErrorDesc()}
    else:
        responseBody = {"Success": invResult.getSuccessMessage()}
        
    # add additional info to error
    additionalInfo = invResult.getAdditionalInfo()
    if additionalInfo:
        responseBody.update(additionalInfo)
        
    return {
        'statusCode': invResult.getStatusCode(),
        'body': json.dumps(responseBody)
    }


def runQueries(cursor, order, invResult):
    try:
        for query in order.getQueries():
            cursor.execute(query)
        invResult.recordSuccessMessage(order.orderNo)
    except Exception as e:
        invResult.recordError(str(e))

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

# class to hold invocation results
class InvocationResult:
    error = False
    errorDesc = None
    statusCode = 200
    successMessage = None
    additionalInfo = {}
    
    def __init__(self):
        pass
        
    def recordError(self, errorDesc, statusCode=400):
        self.statusCode = statusCode
        self.error = True
        self.errorDesc = errorDesc
        
    def recordSuccessMessage(self, successMessage):
        self.successMessage = successMessage
        
    def hasError(self):
        return self.error
        
    def getStatusCode(self):
        return self.statusCode
        
    def getErrorDesc(self):
        return self.errorDesc
        
    def getSuccessMessage(self):
        return self.successMessage
        
    def recordAdditionalInfo(self, key, value):
        self.additionalInfo[key] = value
        
    def getAdditionalInfo(self):
        return self.additionalInfo


# get global db connection shared across invocations
dbConn = getDBConnection()