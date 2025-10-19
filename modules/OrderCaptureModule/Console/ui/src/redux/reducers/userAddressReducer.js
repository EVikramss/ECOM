import * as types from "../actions/types/userAddressActionTypes";
import initialState from "./config/initialState";

export default function userAddressReducer(state = initialState.userSelectedAddress, action) {
  switch (action.type) {
    case types.UPDATE_ADDRESS:
      return { ...action.address};
    default:
      return state;
  }
}