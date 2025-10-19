import axios from 'axios';
import config from '../common/config.json';

export function fetchSKUList(searchParams) {
    return axios.get(appendSearchParams(config.skuURL, searchParams));
}

function appendSearchParams(url, searchParams) {
    const urlObj = new URL(url);

    if (searchParams != null) {
        Object.entries(searchParams).forEach(([key, value]) => {
            urlObj.searchParams.append(key, value);
        });
    }

    return urlObj.toString();
}