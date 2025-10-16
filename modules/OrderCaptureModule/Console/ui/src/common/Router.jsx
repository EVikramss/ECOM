import { Routes, Route, useLocation } from 'react-router-dom';
import ErrorPage from '../ErrorPage';
import Home from '../home/Home';
import ItemDetail from '../details/ItemDetail';
import Address from '../details/Address';
import OrderSummary from '../details/OrderSummary';
import Payment from '../details/Payment';
import Login from '../login/Login';
import Cart from './Cart';
import Profile from '../user/Profile';

function Router(props) {
    return (
        <Routes>
            {/* root and error pages */}
            <Route path="/" element={<Home />} />
            <Route element={<ErrorPage />} />

            {/* show details of selected item */}
            <Route path="/itemDetail" element={<ItemDetail />} />

            {/* handle login and login redirect screens */}
            <Route path="/login" element={<Login />} />

            {/* profile screen */}
            <Route path="/profile" element={<Profile />} />

            {/* on cart checkout go to address, then to payment and finally to confirm order */}
            <Route path="/cart" element={<Cart />} />
            <Route path="/address" element={<Address />} />
            <Route path="/payment" element={<Payment />} />
            <Route path="/confirmOrder" element={<OrderSummary />} />
        </Routes>
    )
}

export default Router