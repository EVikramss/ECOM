export default {
  // tracking ongoing api calls
  apiCallsInProgress: 0,
  // user info
  userInfo: {},
  // user selected address on checkout page
  userSelectedAddress: {},
  // all addresses for a user
  addressListState: {
    userAddressList: {},
    isAddressEntryPresent: 0
  },
  // track cart
  cartState: {
    cartItems: {},
    isCartEntryPresent: 0
  },
  // track order
  orderState: {
    orderNo: null,
    isOrderExported: false
  },
  // item and search params
  skuData: {
    skuList: [],
    pageData: {
      // pageNumber: 0,
      lastPageReached: false
    },
    searchParams: {
      pageSize: 30
    }
  }
};
