import json
import os
import urllib.request
import psycopg2
import datetime
import random
import boto3
import botocore

aws_session_token = os.environ.get('AWS_SESSION_TOKEN')


def lambda_handler(event, context, dbConn):
    
    # create result object and parse payload
    invResult = InvocationResult()
    payload = parseEvent(event)
    isPayloadValid = True
    
    # fetch payload attributes and validate
    try:
        itemID = payload["ItemID"]
        node = payload["Node"]
        isPayloadValid = isValid(itemID, node, invResult)
    except:
        isPayloadValid = False
        invResult.recordError("Invalid attributes. Expected ItemID, Node, Qty, Id")
    
    # if valid process request
    if isPayloadValid:
        # get database connection & cursor
        cursor = dbConn.cursor()
        
        try:
            calculateAvailability(cursor, itemID, node, invResult)
        finally:
            # close the cursor
            cursor.close()
    
    return generateResponse(invResult)

    
def generateResponse(invResult):

    responseBody = None
    
    if invResult.hasError():
        responseBody = {"Error": invResult.getErrorDesc()}
    else:
        responseBody = {"Availability": invResult.getSuccessMessage()}
        
    # add additional info to error
    additionalInfo = invResult.getAdditionalInfo()
    if additionalInfo:
        responseBody.update(additionalInfo)
        
    return {
        'statusCode': invResult.getStatusCode(),
        'body': json.dumps(responseBody)
    }


def calculateAvailability(cursor, itemID, node, invResult):
    # get item & node specific key
    nodeInventoryKey = getNodeInventoryKey(cursor, itemID, node)
        
    # if key not present, return 0 availability
    # else calculate
    if nodeInventoryKey is None:
        invResult.recordSuccessMessage(0)
    else: 
        supplyQty = getSupply(cursor, nodeInventoryKey)
        demandQty = getDemand(cursor, nodeInventoryKey)
        availableQty = supplyQty - demandQty
        invResult.recordSuccessMessage(availableQty)


def parseEvent(event):
    if "body" in event:
        return json.loads(event["body"])
    else:
        return event
    
def getNodeInventoryKey(cursor, itemID, node):
    nodeInventoryKey = None

    query = "select NodeInventoryKey from NodeInventoryMap where ItemID='{}' and ShipNode='{}'".format(itemID, node)
    cursor.execute(query)
    
    results = cursor.fetchall()
    if len(results) > 0:
        nodeInventoryKey = results[0][0]
    
    return nodeInventoryKey

    
def getSupply(cursor, nodeInventoryKey):
    supplyQty = 0

    query = "select Quantity from ItemNodeSupply where NodeInventoryKey='{}'".format(nodeInventoryKey)
    cursor.execute(query)
    
    results = cursor.fetchall()
    if len(results) > 0:
        supplyQty = results[0][0]
    
    return supplyQty

    
def getDemand(cursor, nodeInventoryKey):
    demandQty = 0
    useConsolidatedDemand = True

    if useConsolidatedDemand:
        query = "select Quantity from ItemNodeDemandConsolidated where NodeInventoryKey='{}'".format(nodeInventoryKey)
        cursor.execute(query)
        
        results = cursor.fetchall()
        if len(results) > 0:
            demandQty = results[0][0]
    else:
        query = "select Quantity from ItemNodeDemand where NodeInventoryKey='{}'".format(nodeInventoryKey)
        cursor.execute(query)
        
        results = cursor.fetchall()
        if len(results) > 0:
            for res in results:
                demandQty += res[0]
    
    return demandQty


def isValid(itemID, node, invResult):
    is_itemID_valid = isinstance(itemID, str) and itemID.strip() != ""
    is_node_valid = isinstance(node, str) and node.strip() != ""
    
    isRequestValid = is_itemID_valid and is_node_valid
    if not(isRequestValid):
        invResult.recordError("Invalid attributes. Expected ItemID, Node")
    
    return isRequestValid


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