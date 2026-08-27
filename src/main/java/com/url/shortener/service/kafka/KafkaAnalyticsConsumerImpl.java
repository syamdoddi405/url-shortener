package com.url.shortener.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.rebalance.ConsumerAwareRebalanceListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.url.shortener.dto.AnalyticsDTO;
import com.url.shortener.repository.AnalyticsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Kafka Consumer Implementation for Analytics Events
 * Consumes analytics data from Kafka topic and persists to database
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaAnalyticsConsumerImpl {

    private final AnalyticsRepository analyticsRepository;

    /**
     * Consume analytics events from Kafka topic
     * Processes the analytics data and saves it to the database
     *
     * @param analyticsDTO the analytics payload
     * @param partition the partition from which the record was received
     * @param offset the offset of the record
     * @param acknowledgment manual acknowledgment handle
     */
    @KafkaListener(
            topics = "${app.kafka.topic.analytics:analytics-topic}",
            groupId = "${spring.kafka.consumer.group-id:analytics-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAnalyticsEvent(
            @Payload AnalyticsDTO analyticsDTO,
            @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        try {
            log.debug("Received analytics event from partition {} with offset {}: shortCode = {}",
                    partition, offset, analyticsDTO.getShortCode());

            // Validate the analytics DTO
            if (analyticsDTO == null || analyticsDTO.getShortCode() == null) {
                log.warn("Received invalid analytics event with null shortCode");
                acknowledgment.acknowledge();
                return;
            }

            // Save analytics to database
            analyticsRepository.save(analyticsDTO);
            log.info("Successfully persisted analytics event for short code: {} (partition: {}, offset: {})",
                    analyticsDTO.getShortCode(), partition, offset);

            // Acknowledge the message
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing analytics event from partition {} with offset {}: {}",
                    partition, offset, analyticsDTO.getShortCode(), e);
            // In case of error, we might want to move to a dead letter queue
            // For now, we acknowledge to prevent infinite retry loops
            acknowledgment.acknowledge();
        }
    }
}
