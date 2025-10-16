import '../common/Common.css';
import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';

function Home() {
    const [itemList, setItemList] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        const data = [
            {
                itemID: "i1",
                price: "100",
                currency: "INR",
                availability: "InStock",
                maxQty: 10,
                taxCode: "5",
                desc: "Earthen Bottle",
                imgurl: "https://tailwindcss.com/plus-assets/img/ecommerce-images/category-page-04-image-card-01.jpg"
            },
            {
                itemID: "i2",
                price: "100",
                currency: "INR",
                availability: "InStock",
                maxQty: 10,
                taxCode: "5",
                desc: "Nomad Tumbler",
                imgurl: "https://tailwindcss.com/plus-assets/img/ecommerce-images/category-page-04-image-card-02.jpg"
            },
            {
                itemID: "i3",
                price: "100",
                currency: "INR",
                availability: "InStock",
                maxQty: 10,
                taxCode: "5",
                desc: "Machined Mechanical Pencil",
                imgurl: "https://tailwindcss.com/plus-assets/img/ecommerce-images/category-page-04-image-card-04.jpg"
            },
            {
                itemID: "i4",
                price: "100",
                currency: "INR",
                availability: "InStock",
                maxQty: 10,
                taxCode: "5",
                desc: "Focus Card Tray",
                imgurl: "https://tailwindcss.com/plus-assets/img/ecommerce-images/category-page-04-image-card-05.jpg"
            },
            {
                itemID: "i5",
                price: "100",
                currency: "INR",
                availability: "InStock",
                maxQty: 10,
                taxCode: "5",
                desc: "Earthen Bottle",
                imgurl: "https://tailwindcss.com/plus-assets/img/ecommerce-images/category-page-04-image-card-01.jpg"
            },
            {
                itemID: "i6",
                price: "60",
                currency: "INR",
                availability: "InStock",
                maxQty: 10,
                taxCode: "5",
                desc: "Nomad Tumbler",
                imgurl: "https://tailwindcss.com/plus-assets/img/ecommerce-images/category-page-04-image-card-02.jpg"
            },
            {
                itemID: "i7",
                price: "50",
                currency: "INR",
                availability: "InStock",
                maxQty: 10,
                taxCode: "5",
                desc: "Focus Carry Pouch",
                imgurl: "https://tailwindcss.com/plus-assets/img/ecommerce-images/category-page-04-image-card-08.jpg"
            }
        ];
        setItemList(data);
    }, []);

    const showItem = (item) => {
        navigate('/itemDetail', { state: { data: item } });
    };

    return (
        <div className="bg-white headerSpacing">
            <div className="mx-auto max-w-xl px-4 py-16 sm:px-6 sm:py-24 lg:max-w-7xl lg:px-8">
                <h2 className="sr-only">Products</h2>

                <div className="grid grid-cols-1 gap-x-6 gap-y-10 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 xl:gap-x-8">
                    {
                        itemList.map((item) => (
                            <a onClick={(e) => {
                                e.preventDefault();
                                showItem(item);
                            }
                            } key={item.itemID} className="group cursor-pointer">
                                <img src={item.imgurl} className="aspect-square w-full rounded-lg bg-gray-200 object-cover xl:aspect-7/8" />
                                <h3 className="mt-4 text-sm text-gray-700">{item.desc}</h3>
                                <p className="mt-1 text-lg font-medium text-gray-900">
                                    {item.currency == "INR" ? "₹" : ""}{item.price}</p>
                            </a>
                        ))
                    }
                </div>
            </div>
        </div >
    )
}

export default Home;


