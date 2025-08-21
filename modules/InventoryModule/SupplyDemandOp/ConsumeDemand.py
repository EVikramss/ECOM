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
            consumeDemandAndSupply(cursor, itemID, node, qty, demandID, invResult)
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
        responseBody = {"RemainingDemand": invResult.getSuccessMessage()}
        
    # add additional info to error
    additionalInfo = invResult.getAdditionalInfo()
    if additionalInfo:
        responseBody.update(additionalInfo)
        
    return {
        'statusCode': invResult.getStatusCode(),
        'body': json.dumps(responseBody)
    }

def consumeDemandAndSupply(cursor, itemID, node, qty, id, invResult):
    # get item & node specific key
    nodeInventoryKey = getNodeInventoryKeyWithLock(cursor, itemID, node)

    # if key not present, return 0 availability
    # else calculate
    if nodeInventoryKey is None:
        invResult.recordError("No inventory")
    else:
        # check if demand id already exists
        (existingID, demandQty, itemNodeDemandKey, consolDemandQty) = getDemandDetails(cursor, id, nodeInventoryKey)
        
        if existingID is None:
            invResult.recordError("Given demand id not found")
        else:
            supplyQty = getSupply(cursor, nodeInventoryKey)

            try:
                # reduce demand & supply both
                reducedQty = reduceDemand(cursor, qty, itemNodeDemandKey, consolDemandQty, demandQty, nodeInventoryKey)
                reduceSupply(cursor, supplyQty, reducedQty, nodeInventoryKey)
                
                invResult.recordSuccessMessage(demandQty - reducedQty)
                invResult.recordAdditionalInfo("Availability", supplyQty - reducedQty)
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
    supplyQty = 0

    query = "select Quantity from ItemNodeSupply where NodeInventoryKey='{}'".format(nodeInventoryKey)
    cursor.execute(query)
    
    results = cursor.fetchall()
    if len(results) > 0:
        supplyQty = results[0][0]
    
    return supplyQty

    
def getDemandDetails(cursor, demandID, nodeInventoryKey):
    demandQty = 0
    itemNodeDemandKey = None
    consolDemandQty = None
    existingDemandId = None

    query = "select DemandReference, Quantity, ItemNodeDemandKey from ItemNodeDemand where DemandReference='{}' and NodeInventoryKey='{}'".format(demandID, nodeInventoryKey)
    cursor.execute(query)
    
    results = cursor.fetchall()
    if len(results) > 0:
        existingDemandId = results[0][0]
        demandQty = results[0][1]
        itemNodeDemandKey = results[0][2]
        
    query = "select Quantity from ItemNodeDemandConsolidated where NodeInventoryKey='{}'".format(nodeInventoryKey)
    cursor.execute(query)
    
    results = cursor.fetchall()
    if len(results) > 0:
        consolDemandQty = results[0][0]
    
    return existingDemandId, demandQty, itemNodeDemandKey, consolDemandQty

    
def reduceDemand(cursor, qty, itemNodeDemandKey, consolDemandQty, demandQty, nodeInventoryKey):
    
    demandQuery = None
    
    if qty >= demandQty:
        demandQuery = "delete from ItemNodeDemand where ItemNodeDemandKey='{}'".format(itemNodeDemandKey)
        if qty > demandQty:
            qty = demandQty
    else:
        demandQuery = "update ItemNodeDemand set Quantity='{}' where ItemNodeDemandKey='{}'".format(demandQty - qty, itemNodeDemandKey)
    cursor.execute(demandQuery)
        
    consolidatedDemandQuery = "update ItemNodeDemandConsolidated set Quantity='{}' where NodeInventoryKey='{}'".format(consolDemandQty - qty, nodeInventoryKey)
    cursor.execute(consolidatedDemandQuery)
    
    return qty
    
def reduceSupply(cursor, supplyQty, qtyToReduce, nodeInventoryKey):
    
    updatedSupplyQty = supplyQty - qtyToReduce
    if updatedSupplyQty < 0:
        updatedSupplyQty = 0
    
    query = "update ItemNodeSupply set Quantity='{}' where NodeInventoryKey='{}'".format(updatedSupplyQty, nodeInventoryKey)
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