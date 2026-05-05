package com.bankofgeorgia.transaction.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class WithdrawEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public WithdrawEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${app.topics.withdraw}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(WithdrawNotificationEvent event) {
        kafkaTemplate.send(topic, event.accountId(), event);
    }
}
