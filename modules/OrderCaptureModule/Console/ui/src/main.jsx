import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import { BrowserRouter, MemoryRouter } from 'react-router-dom';
import store from "./redux/configureStore";
import { Provider } from "react-redux";
import { AuthProvider } from "react-oidc-context";
import config from './common/config.json';

const cognitoAuthConfig = {
	authority: config.authority,
	client_id: config.client_id,
	redirect_uri: config.redirect_uri,
	response_type: config.response_type,
	scope: config.scope,
};

createRoot(document.getElementById('root')).render(
	<StrictMode>
		<AuthProvider {...cognitoAuthConfig}>
			<Provider store={store}>
				<MemoryRouter>
					<App />
				</MemoryRouter>
			</Provider>
		</AuthProvider>
	</StrictMode>,
)
