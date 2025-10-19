import * as types from "./types/skuListActionTypes";
import * as skuApi from "../../api/skuApi";
import { apiCallError, beginApiCall, endApiCall } from "./apiBoundaryActions";

export function storeSkuList(searchResult) {
    return { type: types.STORE_SKU_LIST, searchResult };
}

export function clearSkuList() {
    return { type: types.CLEAR_SKU_LIST };
}

export function storeSkuSearchParams(searchParams) {
    return { type: types.STORE_SKU_SEARCH_PARAMS, searchParams };
}

export function clearSkuSearchParams() {
    return { type: types.CLEAR_SKU_SEARCH_PARAMS };
}

export function fetchSKUList(searchParams) {
    return function (dispatch) {
        dispatch(beginApiCall());
        return skuApi.fetchSKUList(searchParams)
            .then(response => {
                // if user present in db, store response in redux store
                // else if user not present, add to db
                if (response.data) {
                    dispatch(storeSkuList(response.data));
                }
                dispatch(endApiCall());
            }).catch(error => {
                dispatch(apiCallError(error));
            });
    };
}