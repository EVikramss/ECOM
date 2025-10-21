import axios from 'axios';
import config from '../common/config.json';

export function getSKUAvailability(skuID) {
    const infoType = config.entity;
    let urlVal = config.itemInfoURL + "info?itemID=" + skuID + "&infoType=" + infoType;

    return axios.get(urlVal);
}

function format(str, ...args) {
    return str.replace(/{(\d+)}/g, (match, index) => args[index] ?? match);
}