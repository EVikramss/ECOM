import { useParams, useLocation } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../../common/Common.css';
import { useNavigate } from 'react-router-dom';

function orderDetail() {
    const { state } = useLocation();
    const orderData = state.data;
	
	// navigation hook
	const navigate = useNavigate();

    let getStatusDescFromStatus = (statusNum) => {
        let statusDesc = "Created";
        if (statusNum == 1) {
            statusDesc = "Scheduled";
        } else if (statusNum == 2) {
            statusDesc = "Shipped";
        } else if (statusNum == 3) {
            statusDesc = "Cancelled";
        }
        return statusDesc;
    }

    let statusDesc = getStatusDescFromStatus(orderData.orderStatus.status)

    return (
        <div className="headerSpacing">
			<button className="btn btn-outline-primary" onClick={() => navigate(-1)}>←</button>
			<div className="lineSpacing"></div>
			
            <div className="card">
                <div className="text-decoration-underline">
                    <strong>Order Info</strong>
                </div>
                <div className="lineSpacing"></div>

                <div className="row mb-3">
                    <div className="col-md-6">
                        <strong>Order No:</strong> {orderData.orderNo}
                    </div>
                    <div className="col-md-6">
                        <strong>Entity:</strong> {orderData.entity}
                    </div>
                </div>

                <div className="row">
                    <div className="col-md-6">
                        <strong>Order Date:</strong> {orderData.orderDate}
                    </div>
                    <div className="col-md-6">
                        <strong>Status:</strong> {statusDesc}
                    </div>
                </div>
            </div>

            <div className="paraSpacing"></div>

            <div className="card rounded">
                <div className="text-decoration-underline">
                    <strong>Item Details</strong>
                </div>
                <div className="lineSpacing"></div>
                <table className="table table-bordered mb-0 rounded">
                    <thead className="table-light">
                        <tr>
                            <th>Line No</th>
                            <th>SKU</th>
                            <th>Quantity</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {orderData.itemData.map(item => (
                            <tr key={item.lineno}>
                                <td>{item.lineno}</td>
                                <td>{item.sku}</td>
                                <td>{item.qty}</td>
                                <td>{getStatusDescFromStatus(item.status)}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <div className="paraSpacing"></div>

            <div className="row">
                <div className="col-md-6">
                    <div className="card mb-3">
                        <div className="text-decoration-underline">
                            <strong>Customer Contact</strong>
                        </div>
                        <div className="lineSpacing"></div>
                        <div>
                            <p><strong>Name:</strong> {orderData.customerContact.salutation} {orderData.customerContact.firstName} {orderData.customerContact.lastName}</p>
                            <p><strong>Phone:</strong> {orderData.customerContact.phone}</p>
                            <p><strong>Email:</strong> {orderData.customerContact.email}</p>
                        </div>
                    </div>
                </div>

                <div className="col-md-6">
                    <div className="card mb-3">
                        <div className="text-decoration-underline">
                            <strong>Shipping Address</strong>
                        </div>
                        <div className="lineSpacing"></div>
                        <div>
                            <p><strong>Address Line 1:</strong> {orderData.address.addressline1}</p>
                            <p><strong>Address Line 2:</strong> {orderData.address.addressline2}</p>
                            <p><strong>City:</strong> {orderData.address.city}</p>
                            <p><strong>State:</strong> {orderData.address.state}</p>
                            <p><strong>Country:</strong> {orderData.address.country}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default orderDetail