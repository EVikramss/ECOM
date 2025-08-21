
import { Routes, Route } from 'react-router-dom';
import OrderSearch from '../search/OrderSearch';
import OrderDetail from '../details/OrderDetail';
import Header from './Header'

function Router() {
    return (
        <Routes>
            <Route path="/" element={<Header />} />
            <Route path="/orderSearch" element={<OrderSearch />} />
            <Route path="/orderDetail" element={<OrderDetail />} />
        </Routes>
    )
}

export default Router