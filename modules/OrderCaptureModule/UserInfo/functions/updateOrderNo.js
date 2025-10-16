import { util } from '@aws-appsync/utils';
import { update } from '@aws-appsync/utils/dynamodb';

/**
 * Sends a request to get orderNo from the DynamoDB table.
 * @param {import('@aws-appsync/utils').Context<{userSub: unknown; infoType: unknown;}>} ctx the context
 * @returns {import('@aws-appsync/utils').DynamoDBGetItemRequest} the request
 */
export function request(ctx) {
	// fetch current orderNo value
    const key = { "userSub" : "orderNo", "infoType" : "seq" };
	
	// increment counter
	const counterVal = +ctx.prev.result;
	const newCounterVal = counterVal + 1;
	
	const condition = {};
    condition["data"] = { eq: "" + counterVal };
	
	// update incremented counter value
	return update({
        key,
        update: {data: "" + newCounterVal},
        condition
    })
}

/**
 * Returns the fetched DynamoDB item.
 * @param {import('@aws-appsync/utils').Context} ctx the context
 * @returns {*} the DynamoDB item
 */
export function response(ctx) {
    const { error, result } = ctx;
    if (error) {
        return util.appendError(error.message, error.type, result);
    }
    return ctx.prev.result;
}