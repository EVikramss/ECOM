
import { Routes, Route } from 'react-router-dom';
import OrderSearch from '../search/OrderSearch';
import OrderDetail from '../details/OrderDetail';
import Header from './Header'

function Router(props) {
    return (
        <Routes>
            <Route path="/" element={<Header />} />
            <Route path="/orderSearch" element={<OrderSearch token={props.token} />} />
            <Route path="/orderDetail" element={<OrderDetail />} />
        </Routes>
    )
}

export default Router