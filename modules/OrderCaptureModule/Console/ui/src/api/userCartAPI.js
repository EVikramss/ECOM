import {
    CREATE_USER_INFO, CREATE_USER_INFO_OP_NAME, GET_USER_INFO, GET_USER_INFO_OP_NAME,
    UPDATE_USER_INFO_OP_NAME, UPDATE_USER_INFO
} from './queries/Queries.js'
import axios from 'axios';
import config from '../common/config.json';

export function createCart(cartItems, authInfo) {
    const infoType = "cart";
    const userSub = authInfo.user.profile.sub;

    const graphqlQuery = {
        "operationName": CREATE_USER_INFO_OP_NAME,
        "query": format(CREATE_USER_INFO, infoType, userSub, cartItems),
        "variables": {}
    };

    return axios.post(config.userInfoURL, JSON.stringify(graphqlQuery), {
        headers: {
            Authorization: authInfo.user.access_token
        }
    });
}

export function updateCart(cartItems, authInfo) {
    const infoType = "cart";
    const userSub = authInfo.user.profile.sub;

    const graphqlQuery = {
        "operationName": UPDATE_USER_INFO_OP_NAME,
        "query": format(UPDATE_USER_INFO, infoType, userSub, cartItems),
        "variables": {}
    };

    return axios.post(config.userInfoURL, JSON.stringify(graphqlQuery), {
        headers: {
            Authorization: authInfo.user.access_token
        }
    });
}

export function getCart(authInfo) {
    const infoType = "cart";
    const userSub = authInfo.user.profile.sub;

    const graphqlQuery = {
        "operationName": GET_USER_INFO_OP_NAME,
        "query": format(GET_USER_INFO, infoType, userSub),
        "variables": {}
    };

    return axios.post(config.userInfoURL, JSON.stringify(graphqlQuery), {
        headers: {
            Authorization: authInfo.user.access_token
        }
    });
}

function format(str, ...args) {
    return str.replace(/{(\d+)}/g, (match, index) => args[index] ?? match);
}