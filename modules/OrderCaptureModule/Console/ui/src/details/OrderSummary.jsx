import React, { useEffect } from 'react';
import { useDispatch, useSelector } from "react-redux";
import { useAuth } from "react-oidc-context";
import * as orderAPI from '../api/orderAPI';
import { clearCart } from '../redux/actions/cartActions';
import { clearOrderNo } from '../redux/actions/orderActions';
import '../common/Common.css';

export default function OrderSummary() {
  const cartItems = useSelector((state) => state.cartState.cartItems);
  const userAddress = useSelector((state) => state.userSelectedAddress);
  const orderState = useSelector((state) => state.orderState);
  const auth = useAuth();
  const dispatch = useDispatch();

  const itemsArray = Object.values(cartItems);
  const totalAmount = itemsArray.reduce((sum, item) => sum + item.orderQty * parseFloat(item.price), 0);

  useEffect(() => {
    dispatch(clearCart(auth));
  }, []);

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow-md rounded-md headerSpacing">
      <h2 className="text-xl font-semibold text-gray-900 mb-6">Order Summary</h2>
      <h3 className="text-xl font-semibold text-gray-900 mb-6">{orderState.orderNo}</h3>

      <div className="mb-8">
        <h3 className="text-lg font-medium text-gray-700 mb-2">Customer Info</h3>
        <div className="text-sm text-gray-600">
          <p>{userAddress.firstName} {userAddress.lastName}</p>
          <p>{userAddress.email}</p>
          <p>{userAddress.phone}</p>
          <p>{userAddress.address}, {userAddress.city}, {userAddress.region} - {userAddress.postalCode}</p>
        </div>
      </div>

      <ul className="divide-y divide-gray-200">
        {itemsArray.map((item) => (
          <li key={item.itemID} className="flex py-6">
            <img src={item.imgurl} alt={item.desc} className="w-24 h-24 object-cover rounded" />
            <div className="ml-4 flex-1">
              <h4 className="text-sm font-medium text-gray-900">{item.desc}</h4>
              <p className="text-sm text-gray-500">Qty: {item.orderQty}</p>
              <p className="text-sm text-gray-500">Price: {item.currency} {item.price}</p>
              <p className="text-sm text-gray-500">Subtotal: {item.currency} {item.orderQty * parseFloat(item.price)}</p>
            </div>
          </li>
        ))}
      </ul>

      <div className="mt-6 text-right">
        <p className="text-lg font-semibold text-gray-900">Total: INR {totalAmount}</p>
      </div>
    </div>
  );
}
