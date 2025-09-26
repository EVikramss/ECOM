import { useParams, useLocation } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../../common/Common.css';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import config from '../../common/config.json';
import React, { useState, useEffect, useRef } from 'react';

function orderList(props) {
	// create state for orderData, pageNumber & errors
	const [orderListData, setOrderListData] = useState([]);
	const [pageNumber, setPageNumber] = useState(0);
	const [errorVal, setErrorVal] = useState({strVal : ''});
	let orderListSize = orderListData.length;
	const isFirstRun = useRef(true);
	
	// get state passed from order search
    const { state } = useLocation();
	
	// from state extract formData (used to search), pageSize
	const openSingleOrder = state.openSingleOrder;
	const formData = state.formData;
	const pageSize = 30;
	
	// navigation hook
	const navigate = useNavigate();

	// get order status description
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
	
	// fetch selected order
	const getOrder = async (orderNo) => {
        try {
            const token = props.token;
            axios.get(config.apiURL + "/getOrder?orderNo=" + orderNo, {
                headers: {
                    Authorization: token
                }
            }).then(result => {
                let orderData = result.data;
                if (orderData == null) {
                    setErrorMsg("Order no does not exist");
                } else {
					navigate('/orderDetail', { state: { data: orderData} });
                }
            }).catch(error => {
                setGeneralError(error);
            });
        } catch (error) {
            setGeneralError(error);
        }
    }
	
	// paginate over search results
	useEffect(() => {
		try {
            const token = props.token;
            axios.post(config.apiURL + "/searchOrder?pageSize=" + pageSize + "&pageNumber=" + pageNumber, formData, {
                headers: {
                    Authorization: token
                }
            }).then(result => {
                var orderDataArr = result.data;
                setOrderListData(orderDataArr);
				
				if (isFirstRun.current) {
					isFirstRun.current = false;
					checkAndNavigateToDetailPage(orderDataArr);	
				}
            }).catch(error => {
                setGeneralError(error);
            });
        } catch (error) {
            setGeneralError(error);
        }
	}, [pageNumber]);
	
	const previousPage = () => {
		if(pageNumber >= 1) {
			setPageNumber(pageNumber - 1);	
		}		
	}
	
	const nextPage = () => {
		setPageNumber(pageNumber + 1);
	}
	
	// convert order date to display format
	const convertDateField = (inputDate) => {
		const date = new Date(inputDate);		
		const formatted = date.toLocaleString(undefined, {
		  year: 'numeric',
		  month: '2-digit',
		  day: '2-digit',
		  hour: '2-digit',
		  minute: '2-digit',
		  second: '2-digit',
		  hour12: false
		});

		const formattedDate = formatted.replace(/\//g, '-').replace(',', '');
		
		return formattedDate;
	};
	
	const setGeneralError = (error) => {
		let message = 'Error'
		if(error.message)
			message = error.message
		
        setErrorVal({
            ...errorVal,
            strVal : message
        });
    }
	
	const setErrorMsg = (errorMsg) => {		
        setErrorVal({
            ...errorVal,
            strVal : errorMsg
        });
    }

	// if only 1 order which user is looking for and if found, directly open the details page
    let checkAndNavigateToDetailPage = (orderDataArr) => {
		if(orderDataArr.length == 1 && openSingleOrder) {
			getOrder(orderDataArr[0].orderNo);
		}
	}

    return (
        <div className="headerSpacing">
			<button className="btn btn-outline-primary" onClick={() => navigate(-1)}>←</button>
			<div className="lineSpacing"></div>
		
			<table className="table table-bordered mb-0 rounded">
				<thead className="table-light">
					<tr>
						<th>Order No</th>
						<th>Entity</th>
						<th>Order Date</th>
						<th>Status</th>
					</tr>
				</thead>
				<tbody>
					{orderListData.map(order => (
						<tr key={order.orderNo}>
							<td>
								<a onClick={(e) => {
									e.preventDefault();
									getOrder(order.orderNo);
									}
								} className="text-primary text-decoration-underline" style={{cursor: 'pointer'}}>
									{order.orderNo}
								</a>
							</td>
							<td>{order.entity}</td>
							<td>{convertDateField(order.orderDate)}</td>
							<td>{getStatusDescFromStatus(order.status)}</td>
						</tr>
					))}
				</tbody>
			</table>
			
			<div className="position-fixed bottom-0 end-0 p-3 d-flex gap-2">
				<button className="btn btn-outline-primary" onClick={previousPage} disabled={pageNumber < 1}>←</button>
				<button className="btn btn-outline-primary" onClick={nextPage} disabled={orderListSize == 0 || orderListSize < pageSize}>→</button>
			</div>
			
			<p></p>
			<p>{errorVal.strVal}</p>
        </div>
    )
}

export default orderList