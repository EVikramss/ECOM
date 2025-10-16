import React from 'react';

const SpinnerOverlay = () => (
  <div className="fixed inset-0 z-50 flex items-center justify-center  bg-[rgba(255,255,255,0.6)] pointer-events-auto">
    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-indigo-600"></div>
  </div>
);

export default SpinnerOverlay;
