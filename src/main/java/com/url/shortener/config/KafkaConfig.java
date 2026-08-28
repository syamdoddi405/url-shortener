package com.url.shortener.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic urlClickEventsTopic(
            @Value("${kafka.topic.url-click-events}")
            String topicName) {

        return TopicBuilder
                .name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}