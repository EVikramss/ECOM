import { useDispatch, useSelector } from "react-redux";
import { useAuth } from "react-oidc-context";
import { useEffect } from 'react';

function Login() {
    const auth = useAuth();
    const token = auth.user?.id_token;

    useEffect(() => {
        if (!auth.isLoading && !auth.isAuthenticated) {
            auth.signinRedirect();
        }
    }, [auth.isLoading, auth.isAuthenticated]);

    return (
        <></>
    )
}

export default Login;