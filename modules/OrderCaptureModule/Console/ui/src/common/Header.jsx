import { useNavigate } from 'react-router-dom';
import './Common.css'
import { FaShoppingCart } from 'react-icons/fa';
import { useDispatch, useSelector } from "react-redux";
import { useState, useEffect } from 'react';
import { useAuth } from "react-oidc-context";
import { Menu, MenuButton, MenuItem, MenuItems } from '@headlessui/react'
import { ChevronDownIcon } from '@heroicons/react/20/solid'
import { checkAndUpdateUserInfo } from "../redux/actions/userInfoActions";
import * as cookierHandler from "../login/loginCookierHandler";
import { readCart } from '../redux/actions/cartActions';

function Header() {
    const navigate = useNavigate();
    const auth = useAuth();
    const dispatch = useDispatch();

    const cartItems = useSelector(state => state.cartState.cartItems);
    const cartCount = cartItems ? Object.keys(cartItems).length : 0;

    const userInfo = useSelector((state) => state.userInfo);
    const isUserInfoStored = userInfo["data"] && userInfo["data"].length > 0 ? true : false;

    const signOut = () => {
        // TBD
        // cookierHandler.clearLoginCookie(auth);
        // auth.signoutSilent();
    };

    useEffect(() => {
        {/* get logged in user details*/ }
        if (auth.isAuthenticated) {
            if (!isUserInfoStored) {
                dispatch(checkAndUpdateUserInfo(auth));
            } else {

            }
            cookierHandler.checkAndStoreLoginCookie(auth);
        } else if (!auth.isLoading) {
            {/* If cookie present, redirect to login site to fetch user details */ }
            if (cookierHandler.isLoginCookiePresent(auth)) {
                auth.signinSilent();
            }
        }

        if (!auth.isLoading) {
            {/* load existing cart */ }
            dispatch(readCart(auth), true);
        }
    }, [auth.isLoading])

    return (
        <div className="fixed top-0 left-0 w-full h-[10vh] bg-black border-b-4 border-indigo-300 flex flex-col justify-between px-6 py-2">
            <div className="relative rounded-xl bg-neutral-950 text-white px-6">
                <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(to_bottom,rgba(255,255,255,0.12),rgba(255,255,255,0.02)_35%,transparent_60%)]"></div>

                <div className="flex justify-between items-center">
                    <div className="text-white text-xl font-semibold">
                        <a href="#" onClick={() => navigate('/')}>Order Site</a>
                    </div>

                    <div className="flex items-center space-x-4">
                        {auth.isAuthenticated ? (
                            <Menu as="div" className="relative inline-block">
                                <MenuButton className="inline-flex gap-x-1.5 rounded-md bg-black px-3 py-2 text-sm font-semibold border border-white rounded text-white shadow-sm hover:bg-green-50 hover:text-black">
                                    {auth.user.profile.email}
                                    <ChevronDownIcon aria-hidden="true" className="-mr-1 size-5 text-white" />
                                </MenuButton>

                                <MenuItems
                                    transition
                                    className="absolute right-0 z-40 mt-2 w-24 origin-top-right rounded-md bg-white shadow-lg outline-1 outline-black/5 transition data-closed:scale-95 data-closed:transform data-closed:opacity-0 data-enter:duration-100 data-enter:ease-out data-leave:duration-75 data-leave:ease-in"
                                >
                                    <div className="py-1">
                                        <MenuItem>
                                            <a
                                                href="#"
                                                className="block px-4 py-2 text-sm text-gray-700 data-focus:bg-gray-100 data-focus:text-gray-900 data-focus:outline-hidden"
                                                onClick={() => navigate('/profile')}
                                            >
                                                Profile
                                            </a>
                                        </MenuItem>
                                        <MenuItem>
                                            <a
                                                href="#"
                                                onClick={signOut}
                                                className="block px-4 py-2 text-sm text-gray-700 data-focus:bg-gray-100 data-focus:text-gray-900 data-focus:outline-hidden"
                                            >
                                                Sign out
                                            </a>
                                        </MenuItem>
                                    </div>
                                </MenuItems>
                            </Menu>
                        ) : (
                            <button onClick={() => navigate('/login')} className="text-sm text-white border border-white px-3 py-2 rounded hover:bg-green-50 hover:text-black">
                                {auth.isLoading && cookierHandler.isLoginCookiePresent(auth) ? (
                                    <div className="animate-spin rounded-full h-5 w-5 border-t-2 border-white"></div>
                                ) : (
                                    <>
                                        Login
                                    </>
                                )}
                            </button>
                        )}
                        <div className="flex items-center space-x-1 rounded-md text-white border border-white px-3 py-1 rounded hover:bg-green-50 hover:text-black">
                            <a href="#" onClick={() => navigate('/cart')}>
                                <span className="relative inline-block">
                                    <FaShoppingCart className="text-current" />
                                    {cartCount > 0 && (
                                        <span
                                            className="absolute -bottom-2 -right-14 grid place-items-center h-4 min-w-[16px] px-1
                       rounded-full bg-white text-black text-[10px] leading-4 ring-1 ring-white shadow"
                                            aria-label={`${cartCount} items in cart`}
                                        >
                                            {cartCount > 99 ? "99+" : cartCount}
                                        </span>
                                    )}
                                </span>
                                <span className=""> Cart</span>
                            </a>
                        </div>
                    </div>
                </div>

                {/*<div className="flex justify-center space-x-6 text-sm text-gray-700">
                <span>Mobiles</span>
                <span>Fashion</span>
                <span>Electronics</span>
                <span>Home</span>
                <span>Furniture</span>
                <span>Toys</span>
                <span>Movies</span>
            </div>*/}
            </div>
        </div >
    )
}

export default Header