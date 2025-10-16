import * as types from "../actions/types/cartActionTypes";
import initialState from "./config/initialState";

export default function cartReducer(state = initialState.cartState, action) {
  let newState = state;

  // copy both existing state and new data to maintain immutability
  switch (action.type) {
    case types.UPDATE_CART:
      newState = { ...state };
      newState.cartItems = { ...action.cartItems };
      return newState;
    case types.CART_ENTRY_PRESENT:
      newState = { ...state };
      newState.isCartEntryPresent = 1;
      return newState;
    default:
      return state;
  }
}

