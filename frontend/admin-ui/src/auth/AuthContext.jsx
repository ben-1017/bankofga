import { createContext, useCallback, useMemo, useState } from 'react';
import { login as loginRequest } from '../api/auth.js';
import { loadUser, saveUser } from './storage.js';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => loadUser());

  const login = useCallback(async (credentials) => {
    const profile = await loginRequest(credentials);
    saveUser(profile);
    setUser(profile);
    return profile;
  }, []);

  const logout = useCallback(() => {
    saveUser(null);
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, login, logout, isAuthenticated: Boolean(user) }),
    [user, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
