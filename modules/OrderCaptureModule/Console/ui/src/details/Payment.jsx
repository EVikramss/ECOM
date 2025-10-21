import { useDispatch, useSelector } from 'react-redux';
import React, { useEffect } from 'react';
import config from '../common/config.json';
import { useAuth } from "react-oidc-context";
import { useNavigate } from 'react-router-dom';
import { exportOrder } from '../redux/actions/orderActions';

function Payment() {
    const cartItems = useSelector((state) => state.cartState.cartItems);
    const itemsArray = Object.values(cartItems);
    const totalAmount = itemsArray.reduce((sum, item) => sum + item.orderQty * parseFloat(item.price), 0);
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const auth = useAuth();

    const handleConfirmPayment = (e) => {
        e.preventDefault();
        dispatch(exportOrder(auth, navigate));
      };

    return (
        <div className="max-w-4xl mx-auto p-6 bg-white shadow-md rounded-md headerSpacing">
            <h2 className="text-xl font-semibold text-gray-900 mb-6">Payment</h2>

            <div className="mt-6 text-right">
                <p className="text-lg font-semibold text-gray-900">Total: INR {totalAmount}</p>
            </div>

            <button
                type="button"
                onClick={handleConfirmPayment}
                className="bg-indigo-600 text-white hover:bg-indigo-500 focus-visible:outline-indigo-600inline-flex items-center 
                    justify-center rounded-md px-4 py-2 text-sm font-semibold shadow-sm focus:outline-none focus-visible:outline 
                    focus-visible:outline-2 focus-visible:outline-offset-2 bg-indigo-600 text-white hover:bg-indigo-500 focus-visible:outline-indigo-600">
                Confirm
            </button>
        </div>
    );
}

export default Payment;