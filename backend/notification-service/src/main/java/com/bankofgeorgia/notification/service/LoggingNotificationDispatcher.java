package com.bankofgeorgia.notification.service;

import com.bankofgeorgia.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// TODO: replace with SendGrid + Twilio dispatchers (Ben).
@Component
public class LoggingNotificationDispatcher implements NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationDispatcher.class);

    @Override
    public boolean dispatch(Notification notification) {
        log.info("[{}] {} -> customer {} acct {}: {}",
                notification.getChannel(),
                notification.getType(),
                notification.getCustomerId(),
                notification.getAccountId(),
                notification.getSubject());
        return true;
    }
}
