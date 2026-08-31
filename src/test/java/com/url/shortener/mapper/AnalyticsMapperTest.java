package com.url.shortener.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.url.shortener.dto.AnalyticsDTO;
import com.url.shortener.entity.AnalyticsEntity;

class AnalyticsMapperTest {

    private AnalyticsMapper analyticsMapper;

    private LocalDateTime createdAt;
    private LocalDateTime lastAccessed;

    @BeforeEach
    void setUp() {
        analyticsMapper = new AnalyticsMapper();

        createdAt =
                LocalDateTime.of(2026, 8, 20, 10, 0);

        lastAccessed =
                LocalDateTime.of(2026, 8, 27, 12, 30);
    }


    @Test
    void toEntity_shouldMapAllFields() {

        AnalyticsDTO dto = AnalyticsDTO.builder()
                .id(200L)
                .shortCode("xyz98765")
                .originalUrl("https://example.com")
                .totalClicks(50)
                .createdAt(createdAt)
                .lastAccessed(lastAccessed)
                .lastReferrer("https://google.com")
                .lastUserAgent("Chrome")
                .build();

        AnalyticsEntity result =
                analyticsMapper.toEntity(dto);

        assertNotNull(result);

        assertEquals(200L, result.getId());
        assertEquals(
                "xyz98765",
                result.getShortCode()
        );
        assertEquals(
                "https://example.com",
                result.getOriginalUrl()
        );
        assertEquals(
                50,
                result.getTotalClicks()
        );
        assertEquals(
                createdAt,
                result.getCreatedAt()
        );
        assertEquals(
                lastAccessed,
                result.getLastAccessed()
        );
        assertEquals(
                "https://google.com",
                result.getLastReferrer()
        );
        assertEquals(
                "Chrome",
                result.getLastUserAgent()
        );
    }

    @Test
    void toEntity_shouldReturnNull_whenInputIsNull() {

        AnalyticsEntity result =
                analyticsMapper.toEntity(null);

        assertNull(result);
    }
}