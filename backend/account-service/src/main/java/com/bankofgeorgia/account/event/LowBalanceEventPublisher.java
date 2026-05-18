package com.bankofgeorgia.account.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LowBalanceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LowBalanceEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public LowBalanceEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                    @Value("${app.topics.low-balance}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(LowBalanceEvent event) {
        try {
            kafkaTemplate.send(topic, event.accountId(), event);
        } catch (Exception ex) {
            // A notification must never fail a core balance operation; the
            // alert is best-effort.
            log.warn("failed to publish low-balance event for account {}: {}",
                    event.accountId(), ex.getMessage());
        }
    }
}
