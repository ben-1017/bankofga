import { api } from './client.js';

export const customersApi = {
  register: (payload) => api.post('/api/customers/register', payload).then((r) => r.data),
  login: (payload) => api.post('/api/customers/login', payload).then((r) => r.data),
  profile: (id) => api.get(`/api/customers/${id}`).then((r) => r.data),
  update: (id, payload) => api.put(`/api/customers/${id}`, payload).then((r) => r.data),
};
