import axios from 'axios';
import config from '../common/config.json';

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
