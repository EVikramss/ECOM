import { combineReducers } from "redux";
import cartReducer from "../cartReducer";
import apiBoundaryReducer from "../apiBoundaryReducer";
import userInfoReducer from "../userInfoReducer";
import userAddressReducer from "../userAddressReducer";
import userAddressListReducer from "../userAddressListReducer";
import orderReducer from "../orderReducer";
import skuListReducer from "../skuListReducer";

const rootReducer = combineReducers({
  apiCallsInProgress: apiBoundaryReducer,
  userInfo: userInfoReducer,
  userSelectedAddress: userAddressReducer,
  addressListState: userAddressListReducer,
  cartState: cartReducer,  
  orderState: orderReducer,
  skuData: skuListReducer
});

export default rootReducer;