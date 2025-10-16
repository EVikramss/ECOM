import React, { useState } from 'react';
import UserProfile from "./UserProfile";
import Addresses from "./address/Addresses";
import OrderHistory from "./OrderHistory";
import { useLocation } from 'react-router-dom';

const Profile = () => {
    const location = useLocation();
    const initialTab = location.state?.tab || 'User Info';
    const [activeTab, setActiveTab] = useState(initialTab);

    const tabs = ['User Info', 'Addresses', 'Order History'];

    const renderContent = () => {
        switch (activeTab) {
            case 'Order History':
                return <OrderHistory />;
            case 'Addresses':
                return <Addresses />;
            case 'User Info':
                return <UserProfile />;
            default:
                return null;
        }
    };

    return (
        <div className="flex h-screen bg-gray-100 headerSpacing">
            <div className="w-64 bg-white border-r">
                <div className="p-4 font-semibold text-lg text-gray-800 border-b">Profile</div>
                <nav className="mt-4">
                    {tabs.map((tab) => (
                        <button
                            key={tab}
                            onClick={() => setActiveTab(tab)}
                            className={`w-full text-left px-4 py-2 hover:bg-gray-100 ${activeTab === tab ? 'bg-gray-200 font-medium' : ''
                                }`}
                        >
                            {tab}
                        </button>
                    ))}
                </nav>
            </div>

            <div className="flex-1 p-6">
                <h2 className="text-2xl font-semibold mb-4">{activeTab}</h2>
                <div className="bg-white p-4 rounded shadow">{renderContent()}</div>
            </div>
        </div>
    );
};

export default Profile;