// MyContext.js
import React, { createContext, useContext } from 'react';

const Environment = createContext();
export const getToken = () => useContext(environment);

export default Environment
