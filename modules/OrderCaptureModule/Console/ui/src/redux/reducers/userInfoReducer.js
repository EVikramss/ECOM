import * as types from "../actions/types/userInfoActionTypes";
import initialState from "./config/initialState";

export default function userInfoReducer(state = initialState.userInfo, action) {
  switch (action.type) {
    case types.CREATE_USER_INFO:
      return { ...action.userInfo };
    case types.UPDATE_USER_INFO:
      return { ...action.userInfo };
    case types.DELETE_USER_INFO:
      return { ...initialState.userInfo };
    default:
      return state;
  }
}