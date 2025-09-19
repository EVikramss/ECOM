import './App.css'
import Router from './common/Router'
import Header from './common/Header'
import { useAuth } from "react-oidc-context";
import { useEffect } from 'react';

function App() {
  const auth = useAuth();
  const token = auth.user?.id_token;

  useEffect(() => {
    if (!auth.isLoading && !auth.isAuthenticated) {
      auth.signinRedirect();
    }
  }, [auth.isLoading, auth.isAuthenticated]);

  const signOutRedirect = () => {

  };

  if (auth.isAuthenticated) {
    return (
      <>
        <Header />
        <Router token={token} />
      </>
    )
  }
}

export default App