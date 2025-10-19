import * as types from "./types/orderActionTypes";
import * as orderAPI from "../../api/orderAPI";
import { toast } from "react-toastify";
import { beginApiCall, apiCallError, endApiCall } from "./apiBoundaryActions";

export function setOrderNo(orderNo) {
    return { type: types.SET_ORDER_NO, orderNo };
}

export function markOrderExported() {
    return { type: types.MARK_ORDER_EXPORTED };
}

export function getOrderNoError() {
    return { type: types.GET_ORDER_NO_ERROR };
}

export function clearOrderNo() {
    return { type: types.CLEAR_ORDER_NO };
}

export function fetchOrderNo(authInfo, navigate) {
    return function (dispatch, getState) {
        dispatch(beginApiCall());
        orderAPI.getOrderNo(authInfo)
            .then(response => {
                if (response.data.data.generateOrderNo) {
                    dispatch(setOrderNo(response.data.data.generateOrderNo));
                    navigate("/payment");
                } else {
                    toast.error("Error fetching orderNo. Try again");
                    dispatch(getOrderNoError());
                }
                dispatch(endApiCall());
            }).catch(error => {
                toast.error("Error fetching orderNo. Try again");
                dispatch(getOrderNoError());
                dispatch(apiCallError(error));
            });
    };
}