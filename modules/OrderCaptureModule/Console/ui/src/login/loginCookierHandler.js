
export function checkAndStoreLoginCookie(authInfo) {
    if (!isLoginCookiePresent(authInfo)) {
        storeLoginCookie(authInfo);
    }
}

export function isLoginCookiePresent(authInfo) {
    return document.cookie.split('; ').some(cookie => cookie.startsWith("OrderCapLoggedIn=true"));
}

export function storeLoginCookie(authInfo) {
    document.cookie = "OrderCapLoggedIn=true; path=/";
}

export function clearLoginCookie(authInfo) {
    document.cookie = "OrderCapLoggedIn=; path=/; expires=Thu, 01 Jan 1970 00:00:00 UTC";
}