import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { customersApi } from '../api/customers.js';

const STORAGE_KEY = 'bog.customer';
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [customer, setCustomer] = useState(() => {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    if (customer) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(customer));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  }, [customer]);

  const value = useMemo(
    () => ({
      customer,
      isAuthenticated: customer != null,
      async login(username, password) {
        const data = await customersApi.login({ username, password });
        setCustomer(data);
        return data;
      },
      async register(payload) {
        const created = await customersApi.register(payload);
        if (created?.token) {
          setCustomer(created);
          return created;
        }
        const authed = await customersApi.login({
          username: payload.username,
          password: payload.password,
        });
        setCustomer(authed);
        return authed;
      },
      logout() {
        setCustomer(null);
      },
      setCustomer,
    }),
    [customer],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
