import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import './OrderDetail.css';

function orderDetail () {
    const {state} = useLocation();
    const orderData = state.data;

    return (
        <div className="orderDetail">
            <p>{orderData.OrderNo}</p>
            <p>{orderData.orderDate}</p>
            <p>{orderData.entity}</p>
        </div>
    )
}

export default orderDetail