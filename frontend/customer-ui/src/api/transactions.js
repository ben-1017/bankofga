import { api, unwrap } from './client.js';

export function deposit(payload) {
  return unwrap(api.post('/api/transactions/deposit', payload));
}

export function withdraw(payload) {
  return unwrap(api.post('/api/transactions/withdraw', payload));
}

export function listTransactionsForAccount(accountId) {
  return unwrap(api.get(`/api/transactions/account/${accountId}`));
}

export function getTransaction(transactionId) {
  return unwrap(api.get(`/api/transactions/${transactionId}`));
}
