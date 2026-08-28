package com.url.shortener.constants;

public final class KafkaTopics {

    private KafkaTopics() {
    }

    public static final String URL_CLICK_EVENTS =
            "${kafka.topic.url-click-events}";
}