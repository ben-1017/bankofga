package com.bankofgeorgia.scheduler.event;

public interface FeeEventPublisher {
    void publish(ApplyMonthlyFeeEvent event);
}
