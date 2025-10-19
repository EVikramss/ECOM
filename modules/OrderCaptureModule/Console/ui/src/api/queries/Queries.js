export const CREATE_USER_INFO_OP_NAME = "CreateUserInfo";
export const CREATE_USER_INFO = `mutation CreateUserInfo {
  createUserInfo(input: {
    infoType: "{0}",
    userSub: "{1}",
    data: "{2}"
  }) {
    infoType
    userSub
    data
  }
}`;

export const UPDATE_USER_INFO_OP_NAME = "UpdateUserInfo";
export const UPDATE_USER_INFO = `mutation UpdateUserInfo {
  updateUserInfo(input: {
    infoType: "{0}",
    userSub: "{1}",
    data: "{2}"
  }) {
    infoType
    userSub
    data
  }
}`;

export const GET_USER_INFO_OP_NAME = "GetUserInfo";
export const GET_USER_INFO = `query GetUserInfo {
  getUserInfo(infoType: "{0}", userSub: "{1}") {
    infoType
    userSub
    data
  }
}`;

export const GET_ITEM_INFO_OP_NAME = "getItemInfo";
export const GET_ITEM_INFO = `query GetItemInfo {
  getItemInfo(infoType: "{0}", itemID: "{1}") {
    infoType
    itemID
    data
  }
}`;

export const GET_ORDER_NO_OP_NAME = "generateOrderNo";
export const GET_ORDER_NO = `query GenerateOrderNo {
  generateOrderNo
}`;