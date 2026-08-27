package com.url.shortener.service.kafka;

import com.url.shortener.dto.AnalyticsDTO;

/**
 * Interface for Kafka Producer operations
 * Defines contract for sending analytics events to Kafka topic
 */
public interface KafkaAnalyticsProducer {

    /**
     * Send analytics event to Kafka topic
     * @param analyticsDTO the analytics data to send
     */
    void sendAnalyticsEvent(AnalyticsDTO analyticsDTO);

    /**
     * Send analytics event with custom topic
     * @param topic the Kafka topic name
     * @param analyticsDTO the analytics data to send
     */
    void sendAnalyticsEvent(String topic, AnalyticsDTO analyticsDTO);
}
