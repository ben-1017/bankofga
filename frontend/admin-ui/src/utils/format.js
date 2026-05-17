export function formatMoney(value) {
  const amount = Number(value ?? 0);
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

export function formatDateTime(value) {
  if (!value) {
    return 'N/A';
  }

  return new Intl.DateTimeFormat('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value));
}

export function formatLabel(value) {
  return value?.replaceAll('_', ' ') || 'N/A';
}

export function shortId(value) {
  return value ? `${value.slice(0, 8)}...` : 'N/A';
}
