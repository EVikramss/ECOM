import * as types from "./types/userAddressListActionTypes";
import * as userAddressAPI from "../../api/userAddressAPI";
import * as util from "../../common/util";
import { apiCallError, beginApiCall, endApiCall } from "./apiBoundaryActions";

export function updateUserAddressList(addresses) {
    return { type: types.UPDATE_ADDRESS_LIST, addresses };
}

export function markAddressListEntryPresent() {
    return { type: types.ADDRESS_LIST_ENTRY_PRESENT };
}

export function storeUserAddressList(addressData, newAddress, editKey, authInfo) {

    // Clone the original addressData to avoid mutation
    const updatedAddressData = { ...addressData };

    // get count of existing address keys
    const counter = Object.keys(addressData).filter(key => key.startsWith('ad')).length

    if (!editKey || !updatedAddressData.hasOwnProperty(editKey)) {
        // Generate a new key
        const newKey = `ad${counter + 1}`;
        editKey = newKey;
        updatedAddressData[newKey] = newAddress;
    } else {
        // Replace the existing address with the new one
        updatedAddressData[editKey] = newAddress;
    }
    updatedAddressData["recentUsedAddress"] = editKey;

    return function (dispatch, getState) {
        dispatch(beginApiCall());

        const isAddressEntryPresent = getState().addressListState.isAddressEntryPresent;

        const compressedAddressString = util.compressObject(updatedAddressData);
        const promise = isAddressEntryPresent
            ? userAddressAPI.updateUserAddresses(compressedAddressString, authInfo)
            : userAddressAPI.createUserAddresses(compressedAddressString, authInfo);

        promise.then(response => {
            if (response.data) {
                dispatch(updateUserAddressList(updatedAddressData));
            } else {
                dispatch(apiCallError(error));
            }
            dispatch(endApiCall());
        }).catch(error => {
            dispatch(apiCallError(error));
        });
    }
}

export function getUserAddressList(authInfo) {
    return function (dispatch) {
        dispatch(beginApiCall());
        userAddressAPI.getUserAddresses(authInfo)
            .then(response => {
                if (response.data && response.data.data && response.data.data.getUserInfo) {
                    dispatch(markAddressListEntryPresent());
                    const addressData = response.data.data.getUserInfo.data;
                    if (addressData != null) {
                        dispatch(updateUserAddressList(util.decompressObject(addressData)));
                    }
                }
                dispatch(endApiCall());
            }).catch(error => {
                dispatch(apiCallError(error));
            });
    }
}