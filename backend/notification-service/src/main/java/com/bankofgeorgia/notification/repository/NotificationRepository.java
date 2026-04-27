package com.bankofgeorgia.notification.repository;

import com.bankofgeorgia.notification.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(String customerId);
}
