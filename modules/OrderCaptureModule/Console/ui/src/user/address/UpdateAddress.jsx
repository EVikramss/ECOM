import React, { useState, useEffect } from 'react';

const UpdateAddress = ({ initialData = {}, onSubmit, closeForm }) => {
  const [formData, setFormData] = useState({
    addressLine1: '',
    addressLine2: '',
    state: '',
    city: '',
    postalCode: '',
    country: '',
    deliveryInstructions: '',
  });

  useEffect(() => {
    if (initialData) {
      setFormData({
        addressLine1: initialData.addressLine1 || '',
        addressLine2: initialData.addressLine2 || '',
        state: initialData.state || '',
        city: initialData.city || '',
        postalCode: initialData.postalCode || '',
        country: initialData.country || '',
        deliveryInstructions: initialData['delivery instructions'] || '',
      });
    }
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-white shadow-md border-t z-50">
      <div className="max-w-2xl mx-auto p-6">
        <button
          onClick={closeForm}
          className="absolute top-4 right-4 text-gray-500 hover:text-gray-700 text-xl font-bold"
          aria-label="Close"
        >
          &times;
        </button>


        <h2 className="text-lg font-medium text-gray-900 mb-4">
          {initialData ? 'Edit Address' : 'Add New Address'}
        </h2>
        <form onSubmit={handleSubmit} className="space-y-6">
          <input
            name="addressLine1"
            type="text"
            placeholder="Address Line 1"
            onChange={handleChange}
            value={formData.addressLine1}
            className="border p-2 rounded w-full"
            required
          />
          <input
            name="addressLine2"
            type="text"
            placeholder="Address Line 2"
            onChange={handleChange}
            value={formData.addressLine2}
            className="border p-2 rounded w-full"
            required
          />
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <input
              name="state"
              type="text"
              placeholder="State"
              onChange={handleChange}
              value={formData.state}
              className="border p-2 rounded w-full"
              required
            />
            <input
              name="city"
              type="text"
              placeholder="City"
              onChange={handleChange}
              value={formData.city}
              className="border p-2 rounded w-full"
              required
            />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <input
              name="postalCode"
              type="text"
              placeholder="ZIP/Postal code"
              onChange={handleChange}
              value={formData.postalCode}
              className="border p-2 rounded w-full"
              required
            />
            <input
              name="country"
              type="text"
              placeholder="Country"
              onChange={handleChange}
              value={formData.country}
              className="border p-2 rounded w-full"
              required
            />
          </div>
          <input
            name="deliveryInstructions"
            type="text"
            placeholder="Delivery Instructions"
            onChange={handleChange}
            value={formData.deliveryInstructions}
            className="border p-2 rounded w-full"
          />
          <button
            type="submit"
            className="w-full bg-indigo-600 text-white py-2 px-4 rounded hover:bg-indigo-700"
          >
            Save Address
          </button>
        </form>
      </div>
    </div>
  );
};

export default UpdateAddress;