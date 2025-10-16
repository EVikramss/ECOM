import * as types from "../actions/types/apiBoundaryActionTypes";
import initialState from "./config/initialState";

export default function apiBoundaryReducer(state = initialState.apiCallsInProgress, action) {
  if (action.type == types.BEGIN_API_CALL) {
    return 1;
  } else if (action.type === types.API_CALL_ERROR || action.type === types.END_API_CALL) {
    return 0;
  }

  return state;
}
