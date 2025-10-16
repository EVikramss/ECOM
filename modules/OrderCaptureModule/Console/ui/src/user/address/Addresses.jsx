import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import UpdateAddress from './UpdateAddress';
import { useAuth } from "react-oidc-context";
import { getUserAddressList, storeUserAddressList } from '../../redux/actions/userAddressListAction';

const Addresses = () => {
    const dispatch = useDispatch();
    const addressData = useSelector((state) => state.addressListState.userAddressList);
    const { recentUsedAddress, ...addresses } = addressData;
    const auth = useAuth();

    const [showForm, setShowForm] = useState(false);
    const [editKey, setEditKey] = useState(null);
    const [editData, setEditData] = useState(null);

    useEffect(() => {
        dispatch(getUserAddressList(auth));
    }, []);

    const handleEdit = (key) => {
        setEditKey(key);
        setEditData(addresses[key]);
        setShowForm(true);
    };

    const handleAddAddress = () => {
        setEditKey(null);
        setEditData(null);
        setShowForm(true);
    };

    const closeForm = () => {
        setShowForm(false);
    };

    const handleAddAddressFormSubmit = (newAddress) => {
        dispatch(storeUserAddressList(addressData, newAddress, editKey, auth));
    };

    return (
        <div className="max-w-4xl mx-auto p-6 bg-white shadow rounded-lg">
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Saved Addresses</h2>

            <div className="space-y-6">
                {Object.entries(addresses).map(([key, address]) => (
                    <div
                        key={key}
                        className={`border rounded-lg p-4 bg-gray-50 shadow-sm ${key === recentUsedAddress ? 'border-blue-500' : 'border-gray-300'
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
                                onClick={() => handleEdit(key)}
                                className="text-sm text-blue-600 hover:underline"
                            >
                                Edit
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            <div className="mt-8">
                <button
                    onClick={handleAddAddress}
                    className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 focus-visible:outline-indigo-600 text-white rounded"
                >
                    Add Address
                </button>
            </div>

            {showForm && (<UpdateAddress initialData={editData} onSubmit={handleAddAddressFormSubmit} closeForm={closeForm}/>)}

        </div>
    );
};

export default Addresses;