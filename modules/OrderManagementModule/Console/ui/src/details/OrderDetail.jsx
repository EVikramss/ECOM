import { useParams } from 'react-router-dom';
import { useLocation } from 'react-router-dom';
import './OrderDetail.css';

function orderDetail() {
    const { state } = useLocation();
    const orderData = state.data;

    return (
        <div className="orderDetail">
            <table>
                <tr>
                    <td>OrderNo</td>
                    <td><p>{orderData.OrderNo}</p></td>
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
                
                
            </table>
        </div>
    )
}

export default orderDetail