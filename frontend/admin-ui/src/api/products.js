import { api, unwrap } from './client.js';

export function listProducts() {
  return unwrap(api.get('/api/products'));
}

export function getProduct(id) {
  return unwrap(api.get(`/api/products/${id}`));
}

export function createProduct(payload) {
  return unwrap(api.post('/api/products', payload));
}

export function updateProduct(id, payload) {
  return unwrap(api.put(`/api/products/${id}`, payload));
}

export function setProductStatus(id, active) {
  return unwrap(api.patch(`/api/products/${id}/status`, { active }));
}
