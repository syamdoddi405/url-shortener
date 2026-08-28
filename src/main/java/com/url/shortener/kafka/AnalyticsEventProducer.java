package com.url.shortener.kafka;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.url.shortener.dto.AnalyticsEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventProducer {

    private final KafkaTemplate<String, AnalyticsEvent> kafkaTemplate;

    @Value("${kafka.topic.url-click-events}")
    private String topic;

    public void publish(
            String shortCode,
            String referrer,
            String userAgent,
            String clientIp) {

        AnalyticsEvent event = AnalyticsEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .shortCode(shortCode)
                .referrer(referrer)
                .userAgent(userAgent)
                .clientIp(clientIp)
                .eventTime(java.time.LocalDateTime.now())
                .build();

        kafkaTemplate.send(
                topic,
                shortCode,
                event
        ).whenComplete((result, exception) -> {

            if (exception != null) {
                log.error(
                        "Failed to publish analytics event. " +
                        "eventId={}, shortCode={}",
                        event.getEventId(),
                        shortCode,
                        exception
                );
                return;
            }

            log.debug(
                    "Analytics event published. eventId={}, " +
                    "topic={}, partition={}, offset={}",
                    event.getEventId(),
                    topic,
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}