import { api, unwrap } from './client.js';

export function getCustomer(id) {
  return unwrap(api.get(`/api/customers/${id}`));
}

export function updateCustomer(id, payload) {
  return unwrap(api.put(`/api/customers/${id}`, payload));
}
