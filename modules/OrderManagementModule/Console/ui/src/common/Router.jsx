import { Routes, Route } from 'react-router-dom';
import OrderSearch from '../search/order/OrderSearch';
import OrderDetail from '../details/order/OrderDetail';
import OrderList from '../list/order/OrderList';
import ErrorSearch from '../search/error/ErrorSearch';
import ErrorDetail from '../details/error/ErrorDetail';
import ErrorList from '../list/error/ErrorList';
import StatsSearch from '../search/stats/StatsSearch';
import StatsList from '../list/stats/StatsList';
import ErrorPage from '../ErrorPage';
import Header from './Header'

function Router(props) {
    return (
        <Routes>
			// root and error pages
            <Route path="/" element={<Header />} />
			<Route element={<ErrorPage />} />
			
			// search pages
            <Route path="/orderSearch" element={<OrderSearch />} />
			<Route path="/errorSearch" element={<ErrorSearch />} />
			<Route path="/statsSearch" element={<StatsSearch />} />
			
			// detail pages
            <Route path="/orderDetail" element={<OrderDetail />} />
			<Route path="/errorDetail" element={<ErrorDetail />} />
			
			// list pages
			<Route path="/orderList" element={<OrderList token={props.token}/>} />
			<Route path="/errorList" element={<ErrorList token={props.token}/>} />
			<Route path="/statsList" element={<StatsList token={props.token}/>} />
        </Routes>
    )
}

export default Router