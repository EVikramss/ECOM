import React from 'react';
import { useAuth } from "react-oidc-context";

const UserProfile = () => {
  const auth = useAuth();
  const email = auth?.user?.profile?.email;

  const handleChangePassword = () => {
    alert('Not implemented ...');
  };

  return (
    <div className="text-gray-700 space-y-4">
      <div>
        <span className="font-semibold">Email:</span> {email}
      </div>
      <button
        onClick={handleChangePassword}
        className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
      >
        Change Password
      </button>
    </div>
  );
};

export default UserProfile;