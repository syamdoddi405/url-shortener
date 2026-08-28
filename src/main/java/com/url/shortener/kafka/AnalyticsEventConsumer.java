package com.url.shortener.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.url.shortener.dto.AnalyticsEvent;
import com.url.shortener.service.AnalyticsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEventConsumer {

    private final AnalyticsService analyticsService;

    @KafkaListener(
            topics = "${kafka.topic.url-click-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(AnalyticsEvent event) {

        log.debug(
                "Received analytics event. eventId={}, shortCode={}",
                event.getEventId(),
                event.getShortCode()
        );

        analyticsService.saveAnalytics(
                event.getShortCode(),
                event.getReferrer(),
                event.getUserAgent()
        );

        log.debug(
                "Analytics event processed successfully. " +
                "eventId={}",
                event.getEventId()
        );
    }
}