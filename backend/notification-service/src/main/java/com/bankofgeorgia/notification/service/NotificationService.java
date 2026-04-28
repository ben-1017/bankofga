package com.bankofgeorgia.notification.service;

import com.bankofgeorgia.notification.exception.NotificationNotFoundException;
import com.bankofgeorgia.notification.model.Notification;
import com.bankofgeorgia.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationDispatcher dispatcher;

    public NotificationService(NotificationRepository repository, NotificationDispatcher dispatcher) {
        this.repository = repository;
        this.dispatcher = dispatcher;
    }

    public Notification record(Notification notification) {
        notification.setCreatedAt(Instant.now());
        notification.setDelivered(false);
        Notification saved = repository.save(notification);

        boolean delivered = dispatcher.dispatch(saved);
        if (delivered) {
            saved.setDelivered(true);
            saved.setDeliveredAt(Instant.now());
            saved = repository.save(saved);
        }
        return saved;
    }

    public Notification findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("notification not found: " + id));
    }

    public List<Notification> findByCustomer(String customerId) {
        return repository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }
}
