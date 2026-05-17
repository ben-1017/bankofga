import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { loginEmployee } from '../api/admin.js';
import { clearStoredEmployee, loadStoredEmployee, storeEmployee } from './session.js';

const AuthContext = createContext(null);
const DEFAULT_IDLE_TIMEOUT_MINUTES = 15;
const ACTIVITY_EVENTS = ['click', 'keydown', 'mousedown', 'mousemove', 'scroll', 'touchstart'];

function getIdleTimeoutMinutes() {
  const configured = Number(import.meta.env.VITE_IDLE_TIMEOUT_MINUTES);
  return Number.isFinite(configured) && configured > 0
    ? configured
    : DEFAULT_IDLE_TIMEOUT_MINUTES;
}

const idleTimeoutMinutes = getIdleTimeoutMinutes();
const idleTimeoutMs = idleTimeoutMinutes * 60 * 1000;

export function AuthProvider({ children }) {
  const [employee, setEmployee] = useState(loadStoredEmployee);
  const [authNotice, setAuthNotice] = useState('');

  const login = useCallback(async function login(credentials) {
    const nextEmployee = await loginEmployee(credentials);
    storeEmployee(nextEmployee);
    setEmployee(nextEmployee);
    setAuthNotice('');
    return nextEmployee;
  }, []);

  const logout = useCallback(function logout(reason) {
    clearStoredEmployee();
    setEmployee(null);
    setAuthNotice(
      reason === 'idle'
        ? `You were signed out after ${idleTimeoutMinutes} minutes of inactivity.`
        : '',
    );
  }, []);

  const clearAuthNotice = useCallback(function clearAuthNotice() {
    setAuthNotice('');
  }, []);

  useEffect(() => {
    if (!employee?.id || !employee?.token) {
      return undefined;
    }

    let timeoutId;
    let lastActivityAt = Date.now();

    function scheduleTimeout() {
      window.clearTimeout(timeoutId);
      const remainingMs = Math.max(idleTimeoutMs - (Date.now() - lastActivityAt), 0);
      timeoutId = window.setTimeout(() => {
        logout('idle');
      }, remainingMs);
    }

    function recordActivity() {
      lastActivityAt = Date.now();
      scheduleTimeout();
    }

    function checkVisibility() {
      if (document.hidden) {
        return;
      }
      if (Date.now() - lastActivityAt >= idleTimeoutMs) {
        logout('idle');
        return;
      }
      scheduleTimeout();
    }

    scheduleTimeout();
    ACTIVITY_EVENTS.forEach((eventName) => {
      window.addEventListener(eventName, recordActivity, { passive: true });
    });
    document.addEventListener('visibilitychange', checkVisibility);

    return () => {
      window.clearTimeout(timeoutId);
      ACTIVITY_EVENTS.forEach((eventName) => {
        window.removeEventListener(eventName, recordActivity);
      });
      document.removeEventListener('visibilitychange', checkVisibility);
    };
  }, [employee?.id, employee?.token, logout]);

  const value = useMemo(
    () => ({
      employee,
      isAuthenticated: Boolean(employee?.id && employee?.token),
      authNotice,
      clearAuthNotice,
      login,
      logout,
    }),
    [authNotice, clearAuthNotice, employee, login, logout],
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
