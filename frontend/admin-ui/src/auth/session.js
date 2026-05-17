const STORAGE_KEY = 'bankofga.admin.employee';

export function loadStoredEmployee() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    localStorage.removeItem(STORAGE_KEY);
    return null;
  }
}

export function storeEmployee(employee) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(employee));
}

export function clearStoredEmployee() {
  localStorage.removeItem(STORAGE_KEY);
}

export function getStoredToken() {
  return loadStoredEmployee()?.token || '';
}
