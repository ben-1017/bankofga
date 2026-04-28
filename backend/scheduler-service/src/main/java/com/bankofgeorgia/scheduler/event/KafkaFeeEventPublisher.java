package com.bankofgeorgia.scheduler.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaFeeEventPublisher implements FeeEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaFeeEventPublisher(KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${app.fee.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(ApplyMonthlyFeeEvent event) {
        kafkaTemplate.send(topic, event.accountId(), event);
    }
}
