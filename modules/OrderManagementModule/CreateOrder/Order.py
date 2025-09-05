from OrderItem import OrderItem
from Address import Address
from CustomerContact import CustomerContact
from datetime import datetime, timezone
import random

class Order:
    
    orderNo = None
    orderEntity = None
    orderDate = None
    orderItems = []
    shippingAddress = None
    customerContact = None
    isPayloadValid = True
    errorDescList = []
    
    def __init__(self, payload):
        try:
            self.orderItems = []
            self.errorDescList = []
            self.parseOrder(payload)
        except Exception as e:
            self.markError(str(e))
            
    def getQueries(self):
        queries = []
        queries.append(f"INSERT INTO OrderData VALUES ('{self.orderNo}',TO_DATE('{self.orderDate}','YYYY-MM-DD HH24:MI:SS'),'{self.orderEntity}')")
        for orderItem in self.orderItems:
            queries = queries + orderItem.getQueries()
        queries = queries + self.shippingAddress.getQueries()
        queries = queries + self.customerContact.getQueries()
        
        orderStatusKey = self.generateKey()
        status = "0" # 0 for created
        queries.append(f"INSERT INTO OrderStatus VALUES ('{status}','{self.orderNo}','{orderStatusKey}')")
        return queries
    
    # on validation return final result and each error
    def isValid(self):
        return self.isPayloadValid, self.errorDescList
    
    # validate and parse order data
    def parseOrder(self, payload):
        self.validateOrderData(payload)
        
        if self.isPayloadValid:
            self.orderNo = payload["OrderNo"]
            self.orderEntity = payload["Entity"]
            
            counter = 1
            now = datetime.now(timezone.utc)
            timestamp = now.strftime("%Y%m%d%H%M%S")
            random_number = random.randint(100000, 999999)
        
            try:
                orderItemsData = payload["OrderItems"]
                for itemData in orderItemsData:
                    orderItem = OrderItem(itemData, self.orderNo, counter, f"{timestamp}{random_number + counter}")
                    isItemValid, errorDesc = orderItem.isValid()
                    counter = counter + 1
                    
                    if isItemValid:
                        self.orderItems.append(orderItem)
                    else:
                        self.markError(errorDesc)
                        
                shippingAddressData = payload["ShippingAddress"]
                self.shippingAddress = Address(shippingAddressData, self.orderNo)
                isItemValid, errorDesc = self.shippingAddress.isValid()
                if not(isItemValid):
                    self.markError(errorDesc)
                    
                customerContactData = payload["CustomerContact"]
                self.customerContact = CustomerContact(customerContactData, self.orderNo)
                isItemValid, errorDesc = self.customerContact.isValid()
                if not(isItemValid):
                    self.markError(errorDesc)
                
                self.orderDate = now.strftime("%Y-%m-%d %H:%M:%S")
            except Exception as e:
                self.markError(str(e))
        
    def markError(self, errorDesc):
        if type(errorDesc) == list:
            self.errorDescList = self.errorDescList + errorDesc
        else:
            self.errorDescList.append(errorDesc)
        self.isPayloadValid = False;
    
    def generateKey(self):
        now = datetime.now(timezone.utc)
        timestamp = now.strftime("%Y%m%d%H%M%S")
        random_number = random.randint(100000, 999999)
        return f"{timestamp}{random_number}"
    
    def validateOrderData(self, payload):
        if "OrderNo" not in payload:
            self.errorDescList.append("Order: OrderNo is blank")
            self.isPayloadValid = False
        
        if "Entity" not in payload:
            self.errorDescList.append("Order: Entity is blank")
            self.isPayloadValid = False
            
        if "OrderItems" not in payload:
            self.errorDescList.append("Order: OrderItems is blank")
            self.isPayloadValid = False
        else:
            orderItemsData = payload["OrderItems"]
            if len(orderItemsData) == 0:
                self.errorDescList.append("Order: OrderItems is blank")
                self.isPayloadValid = False
                
        if "ShippingAddress" not in payload:
            self.errorDescList.append("Order: ShippingAddress is blank")
            self.isPayloadValid = False
            
        if "CustomerContact" not in payload:
            self.errorDescList.append("Order: CustomerContact is blank")
            self.isPayloadValid = False
        