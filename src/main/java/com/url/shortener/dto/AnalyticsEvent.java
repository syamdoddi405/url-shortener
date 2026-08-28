package com.url.shortener.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsEvent {

    private String eventId;

    private String shortCode;

    private String referrer;

    private String userAgent;

    private String clientIp;

    private LocalDateTime eventTime;
}