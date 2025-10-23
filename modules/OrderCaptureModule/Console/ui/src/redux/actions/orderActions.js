import * as types from "./types/orderActionTypes";
import * as orderAPI from "../../api/orderAPI";
import { toast } from "react-toastify";
import { beginApiCall, apiCallError, endApiCall } from "./apiBoundaryActions";
import config from '../../common/config.json';

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

export function exportOrder(authInfo, navigate) {
    return function (dispatch, getState) {
        const orderNo = getState().orderState.orderNo;
        const userAddress = getState().userSelectedAddress;
        const cartItems = getState().cartState.cartItems;
        const itemsArray = Object.values(cartItems);

        if (orderNo) {
            // form orderJson
            const orderJson = {};
            const entity = config.entity;
            const skuData = [];

            orderJson["orderNo"] = orderNo;
            orderJson["entity"] = entity;
            orderJson["sub"] = authInfo.user.profile.sub;
            orderJson["address"] = {
                "country": "IND",
                "city": userAddress.city,
                "state": userAddress.state,
                "addressline1": userAddress.addressLine1,
                "addressline2": userAddress.addressLine2,
                "pincode": userAddress.phone
            };
            orderJson["customerContact"] = {
                "fullName": authInfo.user.profile.name,
                "phone": "",
                "email": ""
            };

            for (let counter = 0; counter < itemsArray.length; counter++) {
                let itemData = itemsArray[counter];
                let orderSku = {};
                orderSku["sku"] = itemData.itemID;
                orderSku["qty"] = itemData.orderQty;
                orderSku["price"] = itemData.price;
                orderSku["taxCode"] = itemData.taxCode;
                orderSku["desc"] = itemData.desc;
                orderSku["status"] = "pending";
                skuData.push(orderSku);
            }
            orderJson["itemData"] = skuData;

            console.log(orderJson);
            dispatch(beginApiCall());
            orderAPI.postOrder(authInfo, orderJson)
                .then(response => {
                    if (response.status >= 200 && response.status < 300) {
                        dispatch(markOrderExported());
                        navigate("/confirmOrder");
                    } else {
                        toast.error("Error submitting your order. Try again");
                    }
                    dispatch(endApiCall());
                }).catch(error => {
                    toast.error("Error submitting your order. Try again");
                    dispatch(apiCallError(error));
                });
        } else {
            toast.error("Error reading orderNo. Checkout cart again");
        }
    };
}