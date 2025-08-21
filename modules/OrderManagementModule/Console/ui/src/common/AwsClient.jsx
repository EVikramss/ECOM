import { ApolloClient, InMemoryCache, gql, HttpLink, ApolloLink } from '@apollo/client';
import config from './config.json';

export function CreateApolloClient(token) {
  const httpLink = new HttpLink({ uri: config.apiURL });

  const authLink = new ApolloLink((operation, forward) => {
      operation.setContext({
        headers: {
          Authorization: token,
        }
      });
      return forward(operation);
  });

  return new ApolloClient({
    link: authLink.concat(httpLink),
    cache: new InMemoryCache(),
  });
}