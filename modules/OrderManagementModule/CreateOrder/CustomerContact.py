from datetime import datetime, timezone
import random

class CustomerContact:
    
    salutation = ""
    firstName = None
    lastName = None
    phone = None
    email = None
    customerKey = None
    orderNo = None
    
    isPayloadValid = True
    errorDescList = []
    
    def __init__(self, payload, orderNo):
        try:
            self.orderNo = orderNo
            self.parseContactDetails(payload)
        except Exception as e:
            self.markError(str(e))
            
    def getQueries(self):
        queries = []
        queries.append(f"INSERT INTO CustomerContact VALUES ('{self.orderNo}','{self.salutation}','{self.firstName}','{self.lastName}','{self.phone}','{self.email}','{self.customerKey}')")
        return queries
    
    def generateKey(self):
        now = datetime.now(timezone.utc)
        timestamp = now.strftime("%Y%m%d%H%M%S")
        random_number = random.randint(100000, 999999)
        return f"{timestamp}{random_number}"
    
    def isValid(self):
        return self.isPayloadValid, self.errorDescList
        
    def parseContactDetails(self, payload):
        self.validateContactDetails(payload)
        
        if "salutation" in payload: 
            self.salutation = payload["salutation"]
        self.firstName = payload["firstName"]
        self.lastName = payload["lastName"]
        self.phone = payload["phone"]
        self.email = payload["email"]
        self.customerKey = self.generateKey()
        
    def validateContactDetails(self, payload):
        if "firstName" not in payload:
            self.errorDescList.append("CustomerContact: firstName is blank")
            self.isPayloadValid = False
        
        if "lastName" not in payload:
            self.errorDescList.append("CustomerContact: lastName is blank")
            self.isPayloadValid = False
            
        if "phone" not in payload:
            self.errorDescList.append("CustomerContact: phone is blank")
            self.isPayloadValid = False
            
        if "email" not in payload:
            self.errorDescList.append("CustomerContact: email is blank")
            self.isPayloadValid = False
        