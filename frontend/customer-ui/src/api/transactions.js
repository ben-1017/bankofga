import { api } from './client.js';

export const transactionsApi = {
  deposit: (payload) => api.post('/api/transactions/deposit', payload).then((r) => r.data),
  withdraw: (payload) => api.post('/api/transactions/withdraw', payload).then((r) => r.data),
  listByAccount: (accountId) =>
    api.get(`/api/transactions/account/${accountId}`).then((r) => r.data),
  get: (id) => api.get(`/api/transactions/${id}`).then((r) => r.data),
};
