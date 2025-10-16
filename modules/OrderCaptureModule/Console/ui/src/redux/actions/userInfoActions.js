import * as types from "./types/userInfoActionTypes";
import * as userInfoAPI from "../../api/userInfoAPI";
import { beginApiCall, apiCallError, endApiCall } from "./apiBoundaryActions";

export function createUserInfo(userInfo) {
    return { type: types.CREATE_USER_INFO, userInfo };
}

export function updateUserInfo(userInfo) {
    return { type: types.UPDATE_USER_INFO, userInfo };
}

export function deleteUserInfo(authInfo) {
    return { type: types.DELETE_USER_INFO, userInfo };
}

export function checkAndUpdateUserInfo(authInfo) {
    return function (dispatch) {
        dispatch(beginApiCall());
        return userInfoAPI.getUserInfo(authInfo)
            .then(response => {
                // if user present in db, store response in redux store
                // else if user not present, add to db
                if (response.data.data.getUserInfo) {
                    dispatch(createUserInfo(response.data.data.getUserInfo));
                } else {
                    storeUserInfo(dispatch, authInfo);
                }
                dispatch(endApiCall());
            }).catch(error => {
                dispatch(apiCallError(error));
            });
    };
}

function storeUserInfo(dispatch, authInfo) {
    dispatch(beginApiCall());
    userInfoAPI.createUserInfo(authInfo)
        .then(response => {
            if (response.data) {
                dispatch(createUserInfo(response));
            } else {
                dispatch(apiCallError(error));
            }
            dispatch(endApiCall());
        }).catch(error => {
            dispatch(apiCallError(error));
        });
}

/*
export function testSpinner() {
    return function (dispatch) {
        dispatch(beginApiCall());
        sleep(3000).then(response => {
           dispatch(endApiCall(error));
        }).catch(error => {
            dispatch(apiCallError(error));
        });
    }
}


function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}
*/