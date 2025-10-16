import { useParams, useLocation } from 'react-router-dom';
import '../common/Common.css';
import { useNavigate } from 'react-router-dom';
import { useMemo } from 'react';
import { useAuth } from "react-oidc-context";
import { useDispatch, useSelector } from "react-redux";
import { removeFromCart, changeCartQty, clearCart } from "../redux/actions/cartActions";

const formatCurrency = (value, currency = 'INR') =>
    new Intl.NumberFormat('en-IN', { style: 'currency', currency }).format(value);

function Cart() {
    const cartItems = useSelector((state) => state.cartState.cartItems);
    const auth = useAuth();
    const dispatch = useDispatch();
    const navigate = useNavigate();

    // Convert object to array for rendering
    const items = useMemo(() => Object.values(cartItems || {}), [cartItems]);

    const handleRemove = (itemID) => {
        dispatch(removeFromCart(itemID, auth));
    };

    const handleIncrease = (itemID) => {
        dispatch(changeCartQty(itemID, 1, auth));
    };

    const handleDecrease = (itemID) => {
        dispatch(changeCartQty(itemID, -1, auth));
    };

    const checkOut = () => {
        if (!auth.isAuthenticated) {
            navigate('/login');
        } else {
            navigate('/address');
        }
    };

    // Totals
    const { subTotal } = useMemo(() => {
        let sub = 0;
        for (const it of items) {
            const unit = Number(it.price) || 0;
            const qty = Number(it.orderQty ?? 1);
            const line = unit * qty;
            sub += line;
        }
        return { subTotal: sub };
    }, [items]);

    const isEmpty = items.length === 0;

    return (
        <div className="bg-white">
            <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8 py-8">
                <h1 className="text-2xl font-bold tracking-tight text-gray-900">Shopping Cart</h1>

                {isEmpty ? (
                    <div className="mt-12 flex flex-col items-center justify-center rounded-lg border border-dashed border-gray-300 p-12 text-center">
                        <svg
                            className="h-10 w-10 text-gray-300"
                            viewBox="0 0 24 24"
                            fill="none"
                            aria-hidden="true"
                        >
                            <path
                                d="M3 3h2l.4 2M7 13h10l3-8H6.4M7 13l-1.293 4.293A1 1 0 0 0 6.667 19h10.666a1 1 0 0 0 .96-.737L20 13M7 13l.5 2M17 13l-.5 2"
                                stroke="currentColor"
                                strokeWidth="1.5"
                                strokeLinecap="round"
                                strokeLinejoin="round"
                            />
                        </svg>
                        <p className="mt-4 text-sm text-gray-500">Your cart is empty.</p>
                        <a href="#" onClick={() => navigate('/')}>Continue shopping</a>
                    </div>
                ) : (
                    <div className="mt-8 grid grid-cols-1 gap-8 lg:grid-cols-12">
                        {/* Left: Items */}
                        <section className="lg:col-span-8">
                            <ul role="list" className="divide-y divide-gray-200 border-t border-gray-200">
                                {items.map((item) => {
                                    const {
                                        itemID,
                                        desc,
                                        imgurl,
                                        availability,
                                        maxQty = 10,
                                        price,
                                        currency = 'INR',
                                    } = item;

                                    const qty = Number(item.orderQty ?? 1);
                                    const unitPrice = Number(price) || 0;
                                    const lineTotal = unitPrice * qty;

                                    const canDecrease = qty > 1;
                                    const canIncrease = qty < maxQty;

                                    return (
                                        <li key={itemID} className="flex py-6 sm:py-8">
                                            <div className="h-24 w-24 flex-shrink-0 overflow-hidden rounded-md border border-gray-200 sm:h-32 sm:w-32">
                                                <img
                                                    src={imgurl}
                                                    alt={desc}
                                                    className="h-full w-full object-cover object-center"
                                                    loading="lazy"
                                                />
                                            </div>

                                            <div className="ml-4 flex flex-1 flex-col sm:ml-6">
                                                <div className="flex justify-between">
                                                    <h3 className="text-sm sm:text-base font-medium text-gray-900">
                                                        {desc}
                                                    </h3>
                                                    <p className="ml-4 text-sm sm:text-base font-medium text-gray-900">
                                                        {formatCurrency(unitPrice, currency)}
                                                    </p>
                                                </div>

                                                <p className="mt-1 text-xs text-gray-500">
                                                    {availability === 'InStock' ? 'In stock' : 'Out of stock'}
                                                </p>

                                                <div className="mt-4 flex flex-1 items-end justify-between">
                                                    {/* Quantity controls */}
                                                    <div>
                                                        <div className="hidden sm:inline-flex items-center rounded-md border border-gray-300">
                                                            <button
                                                                type="button"
                                                                aria-label={`Decrease quantity for ${desc}`}
                                                                className={`px-2 py-1 text-gray-700 hover:bg-gray-50 ${!canDecrease ? 'opacity-40 cursor-not-allowed' : ''
                                                                    }`}
                                                                onClick={() => canDecrease && handleDecrease(itemID)}
                                                                disabled={!canDecrease}
                                                            >
                                                                −
                                                            </button>
                                                            <span className="min-w-10 px-3 text-center text-sm text-gray-900">
                                                                {qty}
                                                            </span>
                                                            <button
                                                                type="button"
                                                                aria-label={`Increase quantity for ${desc}`}
                                                                className={`px-2 py-1 text-gray-700 hover:bg-gray-50 ${!canIncrease ? 'opacity-40 cursor-not-allowed' : ''
                                                                    }`}
                                                                onClick={() => canIncrease && handleIncrease(itemID)}
                                                                disabled={!canIncrease}
                                                            >
                                                                +
                                                            </button>
                                                        </div>
                                                    </div>

                                                    <div className="text-right">
                                                        <p className="text-sm font-medium text-gray-900">
                                                            {formatCurrency(lineTotal, currency)}
                                                        </p>
                                                        <div className="mt-2">
                                                            <button
                                                                type="button"
                                                                className="text-sm font-medium text-indigo-600 hover:text-indigo-500"
                                                                onClick={() => handleRemove(itemID)}
                                                            >
                                                                Remove
                                                            </button>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </li>
                                    );
                                })}
                            </ul>
                        </section>

                        {/* Right: Summary */}
                        <aside className="lg:col-span-4">
                            <div className="rounded-lg border border-gray-200 bg-gray-50 p-6">
                                <h2 className="text-base font-medium text-gray-900">Order summary</h2>

                                <dl className="mt-4 space-y-2 text-sm">
                                    <div className="flex items-center justify-between">
                                        <dt className="text-gray-600">Subtotal</dt>
                                        <dd className="font-medium text-gray-900">{formatCurrency(subTotal)}</dd>
                                    </div>

                                    <div className="flex items-center justify-between">
                                        <dt className="text-gray-600">Shipping Charge</dt>
                                        <dd className="font-medium text-gray-900">0.00</dd>
                                    </div>

                                    <div className="mt-4 border-t border-gray-200 pt-4 flex items-center justify-between text-base">
                                        <dt className="font-medium text-gray-900">Total</dt>
                                        <dd className="font-semibold text-gray-900">
                                            {formatCurrency(subTotal)}
                                        </dd>
                                    </div>
                                </dl>

                                <button
                                    type="button"
                                    disabled={isEmpty}
                                    onClick={() => checkOut()}
                                    className={`mt-6 w-full rounded-md px-4 py-2 text-sm font-semibold shadow-sm focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 ${isEmpty
                                        ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
                                        : 'bg-indigo-600 text-white hover:bg-indigo-500 focus-visible:outline-indigo-600'
                                        }`}
                                >
                                    Checkout
                                </button>
                            </div>
                        </aside>
                    </div>
                )}
            </div>
        </div >
    )
}

export default Cart;