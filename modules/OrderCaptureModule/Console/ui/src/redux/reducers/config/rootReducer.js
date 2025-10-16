import { combineReducers } from "redux";
import cartReducer from "../cartReducer";
import apiBoundaryReducer from "../apiBoundaryReducer";
import userInfoReducer from "../userInfoReducer";
import userAddressReducer from "../userAddressReducer";
import userAddressListReducer from "../userAddressListReducer";
import orderReducer from "../orderReducer";

const rootReducer = combineReducers({
  apiCallsInProgress: apiBoundaryReducer,
  userInfo: userInfoReducer,
  userAddress: userAddressReducer,
  addressListState: userAddressListReducer,
  cartState: cartReducer,  
  orderState: orderReducer
});

export default rootReducer;