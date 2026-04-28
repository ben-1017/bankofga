package com.bankofgeorgia.notification.listener;

import com.bankofgeorgia.notification.dto.ApplyMonthlyFeeEvent;
import com.bankofgeorgia.notification.dto.LowMaintenanceNotificationEvent;
import com.bankofgeorgia.notification.dto.WithdrawNotificationEvent;
import com.bankofgeorgia.notification.model.DeliveryChannel;
import com.bankofgeorgia.notification.model.Notification;
import com.bankofgeorgia.notification.model.NotificationType;
import com.bankofgeorgia.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock private NotificationService notificationService;

    @Test
    void onFeeApplied_persistsFeeNotification_viaEmail() {
        NotificationListener listener = new NotificationListener(notificationService);
        ApplyMonthlyFeeEvent event = new ApplyMonthlyFeeEvent(
                "acct-12345", "cust-1", new BigDecimal("5.00"), Instant.now());

        listener.onFeeApplied(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).record(captor.capture());
        Notification n = captor.getValue();

        assertThat(n.getType()).isEqualTo(NotificationType.FEE_APPLIED);
        assertThat(n.getChannel()).isEqualTo(DeliveryChannel.EMAIL);
        assertThat(n.getCustomerId()).isEqualTo("cust-1");
        assertThat(n.getAccountId()).isEqualTo("acct-12345");
        assertThat(n.getBody()).contains("$5.00").contains("2345");
    }

    @Test
    void onWithdraw_persistsWithdrawNotification_viaSms() {
        NotificationListener listener = new NotificationListener(notificationService);
        WithdrawNotificationEvent event = new WithdrawNotificationEvent(
                "tx-1", "acct-99887766", "cust-2",
                new BigDecimal("100.00"), new BigDecimal("250.00"), Instant.now());

        listener.onWithdraw(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).record(captor.capture());
        Notification n = captor.getValue();

        assertThat(n.getType()).isEqualTo(NotificationType.WITHDRAW);
        assertThat(n.getChannel()).isEqualTo(DeliveryChannel.SMS);
        assertThat(n.getBody()).contains("$100.00").contains("$250.00").contains("7766");
    }

    @Test
    void onLowBalance_persistsLowBalanceNotification_viaEmail() {
        NotificationListener listener = new NotificationListener(notificationService);
        LowMaintenanceNotificationEvent event = new LowMaintenanceNotificationEvent(
                "acct-1111", "cust-3",
                new BigDecimal("50.00"), new BigDecimal("100.00"), Instant.now());

        listener.onLowBalance(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationService).record(captor.capture());
        Notification n = captor.getValue();

        assertThat(n.getType()).isEqualTo(NotificationType.LOW_BALANCE);
        assertThat(n.getChannel()).isEqualTo(DeliveryChannel.EMAIL);
        assertThat(n.getBody()).contains("$50.00").contains("$100.00");
    }
}
