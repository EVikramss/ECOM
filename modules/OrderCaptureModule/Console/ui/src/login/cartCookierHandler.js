import * as util from "../common/util";

export function isCartCookiePresent() {
    return document.cookie.split('; ').some(cookie => cookie.startsWith("OrderCapCart"));
}

export function readCartCookie() {
    if (isCartCookiePresent) {
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [key, value] = cookie.trim().split('=');
            if (key == "OrderCapCart") {
                return util.decompressObject(decodeURIComponent(value));
            }
        }
    } else {
        return null;
    }
}

export function storeCartCookie(cartItems) {
    // set expiry time as 24 hours
    const now = new Date();
    now.setTime(now.getTime() + (24 * 60 * 60 * 1000));
    const expires = "expires=" + now.toUTCString();

    const encodedData = encodeURIComponent(util.compressObject(cartItems));
    document.cookie = "OrderCapCart=" + encodedData + "; " + expires + "; path=/";
}

export function clearCartCookie(cartItems) {
    document.cookie = "OrderCapCart=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC";
}