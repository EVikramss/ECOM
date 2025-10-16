import { util } from '@aws-appsync/utils';
import { remove } from '@aws-appsync/utils/dynamodb';

/**
 * Deletes an item with userSub `ctx.args.input.userSub` and infoType `ctx.args.input.infoType` from the DynamoDB table.
 * @param {import('@aws-appsync/utils').Context<{input: {userSub: unknown; infoType: unknown;}}>} ctx the context
 * @returns {import('@aws-appsync/utils').DynamoDBDeleteItemRequest} the request
 */
export function request(ctx) {
    const { userSub, infoType } = ctx.args.input;
    const key = { userSub, infoType };
    return remove({
        key,
    })
}

/**
 * Returns the deleted item. Throws an error if the operation failed.
 * @param {import('@aws-appsync/utils').Context} ctx the context
 * @returns {*} the deleted item
 */
export function response(ctx) {
    const { error, result } = ctx;
    if (error) {
        return util.appendError(error.message, error.type, result);
    }
    return result;
}
