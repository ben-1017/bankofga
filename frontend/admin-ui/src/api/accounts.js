import { api, unwrap } from './client.js';

export function listAccounts(params = {}) {
  return unwrap(api.get('/api/accounts', { params }));
}

export function getAccount(id) {
  return unwrap(api.get(`/api/accounts/${id}`));
}

export function listAccountsForCustomer(customerId) {
  return unwrap(api.get(`/api/accounts/customer/${customerId}`));
}

export function setAccountBalance(id, balance) {
  return unwrap(api.put(`/api/accounts/${id}/balance`, { balance }));
}
