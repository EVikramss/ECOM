from datetime import datetime, timezone
import random

class Address:
    
    country = None
    city = None
    state = None
    addressLine1 = None
    addressLine2 = None
    addressKey = None
    orderNo = None
    
    isPayloadValid = True
    errorDescList = []
    
    def __init__(self, payload, orderNo):
        try:
            self.orderNo = orderNo
            self.parseAddress(payload)
        except Exception as e:
            self.markError(str(e))
        
    def getQueries(self):
        queries = []
        queries.append(f"INSERT INTO OrderAddress VALUES ('{self.orderNo}','{self.country}','{self.city}','{self.state}','{self.addressLine1}','{self.addressLine2}','{self.addressKey}')")
        return queries
    
    def isValid(self):
        return self.isPayloadValid, self.errorDescList
    
    def parseAddress(self, payload):
        self.validateAddress(payload)
        
        self.country = payload["country"]
        self.city = payload["city"]
        self.state = payload["state"]
        self.addressLine1 = payload["addressLine1"]
        self.addressLine2 = payload["addressLine2"]
        self.addressKey = self.generateKey()
        
    def generateKey(self):
        now = datetime.now(timezone.utc)
        timestamp = now.strftime("%Y%m%d%H%M%S")
        random_number = random.randint(100000, 999999)
        return f"{timestamp}{random_number}"
    
    def validateAddress(self, payload):
        if "country" not in payload:
            self.errorDescList.append("Address: country is blank")
            self.isPayloadValid = False
        
        if "city" not in payload:
            self.errorDescList.append("Address: city is blank")
            self.isPayloadValid = False
            
        if "state" not in payload:
            self.errorDescList.append("Address: state is blank")
            self.isPayloadValid = False
            
        if "addressLine1" not in payload:
            self.errorDescList.append("Address: addressLine1 is blank")
            self.isPayloadValid = False
            
        if "addressLine2" not in payload:
            self.errorDescList.append("Address: addressLine2 is blank")
            self.isPayloadValid = False
        