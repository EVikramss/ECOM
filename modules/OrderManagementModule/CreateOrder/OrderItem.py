class OrderItem:
    
    id = None
    sku = None
    qty = None
    status = '0'    # 0 for created status
    orderNo = None
    orderItemKey = None
    isPayloadValid = True
    errorDescList = []
    
    def __init__(self, payload, orderNo, id, orderItemKey):
        try:
            self.orderNo = orderNo
            self.id = id
            self.orderItemKey = orderItemKey
            self.parseOrderItem(payload)
        except Exception as e:
            self.markError(str(e))
            
    def getQueries(self):
        queries = []
        queries.append(f"INSERT INTO OrderItemData VALUES ('{self.orderNo}','{self.id}','{self.sku}','{self.status}','{self.qty}','{self.orderItemKey}')")
        return queries
        
    def isValid(self):
        return self.isPayloadValid, self.errorDescList
    
    def parseOrderItem(self, payload):
        self.validateOrderItem(payload)
        
        self.sku = payload["sku"]
        self.qty = payload["qty"]
        
    def validateOrderItem(self, payload):
        if "sku" not in payload:
            self.errorDescList.append("sku is blank")
            self.isPayloadValid = False
        
        if "qty" not in payload:
            self.errorDescList.append("qty is blank")
            self.isPayloadValid = False
        