import { api } from './client.js';

export const notificationsApi = {
  listByCustomer: (customerId) =>
    api.get(`/api/notifications/customer/${customerId}`).then((r) => r.data),
};
