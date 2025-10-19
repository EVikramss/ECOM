import axios from 'axios';
import config from '../common/config.json';
import {
    GET_ORDER_NO_OP_NAME, GET_ORDER_NO
} from './queries/Queries.js'

export function postOrder(authInfo, orderJson) {
    axios.post(config.orderURL, orderJson, {
        headers: {
            Authorization: authInfo.user.access_token
        }
    }).then(result => {
        console.log(result);
    }).catch(error => {
        console.log(error);
    });
}

export function getOrderNo(authInfo) {
    const infoType = "seq";
    const userSub = "orderNo";

    const graphqlQuery = {
        "operationName": GET_ORDER_NO_OP_NAME,
        "query": GET_ORDER_NO,
        "variables": {}
    };

    return axios.post(config.userInfoURL, JSON.stringify(graphqlQuery), {
        headers: {
            Authorization: authInfo.user.access_token
        }
    });
}