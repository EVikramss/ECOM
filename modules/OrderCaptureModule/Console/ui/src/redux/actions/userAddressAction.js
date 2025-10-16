import * as types from "./types/userAddressActionTypes";

export function storeUserAddress(address) {
    return { type: types.UPDATE_ADDRESS, address };
}
