import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useAuth } from "react-oidc-context";
import { getOrderHistory } from '../api/userOrderHistory';
import { apiCallError, beginApiCall, endApiCall } from '../redux/actions/apiBoundaryActions';
import * as util from "../common/util";

function OrderHistory() {
    const dispatch = useDispatch();
    const auth = useAuth();
    const [orderHistory, setOrderHistory] = useState({});

    const [selectedOrder, setSelectedOrder] = useState(null);

    const handleOrderClick = (orderNo) => {
        setSelectedOrder(orderNo === selectedOrder ? null : orderNo);
    };

    const convertStatusToDesc = (intStatus) => {
        let desc = "Pending";
        if (intStatus == 0) {
            desc = "Created";
        } else if (intStatus == 1) {
            desc = "Scheduled";
        } else if (intStatus == 2) {
            desc = "Shipped";
        } else if (intStatus == 3) {
            desc = "Cancelled";
        }

        return desc;
    }

    useEffect(() => {
        dispatch(beginApiCall());
        getOrderHistory(auth).then(response => {
            if (response.data.data.getUserInfo && response.data.data.getUserInfo.data) {
                setOrderHistory(util.decompressObject(response.data.data.getUserInfo.data));
            }
            dispatch(endApiCall());
        }).catch(error => {
            toast.error("Error fetching order History");
            dispatch(apiCallError(error));
        });
    }, []);

    return (
        <div className="max-w-4xl mx-auto p-6 bg-white shadow rounded-lg">
            <h2 className="text-2xl font-semibold text-gray-900 mb-6">Order History</h2>

            <div className="space-y-6">
                {Object.entries(orderHistory).map(([orderNo, items]) => (
                    <div
                        key={orderNo}
                        className={`border rounded-lg p-4 bg-gray-50 shadow-sm ${orderNo === selectedOrder ? 'border-blue-500' : 'border-gray-300'
                            }`}
                    >
                        <div className="flex justify-between items-start">
                            <div>
                                <p className="font-medium text-gray-900 cursor-pointer" onClick={() => handleOrderClick(orderNo)}>
                                    Order No: {orderNo}
                                </p>
                                {orderNo === selectedOrder && (
                                    <div className="mt-4 space-y-2">
                                        {items.map((item, index) => (
                                            <div key={index} className="text-gray-700">
                                                <p>SKU: {item.sku}</p>
                                                <p>Quantity: {item.qty}</p>
                                                <p>Status: {convertStatusToDesc(item.status)}</p>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                            <button
                                onClick={() => handleOrderClick(orderNo)}
                                className="text-sm text-blue-600 hover:underline"
                            >
                                {orderNo === selectedOrder ? 'Hide Details' : 'View Details'}
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}

export default OrderHistory;