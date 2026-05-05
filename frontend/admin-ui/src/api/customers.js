import { api, unwrap } from './client.js';

export function listCustomers() {
  return unwrap(api.get('/api/employees/customers'));
}

export function getCustomer(id) {
  return unwrap(api.get(`/api/employees/customers/${id}`));
}
