import json
import os
import urllib.request
import psycopg2
import datetime
import random
import boto3
import botocore

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
        supplyType = payload["SupplyType"]
        supplyDate = payload["SupplyDate"]
        override = payload["Override"]
        isPayloadValid = isValid(itemID, node, qty, supplyType, supplyDate, override, invResult)
    except:
        isPayloadValid = False
        invResult.recordError("Invalid attributes. Expected ItemID, Node, Qty, Id")
    
    # if valid process request
    if isPayloadValid:
        # get database connection & cursor
        cursor = dbConn.cursor()
        
        try:
            updateSupply(cursor, itemID, node, qty, supplyType, supplyDate, override, invResult)
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
        responseBody = {"UpdatedSupply": invResult.getSuccessMessage()}
        
    # add additional info to error
    additionalInfo = invResult.getAdditionalInfo()
    if additionalInfo:
        responseBody.update(additionalInfo)
        
    return {
        'statusCode': invResult.getStatusCode(),
        'body': json.dumps(responseBody)
    }


def updateSupply(cursor, itemID, node, qty, supplyType, supplyDate, override, invResult):
    # get item & node specific key
    nodeInventoryKey = getNodeInventoryKeyWithLock(cursor, itemID, node)
    
    if override == 'Y':
        if qty < 0:
            qty = 0

    if nodeInventoryKey is None:
        # insert node inventory key and supply values if not present
        nodeInventoryKey = insertNodeInventory(cursor, itemID, node)
        insertSupply(cursor, qty, supplyType, supplyDate, nodeInventoryKey)
        invResult.recordSuccessMessage(qty)
    else:
        (supplyQty, itemNodeSupplyKey) = getSupply(cursor, nodeInventoryKey)
        
        try:
            updatedQty = qty
            if supplyQty is None:
                insertSupply(cursor, updatedQty, supplyType, supplyDate, nodeInventoryKey)
            else:
                if override != 'Y':
                    updatedQty = supplyQty + qty    
                updateExistingSupply(cursor, updatedQty, itemNodeSupplyKey)
            invResult.recordSuccessMessage(updatedQty)
        except Exception as e:
            invResult.recordError(e)


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
    supplyQty = None
    itemNodeSupplyKey = None

    query = "select Quantity, ItemNodeSupplyKey from ItemNodeSupply where NodeInventoryKey='{}'".format(nodeInventoryKey)
    cursor.execute(query)
    
    results = cursor.fetchall()
    if len(results) > 0:
        supplyQty = results[0][0]
        itemNodeSupplyKey = results[0][1]
    
    return supplyQty, itemNodeSupplyKey

    
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

def insertNodeInventory(cursor, itemID, node):
    nodeInventoryKey = generate_custom_timestamp()
    itemInventoryKey = generate_custom_timestamp()
    query = "insert into NodeInventoryMap values ('{}', '{}', '{}', '{}')".format(itemID, node, itemInventoryKey, nodeInventoryKey)
    cursor.execute(query)
    
    return nodeInventoryKey
    
def insertSupply(cursor, qty, supplyType, supplyDate, nodeInventoryKey):
    
    itemNodeSupplyKey = generate_custom_timestamp()
    query = "insert into ItemNodeSupply values ('{}', '{}', '{}', '{}', '{}')".format(nodeInventoryKey, supplyType, qty, supplyDate, itemNodeSupplyKey)
    cursor.execute(query)
    
def updateExistingSupply(cursor, updatedQty, itemNodeSupplyKey):
    
    query = "update ItemNodeSupply set quantity='{}' where itemNodeSupplyKey='{}'".format(updatedQty, itemNodeSupplyKey)
    cursor.execute(query)

    
def isValid(itemID, node, qty, supplyType, supplyDate, override, invResult):
    is_itemID_valid = isinstance(itemID, str) and itemID.strip() != ""
    is_node_valid = isinstance(node, str) and node.strip() != ""
    is_qty_valid = isinstance(qty, (int, float)) and qty is not None
    is_supplyType_valid = isinstance(supplyType, str) and supplyType.strip() != ""
    is_supplyDate_valid = isinstance(supplyDate, str) and supplyDate.strip() != ""
    is_override_valid = isinstance(override, str) and override.strip() != ""
    
    isRequestValid = is_itemID_valid and is_node_valid and is_qty_valid and is_supplyType_valid and is_supplyDate_valid and is_override_valid
    if not(isRequestValid):
        invResult.recordError("Invalid attributes. Expected ItemID, Node, Qty, supplyType, supplyDate, is_override_valid (Y or N)")
    
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