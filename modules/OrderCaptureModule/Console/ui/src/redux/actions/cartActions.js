import * as types from "./types/cartActionTypes";
import { beginApiCall, endApiCall } from "./apiBoundaryActions";
import { readCartCookie, storeCartCookie, clearCartCookie } from "../../login/cartCookierHandler";
import * as userCartAPI from "../../api/userCartAPI";
import { toast } from "react-toastify";
import * as util from "../../common/util";

export function updateCart(cartItems) {
    return { type: types.UPDATE_CART, cartItems }
}

export function markCartEntryPresent() {
    return { type: types.CART_ENTRY_PRESENT };
}

export function storeCart(dispatch, getState, cartItems, auth) {
    dispatch(beginApiCall());

    try {
        // if auth store cart against user info, else store in cookie
        if (auth.isAuthenticated) {
            const isCartEntryPresent = getState().cartState.isCartEntryPresent;

            const compressedCartString = util.compressObject(cartItems);
            const promise = isCartEntryPresent > 0
                ? userCartAPI.updateCart(compressedCartString, auth)
                : userCartAPI.createCart(compressedCartString, auth);

            promise.then(response => {
                dispatch(endApiCall());
                if (response.data) {
                    // TBD
                } else {
                    // TBD
                }
            });
        } else {
            storeCartCookie(cartItems);
            dispatch(endApiCall());
        }
    } catch (error) {
        toast.error("Error updating cart");
        dispatch(endApiCall());
    }
}

export function readCart(auth, init) {
    return function (dispatch, getState) {
        dispatch(beginApiCall());

        try {
            readCartFromCookie(dispatch);
            const existingCart = getState().cartState.cartItems;
            const existingCartCount = existingCart ? Object.keys(existingCart).length : 0;

            if (auth.isAuthenticated) {
                // since user logged in, clear cart from cookie
                clearCartCookie();

                userCartAPI.getCart(auth).then(response => {
                    if (response.data && response.data.data && response.data.data.getUserInfo) {
                        // mark cart items entry present in DB
                        dispatch(markCartEntryPresent());

                        // fetch cart items
                        const cartItems = response.data.data.getUserInfo.data;
                        if (cartItems != null) {
                            const persistedCart = util.decompressObject(cartItems);
                            let mergedCart = persistedCart;

                            // merge with existing items if present & store the updated cart
                            if (existingCartCount > 0) {
                                mergedCart = mergeCartItems(existingCart, persistedCart);
                                storeCart(dispatch, getState, mergedCart, auth);
                            }

                            // update final cart
                            dispatch(updateCart(mergedCart));
                        }
                    } else if (init && existingCartCount > 0) {
                        // store existing cart from state against user
                        storeCart(dispatch, getState, existingCart, auth);
                    }
                    dispatch(endApiCall());
                }).catch(error => {
                    dispatch(apiCallError(error));
                });
            }
        } catch (error) {
            toast.error("Error reading cart");
            dispatch(endApiCall());
        }
    }
}

function mergeCartItems(existingCart, persistedCart) {

    let mergedCartItems = JSON.parse(JSON.stringify(existingCart));

    // merge itemID from persistedCart
    for (let itemID in persistedCart) {
        if (mergedCartItems[itemID]) {
            mergedCartItems[itemID].orderQty += persistedCart[itemID].orderQty;

            if (mergedCartItems[itemID].orderQty > mergedCartItems[itemID].maxQty) {
                mergedCartItems[itemID].orderQty = mergedCartItems[itemID].maxQty;
            }
        } else {
            mergedCartItems[itemID] = { ...persistedCart[itemID] };
        }
    }

    return mergedCartItems;
}

function readCartFromCookie(dispatch) {
    const cartItems = readCartCookie();
    if (cartItems != null) {
        dispatch(updateCart(cartItems));
    }
}

export function clearCart(auth) {
    return function (dispatch, getState) {
        // get updated cart
        const updatedCart = {};

        // store updated cart
        storeCart(dispatch, getState, updatedCart, auth);
        dispatch(updateCart(updatedCart));
    }
}

export function addToCart(itemData, auth) {
    return function (dispatch, getState) {
        const cartItems = getState().cartItems;
        let updatedCart = { ...cartItems };
        let itemID = newItem.itemID

        // if item existing, add qty and check against max qty limit
        if (itemID in updatedCart) {
            let existingItem = updatedCart[itemID];
            let newQty = existingItem.orderQty + newItem.orderQty;
            let maxQty = existingItem.maxQty;
            if (newQty > maxQty) {
                newQty = maxQty;
            }
            existingItem.orderQty = newQty;
        } else {
            updatedCart[itemID] = newItem;
        }

        // store updated cart
        storeCart(dispatch, getState, updatedCart, auth);
        dispatch(updateCart(updatedCart));
    }
}

export function removeFromCart(itemID, auth) {
    return function (dispatch, getState) {
        const cartItems = getState().cartItems;
        let updatedCart = { ...cartItems };

        // if item existing, add qty and check against max qty limit
        if (itemID in updatedCart) {
            delete updatedCart[itemID];
        }

        // store updated cart
        storeCart(dispatch, getState, updatedCart, auth);
        dispatch(updateCart(updatedCart));
    }
}

export function changeCartQty(itemID, deltaQty, auth) {
    return function (dispatch, getState) {
        const cartItems = getState().cartItems;
        let updatedCart = { ...cartItems };

        // get updated cart
        if (itemID in updatedCart) {
            let existingItem = updatedCart[itemID];
            existingItem.orderQty = existingItem.orderQty + changeInQty;
        }

        // store updated cart
        storeCart(dispatch, getState, updatedCart, auth);
        dispatch(updateCart(updatedCart));
    }
}