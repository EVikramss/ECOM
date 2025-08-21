export const ORDER_SEARCH_QUERY = `query getOrder($input: String!){
                getOrder(orderNo: $input) {
                    orderNo
                    orderDate
                    entity
                }
            }`;