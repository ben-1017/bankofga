package com.bankofgeorgia.notification.service;

import com.bankofgeorgia.notification.model.Notification;

public interface NotificationDispatcher {
    boolean dispatch(Notification notification);
}
