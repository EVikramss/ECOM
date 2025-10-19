import * as types from "../actions/types/skuListActionTypes";
import initialState from "./config/initialState";

export default function userInfoReducer(state = initialState.skuData, action) {
  let newState = state;

  switch (action.type) {
    case types.STORE_SKU_LIST:
      newState = { ...state };
      newState.skuList = action.searchResult.itemResultList;
      newState.pageData = action.searchResult.pageData;

      // mark last page
      if (newState.skuList.length < newState.searchParams.pageSize) {
        newState.pageData.lastPageReached = true;
      } else if (state.skuList.length > 0 && newState.skuList.length == 0) {
        newState = { ...state };
        newState.pageData.lastPageReached = true;
      }

      return newState;
    case types.CLEAR_SKU_LIST:
      newState = { ...state };
      newState.skuList = null;
      return newState;
    case types.STORE_SKU_SEARCH_PARAMS:
      newState = { ...state };
      newState.searchParams = { ...action.searchParams };
      return newState;
    case types.CLEAR_SKU_SEARCH_PARAMS:
      newState = { ...state };
      newState.searchParams = null;
      return newState;
    default:
      return state;
  }
}