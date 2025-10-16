import React, { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import '../common/Common.css';
import { storeUserAddress } from "../redux/actions/userAddressAction";
import { useNavigate } from 'react-router-dom';
import { useAuth } from "react-oidc-context";
import { getUserAddressList } from '../redux/actions/userAddressListAction';

function Address() {
  const auth = useAuth();
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const addressData = useSelector((state) => state.addressListState.userAddressList);
  const { recentUsedAddress, ...addresses } = addressData;

  const [selectedAddressKey, setAddressKey] = useState(recentUsedAddress);

  const cartItems = useSelector((state) => state.cartState.cartItems);
  const cartCount = cartItems ? Object.keys(cartItems).length : 0;

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
    navigate("/payment");
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
              <button
                onClick={() => handleSelect(key)}
                className="text-sm text-blue-600 hover:underline"
              >
                Select
              </button>
            </div>
          </div>
        ))}
      </div>

      <div className="lineSpacing"/>

      <a 
        onClick={() => navigate('/profile', { state: { tab: 'Addresses' } })}>
        To add new address click here
      </a>

      <div className="paraSpacing"/>

      <div className="mt-8">
        <button onClick={handleConfirmAddress} disabled={cartCount < 1 || selectedAddressKey == null}
          className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 focus-visible:outline-indigo-600 text-white rounded">
          Confirm
        </button>
      </div>

    </div>
  )
}

export default Address;