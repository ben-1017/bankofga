package com.bankofgeorgia.notification.service;

import com.bankofgeorgia.notification.exception.NotificationNotFoundException;
import com.bankofgeorgia.notification.model.DeliveryChannel;
import com.bankofgeorgia.notification.model.Notification;
import com.bankofgeorgia.notification.model.NotificationType;
import com.bankofgeorgia.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository repository;
    @Mock private NotificationDispatcher dispatcher;

    @InjectMocks private NotificationService service;

    @Test
    void record_persistsThenDispatches_andMarksDeliveredOnSuccess() {
        Notification n = new Notification();
        n.setCustomerId("c-1");
        n.setAccountId("a-1");
        n.setType(NotificationType.FEE_APPLIED);
        n.setChannel(DeliveryChannel.EMAIL);
        n.setSubject("subject");
        n.setBody("body");

        when(repository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification arg = inv.getArgument(0);
            if (arg.getId() == null) arg.setId("n-1");
            return arg;
        });
        when(dispatcher.dispatch(any(Notification.class))).thenReturn(true);

        Notification saved = service.record(n);

        assertThat(saved.getId()).isEqualTo("n-1");
        assertThat(saved.isDelivered()).isTrue();
        assertThat(saved.getDeliveredAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        verify(dispatcher).dispatch(any(Notification.class));
    }

    @Test
    void record_persists_butLeavesUndelivered_whenDispatcherFails() {
        Notification n = new Notification();
        n.setCustomerId("c-1");

        when(repository.save(any(Notification.class))).thenAnswer(inv -> {
            Notification arg = inv.getArgument(0);
            if (arg.getId() == null) arg.setId("n-1");
            return arg;
        });
        when(dispatcher.dispatch(any(Notification.class))).thenReturn(false);

        Notification saved = service.record(n);

        assertThat(saved.isDelivered()).isFalse();
        assertThat(saved.getDeliveredAt()).isNull();
    }

    @Test
    void findById_throwsNotFound_whenIdMissing() {
        when(repository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById("ghost"))
                .isInstanceOf(NotificationNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void findByCustomer_delegatesToRepository() {
        Notification a = new Notification();
        a.setId("n-1");
        when(repository.findByCustomerIdOrderByCreatedAtDesc("c-1")).thenReturn(List.of(a));

        List<Notification> result = service.findByCustomer("c-1");

        assertThat(result).extracting(Notification::getId).containsExactly("n-1");
    }
}
