import { createContext, useContext, useMemo, useState } from 'react';
import { loginEmployee } from '../api/admin.js';
import { clearStoredEmployee, loadStoredEmployee, storeEmployee } from './session.js';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [employee, setEmployee] = useState(loadStoredEmployee);

  async function login(credentials) {
    const nextEmployee = await loginEmployee(credentials);
    storeEmployee(nextEmployee);
    setEmployee(nextEmployee);
    return nextEmployee;
  }

  function logout() {
    clearStoredEmployee();
    setEmployee(null);
  }

  const value = useMemo(
    () => ({
      employee,
      isAuthenticated: Boolean(employee?.id && employee?.token),
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
