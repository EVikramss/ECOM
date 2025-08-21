import './App.css'
import Router from './common/Router'
import Header from './common/Header'
import { useAuth } from "react-oidc-context";
import { ApolloProvider } from '@apollo/client';
import { useEffect, useMemo } from 'react';
import { CreateApolloClient } from './common/AwsClient';

function App() {
  const auth = useAuth();
  const token = auth.user?.id_token;
  const client = useMemo(() => CreateApolloClient(token), [token]);

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
        <ApolloProvider client={client}>
          <Header />
          <Router />
        </ApolloProvider>
      </>
    )
  }
}

export default App