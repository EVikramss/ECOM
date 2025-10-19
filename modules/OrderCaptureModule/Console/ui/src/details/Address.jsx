import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import '../common/Common.css';
import { storeUserAddress } from "../redux/actions/userAddressAction";
import { useNavigate } from 'react-router-dom';
import { useAuth } from "react-oidc-context";
import { getUserAddressList } from '../redux/actions/userAddressListAction';
import { fetchOrderNo } from '../redux/actions/orderActions';

function Address() {
  const auth = useAuth();
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const addressData = useSelector((state) => state.addressListState.userAddressList);
  const { recentUsedAddress, ...addresses } = addressData;

  const [selectedAddressKey, setAddressKey] = useState(recentUsedAddress);

  const cartItems = useSelector((state) => state.cartState.cartItems);
  const cartCount = cartItems ? Object.keys(cartItems).length : 0;

  const orderState = useSelector((state) => state.orderState);
  const orderNo = orderState.orderNo;
  const orderNoRetry = orderState.orderNoRetry;

  useEffect(() => {
    if (!auth.isAuthenticated) {
      navigate("/login");
    } else {
      dispatch(getUserAddressList(auth));
    }
  }, []);

  const handleSelect = (key) => {
    setAddressKey(key);
  };

  const handleConfirmAddress = (e) => {
    e.preventDefault();
    dispatch(storeUserAddress(addressData[selectedAddressKey]));
    dispatch(fetchOrderNo(auth, navigate));
  };

  return (
    <div className="max-w-4xl mx-auto p-6 bg-white shadow rounded-lg headerSpacing">
      <h2 className="text-2xl font-semibold text-gray-900 mb-6">Saved Addresses</h2>

      <div className="space-y-6">
        {Object.entries(addresses).map(([key, address]) => (
          <div
            key={key}
            className={`border rounded-lg p-4 bg-gray-50 shadow-sm ${key === selectedAddressKey ? 'border-blue-500' : 'border-gray-300'
              }`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="font-medium text-gray-900">{address.addressLine1}</p>
                <p className="text-gray-700">{address.addressLine2}</p>
                <p className="text-gray-700">
                  {address.city}, {address.state} - {address.postalCode}
                </p>
                <p className="text-gray-700">{address.country}</p>
                {address['delivery instructions'] && (
                  <p className="text-sm text-gray-500 mt-1">
                    Instructions: {address['delivery instructions']}
                  </p>
                )}
              </div>
              <input
                type="checkbox"
                checked={key === selectedAddressKey}
                onChange={() => handleSelect(key)}
                className="mt-2"
              />
            </div>
          </div>
        ))}
      </div>

      <div className="lineSpacing" />

      <a
        onClick={() => navigate('/profile', { state: { tab: 'Addresses' } })}>
        To add new address click here
      </a>

      <div className="paraSpacing" />

      <div className="mt-8">
        <button onClick={handleConfirmAddress} disabled={cartCount < 1 || selectedAddressKey == null}
          className={`px-4 py-2 rounded text-white focus-visible:outline-indigo-600
              ${cartCount < 1 || selectedAddressKey == null
              ? 'bg-indigo-300 cursor-not-allowed'
              : 'bg-indigo-600 hover:bg-indigo-500'}
          `}>
          Confirm
        </button>
      </div>

    </div >
  )
}

export default Address;