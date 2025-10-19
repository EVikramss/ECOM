import { useParams, useLocation } from 'react-router-dom';
import '../common/Common.css';
import { useNavigate } from 'react-router-dom';
import React, { useState, useMemo } from 'react';
import { useDispatch, useSelector } from "react-redux";
import { addToCart } from "../redux/actions/cartActions";
import { toast } from "react-toastify";
import { useAuth } from "react-oidc-context";
import 'react-toastify/dist/ReactToastify.css';

function ItemDetail() {
    const { state } = useLocation();
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const auth = useAuth();

    const itemData = state.data;
    const {
        itemID,
        price,
        currency,
        availability,
        maxQty,
        taxCode,
        desc,
        imgurl,
    } = itemData;
    const [qty, setQty] = useState(1);

    const cartItems = useSelector(state => state.cartState.cartItems);
    let orderQty = 0;
    if (itemID in cartItems)
        orderQty = cartItems[itemID].orderQty;

    const fmt = useMemo(
        () =>
            new Intl.NumberFormat("en-IN", {
                style: "currency",
                currency: currency || "INR",
                maximumFractionDigits: 2,
            }),
        [currency]
    );

    const onAddToCart = () => {
        let totalQty = orderQty + qty;
        if (totalQty > maxQty) {
            toast.error("Total order quantity limited to " + maxQty);
        } else {
            itemData.orderQty = qty;
            dispatch(addToCart(itemData, auth));
        }
    };

    const inStock = availability === "InStock";

    return (
        <div className="bg-white headerSpacing">
            <button
                type="button"
                onClick={() => navigate(-1)}
                className="paraSpacing inline-flex items-center justify-center rounded-md px-4 py-2 text-sm font-semibold 
                    shadow-sm bg-indigo-600 text-white hover:bg-indigo-500 offset ml-2"
            >
                ← Back
            </button>

            <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
                <div className="grid grid-cols-1 gap-10 lg:grid-cols-2 lg:gap-16">
                    <div className="w-full">
                        <div className="overflow-hidden rounded-2xl bg-gray-100 shadow-sm ring-1 ring-gray-200">
                            <img
                                src={imgurl}
                                alt={desc}
                                className="h-full w-full object-cover object-center"
                            />
                        </div>
                    </div>

                    <div className="flex flex-col">
                        <div className="mb-6">
                            <h1 className="text-2xl font-semibold tracking-tight text-gray-900 sm:text-3xl">
                                {desc}
                            </h1>
                            <div className="mt-3 flex items-center gap-3">
                                <p className="text-2xl font-bold text-gray-900">{price}</p>

                                {inStock ? (
                                    <span className="inline-flex items-center rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-medium text-emerald-700 ring-1 ring-inset ring-emerald-600/20">
                                        In stock
                                    </span>
                                ) : (
                                    <span className="inline-flex items-center rounded-full bg-rose-50 px-2.5 py-1 text-xs font-medium text-rose-700 ring-1 ring-inset ring-rose-600/20">
                                        Out of stock
                                    </span>
                                )}
                            </div>
                        </div>

                        {/* Quantity + Actions */}
                        <div className="mb-8">
                            <label htmlFor="qty" className="block text-sm font-medium text-gray-900">
                                Quantity
                            </label>
                            <div className="mt-2 flex items-center gap-3">
                                <select
                                    id="qty"
                                    value={qty}
                                    onChange={(e) => setQty(Number(e.target.value))}
                                    className="block w-28 rounded-md border-0 bg-white py-2 pl-3 pr-10 text-gray-900 ring-1 ring-inset ring-gray-300 focus:ring-2 focus:ring-indigo-600 sm:text-sm"
                                    disabled={!inStock}
                                >
                                    {Array.from({ length: Math.max(0, maxQty) }, (_, i) => i + 1).map((n) => (
                                        <option key={n} value={n}>
                                            {n}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="mt-6 flex flex-wrap gap-3">
                                <button
                                    type="button"
                                    onClick={onAddToCart}
                                    disabled={!inStock}
                                    className={`inline-flex items-center justify-center rounded-md px-4 py-2 text-sm font-semibold shadow-sm focus:outline-none focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 ${inStock
                                        ? "bg-indigo-600 text-white hover:bg-indigo-500 focus-visible:outline-indigo-600"
                                        : "bg-gray-200 text-gray-500 cursor-not-allowed"
                                        }`}
                                >
                                    Add to cart
                                </button>
                            </div>
                        </div>

                        {/* Order summary */}
                        <div className="mt-auto">
                            <div className="rounded-xl border border-gray-200 bg-gray-50 p-4">
                                <h2 className="text-sm font-semibold text-gray-900">Order summary</h2>
                                <dl className="mt-3 space-y-2 text-sm">
                                    <div className="flex items-center justify-between">
                                        <dt className="text-gray-600">Total Quantity</dt>
                                        <dd className="font-medium text-gray-900">
                                            {orderQty}
                                        </dd>
                                    </div>
                                </dl>
                                <dl className="mt-3 space-y-2 text-sm">
                                    <div className="flex items-center justify-between">
                                        <dt className="text-gray-600">Total Price</dt>
                                        <dd className="font-medium text-gray-900">
                                            {price * orderQty}
                                        </dd>
                                    </div>
                                </dl>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default ItemDetail;