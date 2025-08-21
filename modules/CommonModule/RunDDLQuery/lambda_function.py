import json
import os
import urllib.request
import psycopg2
import datetime
import random
import boto3
import botocore

aws_session_token = os.environ.get('AWS_SESSION_TOKEN')

def lambda_handler(event, context):
    
    # create result object and parse payload
    invResult = InvocationResult()
    payload = parseEvent(event)
    isPayloadValid = True
    
    # fetch payload attributes and validate
    try:
        queries = payload["Queries"]
        secretARN = payload["SecretARN"]
    except:
        isPayloadValid = False
        invResult.recordError("Invalid attributes. Expected Queries, EP, SecretARN")
    
    # if valid process request
    if isPayloadValid:
        # get database connection & cursor
        dbConn = getDBConnection(secretARN) 
        cursor = dbConn.cursor()
        
        try:
            runQueries(cursor, queries, invResult)
        finally:
            # close the cursor
            cursor.close()
            
            # commit or roll back the connection
            if invResult.hasError():
                dbConn.rollback()
            else:
                dbConn.commit()
            dbConn.close()
    
    return generateResponse(invResult)

    
def generateResponse(invResult):

    responseBody = None
    
    if invResult.hasError():
        responseBody = {"Error": invResult.getErrorDesc()}
    else:
        responseBody = {"Result": "success"}
        
    # add additional info to error
    additionalInfo = invResult.getAdditionalInfo()
    if additionalInfo:
        responseBody.update(additionalInfo)
        
    return {
        'statusCode': invResult.getStatusCode(),
        'body': json.dumps(responseBody)
    }

def parseEvent(event):
    if "body" in event:
        return json.loads(event["body"])
    else:
        return event

    
def getDBConnection(secretARN):
    
    (userName, password, url, port) = getDBCreds(secretARN)
    
    # connect to rds proxy
    connection = psycopg2.connect(user=userName, password=password, host=url, port=port)
    return connection

def getDBCreds(secretARN):    
    session = boto3.session.Session()
    client = session.client(
        service_name='secretsmanager',
        endpoint_url="https://secretsmanager.us-east-1.amazonaws.com",
        region_name='us-east-1'
    )
    response = client.get_secret_value(SecretId=secretARN)
    value = json.loads(response["SecretString"])
    
    userName = value["username"]
    password = value["password"]
    url = value["host"]
    port = value["port"]
    
    return (userName, password, url, port)
    
def runQueries(cursor, queries, invResult):
    for query in queries:
        cursor.execute(query)

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