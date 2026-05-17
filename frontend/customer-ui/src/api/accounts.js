import { api } from './client.js';

export const accountsApi = {
  open: (payload) => api.post('/api/accounts', payload).then((r) => r.data),
  get: (id) => api.get(`/api/accounts/${id}`).then((r) => r.data),
  listByCustomer: (customerId) =>
    api.get(`/api/accounts/customer/${customerId}`).then((r) => r.data),
};
