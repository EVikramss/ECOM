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
        qty = payload["Qty"]
        demandID = payload["Id"]
        isPayloadValid = isValid(itemID, node, qty, demandID, invResult)
    except:
        isPayloadValid = False
        invResult.recordError("Invalid attributes. Expected ItemID, Node, Qty, Id")
    
    # if valid process request
    if isPayloadValid:
        # get database connection & cursor
        cursor = dbConn.cursor()
        
        try:
            reserveInventory(cursor, itemID, node, qty, demandID, invResult)
        finally:
            # close the cursor
            cursor.close()
            
            # commit or roll back the connection
            if invResult.hasError():
                dbConn.rollback()
            else:
                dbConn.commit()
    
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


def reserveInventory(cursor, itemID, node, qty, id, invResult):
    # get item & node specific key
    nodeInventoryKey = getNodeInventoryKeyWithLock(cursor, itemID, node)

    # if key not present, return 0 availability
    # else calculate
    if nodeInventoryKey is None:
        invResult.recordError("No inventory")
    else:
        supplyQty = getSupply(cursor, nodeInventoryKey)
        demandQty, consolDemandQty = getDemand(cursor, nodeInventoryKey)
        availableQty = supplyQty - demandQty
        
        if availableQty >= qty:
            # check if demand id already exists
            existingID = getDemandID(cursor, id, nodeInventoryKey)
            
            if existingID is None:
                try:
                    # create demand entry
                    insertDemand(cursor, qty, id, nodeInventoryKey, consolDemandQty)
                    invResult.recordSuccessMessage(availableQty - qty)
                except Exception as e:
                    invResult.recordError(e)
            else:
                invResult.recordError("Id already exists")
        else:
            invResult.recordError("No inventory")
            invResult.recordAdditionalInfo("Availability", availableQty)


def parseEvent(event):
    if "body" in event:
        return json.loads(event["body"])
    else:
        return event 
    
def getNodeInventoryKeyWithLock(cursor, itemID, node):
    nodeInventoryKey = None

    query = "select NodeInventoryKey from NodeInventoryMap where ItemID='{}' and ShipNode='{}' for update".format(itemID, node)
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
    consolDemandQty = None

    query = "select Quantity from ItemNodeDemand where NodeInventoryKey='{}'".format(nodeInventoryKey)
    cursor.execute(query)
    
    results = cursor.fetchall()
    if len(results) > 0:
        for res in results:
            demandQty += res[0]
       
    # Also lock the demand consolidation table for updating later on
    query = "select Quantity from ItemNodeDemandConsolidated where NodeInventoryKey='{}'".format(nodeInventoryKey)
    cursor.execute(query)
    
    results = cursor.fetchall()
    if len(results) > 0:
        consolDemandQty = results[0][0]
    
    return demandQty, consolDemandQty

    
def getDemandID(cursor, id, nodeInventoryKey):
    demandID = None

    query = "select DemandReference from ItemNodeDemand where DemandReference='{}' and NodeInventoryKey='{}'".format(id, nodeInventoryKey)
    cursor.execute(query)
    
    results = cursor.fetchall()
    if len(results) > 0:
        demandID = results[0][0]
    
    return demandID

    
def insertDemand(cursor, qty, id, nodeInventoryKey, consolDemandQty):
    itemNodeDemandKey = generate_custom_timestamp()
    
    query = "insert into ItemNodeDemand values ('{}', 'CURRENT', '{}', null, '{}', null, '{}')".format(nodeInventoryKey, qty, id, itemNodeDemandKey)
    cursor.execute(query)
    
    if consolDemandQty is None:
        # insert new record
        itemNodeDemandConsolKey = generate_custom_timestamp()
        query = "insert into ItemNodeDemandConsolidated values ('{}', '{}', '{}')".format(nodeInventoryKey, qty, itemNodeDemandConsolKey)
    else:
        # update existing record
        updatedQty = consolDemandQty + qty
        query = "update ItemNodeDemandConsolidated set Quantity='{}' where NodeInventoryKey='{}'".format(updatedQty, nodeInventoryKey)
    cursor.execute(query)
    
def isValid(itemID, node, qty, demandId, invResult):
    is_itemID_valid = isinstance(itemID, str) and itemID.strip() != ""
    is_node_valid = isinstance(node, str) and node.strip() != ""
    is_qty_valid = isinstance(qty, (int, float)) and qty is not None
    is_id_valid = isinstance(demandId, str) and demandId.strip() != ""
    
    isRequestValid = is_itemID_valid and is_node_valid and is_qty_valid and is_id_valid
    if not(isRequestValid):
        invResult.recordError("Invalid attributes. Expected ItemID, Node, Qty, Id")
    
    return isRequestValid


def generate_custom_timestamp():
    now = datetime.datetime.now()
    random_number = random.randint(10000, 99999)
    timestamp = now.strftime("%Y%m%d%H%M%S") + str(random_number)
    return timestamp


# class to hold invocation results
class InvocationResult:
    error = False
    errorDesc = None
    statusCode = 200
    successMessage = None
    additionalInfo = {}
    
    def __init__(self):
        pass
        
    def recordError(self, errorDesc, statusCode = 400):
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