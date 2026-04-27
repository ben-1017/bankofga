package com.bankofgeorgia.notification.dto;

import com.bankofgeorgia.notification.model.DeliveryChannel;
import com.bankofgeorgia.notification.model.Notification;
import com.bankofgeorgia.notification.model.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        String id,
        String customerId,
        String accountId,
        NotificationType type,
        DeliveryChannel channel,
        String subject,
        String body,
        boolean delivered,
        Instant createdAt,
        Instant deliveredAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getCustomerId(), n.getAccountId(),
                n.getType(), n.getChannel(), n.getSubject(), n.getBody(),
                n.isDelivered(), n.getCreatedAt(), n.getDeliveredAt()
        );
    }
}
