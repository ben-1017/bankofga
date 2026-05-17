import { api } from './client.js';

export async function loginEmployee(credentials) {
  const { data } = await api.post('/api/employees/login', credentials);
  return data;
}

export async function listProducts() {
  const { data } = await api.get('/api/products');
  return data;
}

export async function getProduct(id) {
  const { data } = await api.get(`/api/products/${id}`);
  return data;
}

export async function createProduct(payload) {
  const { data } = await api.post('/api/products', payload);
  return data;
}

export async function updateProduct(id, payload) {
  const { data } = await api.put(`/api/products/${id}`, payload);
  return data;
}

export async function setProductStatus(id, active) {
  const { data } = await api.patch(`/api/products/${id}/status`, { active });
  return data;
}
