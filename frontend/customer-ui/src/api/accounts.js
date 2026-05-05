import { api, unwrap } from './client.js';

export function listAccountsForCustomer(customerId) {
  return unwrap(api.get(`/api/accounts/customer/${customerId}`));
}

export function getAccount(accountId) {
  return unwrap(api.get(`/api/accounts/${accountId}`));
}

export function openAccount(payload) {
  return unwrap(api.post('/api/accounts', payload));
}
