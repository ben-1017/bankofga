import { api, unwrap } from './client.js';

export function login({ email, password }) {
  return unwrap(api.post('/api/employees/login', { email, password }));
}
