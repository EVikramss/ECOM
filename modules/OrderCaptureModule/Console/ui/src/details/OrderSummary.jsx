import React, { useEffect } from 'react';
import { useDispatch, useSelector } from "react-redux";
import { useAuth } from "react-oidc-context";
import { clearCart } from '../redux/actions/cartActions';
import { clearOrderNo } from '../redux/actions/orderActions';
import '../common/Common.css';

export default function OrderSummary() {
  const userAddress = useSelector((state) => state.userSelectedAddress);
  const orderState = useSelector((state) => state.orderState);
  const auth = useAuth();
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(clearCart(auth));
  }, []);

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow-md rounded-md headerSpacing">
      <h2 className="text-xl font-semibold text-gray-900 mb-6">Order Summary</h2>
      <h3 className="text-xl font-semibold text-gray-900 mb-6">OrderNo: {orderState.orderNo}</h3>

      <div className="mb-8">
        <h3 className="text-lg font-medium text-gray-700 mb-2">Order Address</h3>
        <div className="text-sm text-gray-600">
          <p>{userAddress.addressLine1}</p>
          <p>{userAddress.addressLine2}</p>
          <p>{userAddress.city}</p>
          <p>{userAddress.country}</p>
          <p>{userAddress.postalCode}</p>
        </div>
      </div>
    </div>
  );
}
