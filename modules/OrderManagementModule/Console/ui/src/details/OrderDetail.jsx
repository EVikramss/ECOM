import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import './OrderDetail.css';

function orderDetail() {
    const { state } = useLocation();
    const orderData = state.data;

    const statusNum = orderData.orderStatus.status;
    var statusDesc = "Created";
    if (statusNum == 1) {
        statusDesc = "Scheduled";
    } else if (statusNum == 2) {
        statusDesc = "Shipped";
    } else if (statusNum == 3) {
        statusDesc = "Cancelled";
    }

    return (
        <div className="orderDetail">
            <table>
                <tr>
                    <td>OrderNo</td>
                    <td><p>{orderData.orderNo}</p></td>
                </tr>
                <tr>
                    <td>Status</td>
                    <td><p>{statusDesc}</p></td>
                </tr>
                <tr>
                    <td>OrderDate</td>
                    <td><p>{orderData.orderDate}</p></td>
                </tr>
                <tr>
                    <td>Entity</td>
                    <td><p>{orderData.entity}</p></td>
                </tr>

                <tr></tr>

                <tr>
                    <td>Customer Details</td>
                </tr>
                <tr>
                    <td><p>{orderData.customerContact.salutation} {orderData.customerContact.firstName} {orderData.customerContact.lastName}</p></td>
                </tr>
                <tr>
                    <td><p>{orderData.customerContact.phone}</p></td>
                </tr>
                <tr>
                    <td><p>{orderData.customerContact.email}</p></td>
                </tr>

                <tr></tr>

                <tr>
                    <td>Shipping Address</td>
                </tr>
                <tr>
                    <td>Address Line 1</td>
                    <td><p>{orderData.address.addressline1}</p></td>
                </tr>
                <tr>
                    <td>Address Line 2</td>
                    <td><p>{orderData.address.addressline2}</p></td>
                </tr>
                <tr>
                    <td>City</td>
                    <td><p>{orderData.address.city}</p></td>
                </tr>
                <tr>
                    <td>State</td>
                    <td><p>{orderData.address.state}</p></td>
                </tr>
                <tr>
                    <td>Country</td>
                    <td><p>{orderData.address.country}</p></td>
                </tr>

                <tr></tr>

                <tr>
                    <td>Order Items</td>
                </tr>

                {users.itemData.map((item, index) => (
                    <li key={index}>
                        {item.lineno} - {item.qty} - {item.sku} - {
                            item.status === 1 ? "Scheduled" :
                                item.status === 2 ? "Shipped" :
                                    item.status === 3 ? "Cancelled" :
                                        "Created"
                        }
                    </li>
                ))}

            </table>
        </div>
    )
}

export default orderDetail