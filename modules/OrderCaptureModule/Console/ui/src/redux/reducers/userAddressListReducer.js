import * as types from "../actions/types/userAddressListActionTypes";
import initialState from "./config/initialState";

export default function userAddressListReducer(state = initialState.addressListState, action) {
  let newState = state;

  switch (action.type) {
    case types.UPDATE_ADDRESS_LIST:
      newState = { ...state };
      newState.userAddressList = { ...action.addresses };
      return newState;
    case types.ADDRESS_LIST_ENTRY_PRESENT:
      newState = { ...state };
      newState.isAddressEntryPresent = 1;
      return newState;
    default:
      return state;
  }
}