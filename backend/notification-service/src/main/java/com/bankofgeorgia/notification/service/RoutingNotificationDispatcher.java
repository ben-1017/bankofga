package com.bankofgeorgia.notification.service;

import com.bankofgeorgia.notification.model.DeliveryChannel;
import com.bankofgeorgia.notification.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class RoutingNotificationDispatcher implements NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(RoutingNotificationDispatcher.class);

    private final SendGridEmailDispatcher email;
    private final TwilioSmsDispatcher sms;

    public RoutingNotificationDispatcher(SendGridEmailDispatcher email, TwilioSmsDispatcher sms) {
        this.email = email;
        this.sms = sms;
    }

    @Override
    public boolean dispatch(Notification notification) {
        DeliveryChannel channel = notification.getChannel();
        if (channel == DeliveryChannel.EMAIL) {
            return email.send(notification);
        }
        if (channel == DeliveryChannel.SMS) {
            return sms.send(notification);
        }
        log.warn("no dispatcher for channel {}", channel);
        return false;
    }
}
