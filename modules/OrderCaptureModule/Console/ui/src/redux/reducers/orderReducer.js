import * as types from "../actions/types/orderActionTypes";
import initialState from "./config/initialState";

export default function userInfoReducer(state = initialState.orderState, action) {
  let newState = state;

  switch (action.type) {
    case types.MARK_ORDER_EXPORTED:
      newState = { ...state };
      newState.isOrderExported = 1;
      return newState;
    case types.SET_ORDER_NO:
      newState = { ...state };
      newState.orderNo = action.orderNo;
      newState.orderNoRetry = false;
      return newState;
    case types.GET_ORDER_NO_ERROR:
      newState = { ...state };
      newState.orderNoRetry = true;
      return newState;
    case types.CLEAR_ORDER_NO:
      newState = { ...state };
      newState.orderNo = null;
      newState.orderNoRetry = false;
      return newState;
    default:
      return state;
  }
}