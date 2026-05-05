import { api, unwrap } from './client.js';

export function login({ username, password }) {
  return unwrap(api.post('/api/customers/login', { username, password }));
}

export function register(payload) {
  return unwrap(api.post('/api/customers/register', payload));
}
