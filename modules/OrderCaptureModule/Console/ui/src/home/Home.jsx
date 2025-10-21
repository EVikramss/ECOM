import '../common/Common.css';
import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from "react-redux";
import { fetchSKUList, storeSkuSearchParams } from '../redux/actions/skuListActions';
import config from '../common/config.json';

function Home() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const skuList = useSelector(state => state.skuData.skuList);
    const searchParams = useSelector(state => state.skuData.searchParams);
    const pageData = useSelector(state => state.skuData.pageData);

    // calculate page number manually
    const pageNumber = pageData.doc ? ((pageData.doc + 1) / searchParams.pageSize) - 1 : 0;

    useEffect(() => {
        dispatch(fetchSKUList(searchParams));
    }, [searchParams]);

    const previousPage = () => {
        if (pageNumber > 0) {
            const newDoc = pageData.doc - (2 * searchParams.pageSize);

            const newSearchParams = { ...searchParams };
            newSearchParams.pageResults = 1;
            newSearchParams.score = pageData.score;
            newSearchParams.doc = newDoc;
            newSearchParams.shardIndex = pageData.shardIndex;
            dispatch(storeSkuSearchParams(newSearchParams));
        }
    }

    const nextPage = () => {
        const newSearchParams = { ...searchParams };
        newSearchParams.pageResults = 1;
        newSearchParams.score = pageData.score;
        newSearchParams.doc = pageData.doc;
        newSearchParams.shardIndex = pageData.shardIndex;
        dispatch(storeSkuSearchParams(newSearchParams));
    }

    const onChangeResultsPerPage = (pageSize) => {
        searchParams.pageSize = pageSize;
        dispatch(storeSkuSearchParams(searchParams));
    }

    const showItem = (item) => {
        navigate('/itemDetail', { state: { data: item } });
    };

    return (
        <div className="bg-white headerSpacing relative">

            <div className="absolute top-4 left-4 flex flex-col items-end">
                <select
                    id="pageSize"
                    name="pageSize"
                    onChange={(e) => onChangeResultsPerPage(Number(e.target.value))}
                    className="rounded-md border-gray-300 shadow-sm px-3 py-2 text-sm font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    defaultValue={30}
                >
                    <option value={30}>30</option>
                    <option value={60}>60</option>
                    <option value={90}>90</option>
                </select>
                <label htmlFor="pageSize" className="text-sm font-small text-gray-700 mb-1">
                    per page
                </label>
            </div>

            <div className="mx-auto max-w-xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                <h2 className="sr-only">Products</h2>

                <div className="grid grid-cols-1 gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 xl:gap-x-8">
                    {
                        skuList.map((item) => (
                            <a onClick={(e) => {
                                e.preventDefault();
                                showItem(item);
                            }
                            } key={item.itemID} className="group cursor-pointer">
                                <img src={config.staticContentPrefix + item.imgUrl} className="aspect-square w-full rounded-lg bg-gray-200 object-cover xl:aspect-7/8" />
                                <h3 className="mt-4 text-sm text-gray-700">{item.desc}</h3>
                                <p className="mt-1 text-lg font-medium text-gray-900">
                                    {item.currency == "INR" ? "₹" : ""}{item.price}</p>
                            </a>
                        ))
                    }
                </div>

                <div className="flex items-center justify-center space-x-4">
                    <button
                        type="button"
                        onClick={previousPage}
                        className={`paraSpacing inline-flex items-center justify-center rounded-md px-4 py-2 
                            text-sm font-semibold ${pageNumber < 1
                                ? "bg-gray-200 text-gray-500 cursor-not-allowed"
                                : "shadow-sm text-black hover:bg-gray-100 offset ml-2"
                            }`}
                        disabled={pageNumber < 1}
                    >
                        &lt;
                    </button>

                    <button
                        type="button"
                        onClick={nextPage}
                        className={`paraSpacing inline-flex items-center justify-center rounded-md px-4 py-2 
                            text-sm font-semibold ${pageData.lastPageReached
                                ? "bg-gray-200 text-gray-500 cursor-not-allowed"
                                : "shadow-sm text-black hover:bg-gray-100 offset ml-2"
                            }`}
                        disabled={pageData.lastPageReached}
                    >
                        &gt;
                    </button>
                </div>
            </div>


        </div >
    )
}

export default Home;


