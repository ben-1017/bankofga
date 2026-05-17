import { createContext, useContext, useMemo, useState } from 'react';
import { loginEmployee } from '../api/admin.js';

const STORAGE_KEY = 'bankofga.admin.employee';

const AuthContext = createContext(null);

function loadStoredEmployee() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function AuthProvider({ children }) {
  const [employee, setEmployee] = useState(loadStoredEmployee);

  async function login(credentials) {
    const nextEmployee = await loginEmployee(credentials);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(nextEmployee));
    setEmployee(nextEmployee);
    return nextEmployee;
  }

  function logout() {
    localStorage.removeItem(STORAGE_KEY);
    setEmployee(null);
  }

  const value = useMemo(
    () => ({
      employee,
      isAuthenticated: Boolean(employee?.id),
      login,
      logout,
    }),
    [employee],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return value;
}
