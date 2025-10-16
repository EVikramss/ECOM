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