import { api } from './client.js';

export const productsApi = {
  list: () => api.get('/api/products').then((r) => r.data),
  get: (id) => api.get(`/api/products/${id}`).then((r) => r.data),
};
