const KEY = 'bog.admin.user';

export function loadUser() {
  try {
    const raw = window.localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function saveUser(user) {
  if (user) window.localStorage.setItem(KEY, JSON.stringify(user));
  else window.localStorage.removeItem(KEY);
}
