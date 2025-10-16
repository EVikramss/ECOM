import * as types from "./types/apiBoundaryActionTypes";

export function beginApiCall() {
  return { type: types.BEGIN_API_CALL };
}

export function endApiCall() {
  return { type: types.END_API_CALL };
}

export function apiCallError() {
  return { type: types.API_CALL_ERROR };
}
