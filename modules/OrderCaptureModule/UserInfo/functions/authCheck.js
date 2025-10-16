import { util } from '@aws-appsync/utils';

/**
 * Ensures the caller is authenticated via Cognito User Pool and extracts `sub`.
 * Stashes `ownerSub` for use by following functions in the pipeline.
 */
export function request(ctx) {
  // Cognito sub is available on identity or identity.claims depending on typings
  const sub = ctx.identity?.sub ?? ctx.identity?.claims?.sub;

  if (!sub) {
    util.unauthorized(); // throws 401/403
  }

  // Persist for downstream functions
  ctx.stash.ownerSub = sub;

  // No data source call here
  return {};
}

export function response(ctx) {
  return ctx.prev;
}