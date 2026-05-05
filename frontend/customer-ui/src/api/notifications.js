import { api, unwrap } from './client.js';

export function listNotificationsForCustomer(customerId) {
  return unwrap(api.get(`/api/notifications/customer/${customerId}`));
}

export function getNotification(notificationId) {
  return unwrap(api.get(`/api/notifications/${notificationId}`));
}
