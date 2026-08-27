package com.url.shortener.service.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.url.shortener.dto.AnalyticsDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Producer Implementation for Analytics Events
 * Handles sending analytics data to Kafka topics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaAnalyticsProducerImpl implements KafkaAnalyticsProducer {

    private final KafkaTemplate<String, AnalyticsDTO> kafkaTemplate;

    @Value("${app.kafka.topic.analytics:analytics-topic}")
    private String analyticsTopic;

    @Override
    public void sendAnalyticsEvent(AnalyticsDTO analyticsDTO) {
        sendAnalyticsEvent(analyticsTopic, analyticsDTO);
    }

    @Override
    public void sendAnalyticsEvent(String topic, AnalyticsDTO analyticsDTO) {
        try {
            log.debug("Sending analytics event to topic '{}': {}", topic, analyticsDTO.getShortCode());
            
            Message<AnalyticsDTO> message = MessageBuilder
                    .withPayload(analyticsDTO)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, analyticsDTO.getShortCode())
                    .build();

            kafkaTemplate.send(message)
                    .whenComplete((sendResult, exception) -> {
                        if (exception != null) {
                            log.error("Failed to send analytics event to topic '{}' for short code: {}",
                                    topic, analyticsDTO.getShortCode(), exception);
                        } else {
                            log.debug("Successfully sent analytics event to topic '{}' with offset: {}",
                                    topic, sendResult.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending analytics event to Kafka topic '{}': {}", topic, analyticsDTO.getShortCode(), e);
            throw new RuntimeException("Failed to send analytics event to Kafka", e);
        }
    }
}
