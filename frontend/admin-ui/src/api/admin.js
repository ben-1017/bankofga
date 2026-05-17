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

export async function listCustomers() {
  const { data } = await api.get('/api/employees/customers');
  return data;
}

export async function getCustomer(id) {
  const { data } = await api.get(`/api/employees/customers/${id}`);
  return data;
}

export async function listAccounts({ page = 0, size = 20 } = {}) {
  const { data } = await api.get('/api/accounts', { params: { page, size } });
  return data;
}

export async function getAccount(id) {
  const { data } = await api.get(`/api/accounts/${id}`);
  return data;
}

export async function listCustomerAccounts(customerId) {
  const { data } = await api.get(`/api/accounts/customer/${customerId}`);
  return data;
}

export async function listAccountTransactions(accountId) {
  const { data } = await api.get(`/api/transactions/account/${accountId}`);
  return data;
}
