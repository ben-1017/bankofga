package com.bankofgeorgia.notification.controller;

import com.bankofgeorgia.notification.dto.NotificationResponse;
import com.bankofgeorgia.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(NotificationResponse.from(notificationService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<NotificationResponse>> byCustomer(@PathVariable String customerId) {
        List<NotificationResponse> body = notificationService.findByCustomer(customerId).stream()
                .map(NotificationResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }
}
