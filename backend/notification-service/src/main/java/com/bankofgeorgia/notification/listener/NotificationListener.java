package com.bankofgeorgia.notification.listener;

import com.bankofgeorgia.notification.dto.ApplyMonthlyFeeEvent;
import com.bankofgeorgia.notification.dto.LowMaintenanceNotificationEvent;
import com.bankofgeorgia.notification.dto.WithdrawNotificationEvent;
import com.bankofgeorgia.notification.model.DeliveryChannel;
import com.bankofgeorgia.notification.model.Notification;
import com.bankofgeorgia.notification.model.NotificationType;
import com.bankofgeorgia.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private final NotificationService notificationService;

    public NotificationListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.topics.apply-monthly-fee}")
    public void onFeeApplied(ApplyMonthlyFeeEvent event) {
        Notification n = new Notification();
        n.setCustomerId(event.customerId());
        n.setAccountId(event.accountId());
        n.setType(NotificationType.FEE_APPLIED);
        n.setChannel(DeliveryChannel.EMAIL);
        n.setSubject("Monthly maintenance fee applied");
        n.setBody(String.format(
                "A monthly maintenance fee of $%s was applied to your account ending %s.",
                event.feeAmount().toPlainString(), tail(event.accountId())));
        notificationService.record(n);
    }

    @KafkaListener(topics = "${app.topics.withdraw}")
    public void onWithdraw(WithdrawNotificationEvent event) {
        Notification n = new Notification();
        n.setCustomerId(event.customerId());
        n.setAccountId(event.accountId());
        n.setType(NotificationType.WITHDRAW);
        n.setChannel(DeliveryChannel.SMS);
        n.setSubject("Withdrawal confirmation");
        n.setBody(String.format(
                "Withdrew $%s from account ending %s. New balance: $%s.",
                event.amount().toPlainString(), tail(event.accountId()),
                event.balanceAfter().toPlainString()));
        notificationService.record(n);
    }

    @KafkaListener(topics = "${app.topics.low-balance}")
    public void onLowBalance(LowMaintenanceNotificationEvent event) {
        Notification n = new Notification();
        n.setCustomerId(event.customerId());
        n.setAccountId(event.accountId());
        n.setType(NotificationType.LOW_BALANCE);
        n.setChannel(DeliveryChannel.EMAIL);
        n.setSubject("Low balance alert");
        n.setBody(String.format(
                "Your balance ($%s) is below the maintenance threshold ($%s) on account ending %s.",
                event.balance().toPlainString(), event.threshold().toPlainString(),
                tail(event.accountId())));
        notificationService.record(n);
    }

    private static String tail(String accountId) {
        return accountId == null || accountId.length() < 4 ? "????" : accountId.substring(accountId.length() - 4);
    }
}
