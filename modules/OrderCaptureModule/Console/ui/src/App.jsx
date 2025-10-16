import Router from './common/Router'
import Header from './common/Header'
import SpinnerOverlay from './common/SpinnerOverlay'
import { ToastContainer } from 'react-toastify';
import { useSelector } from "react-redux";

function App() {
  const apiCallsInProgress = useSelector((state) => state.apiCallsInProgress);

  return (
    <>
      <Header />
      <Router />
      <ToastContainer position="top-right" autoClose={3000} />
      {apiCallsInProgress > 0 && <SpinnerOverlay />}
    </>
  )
}

export default App