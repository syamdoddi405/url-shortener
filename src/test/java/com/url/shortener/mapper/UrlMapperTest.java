package com.url.shortener.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.url.shortener.dto.UrlDTO;
import com.url.shortener.entity.UrlEntity;

class UrlMapperTest {

    private UrlMapper urlMapper;

    private LocalDateTime createdAt;

    @BeforeEach
    void setUp() {
        urlMapper = new UrlMapper();
        createdAt = LocalDateTime.of(2026, 8, 27, 10, 30);
    }


    @Test
    void toEntity_shouldMapAllFields() {

        UrlDTO dto = UrlDTO.builder()
                .id(10L)
                .originalUrl("https://example.com")
                .shortCode("xyz98765")
                .createdAt(createdAt)
                .build();

        UrlEntity result = urlMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(
                "https://example.com",
                result.getOriginalUrl()
        );
        assertEquals("xyz98765", result.getShortCode());
        assertEquals(createdAt, result.getCreatedAt());
    }

    @Test
    void toEntity_shouldReturnNull_whenInputIsNull() {

        UrlEntity result = urlMapper.toEntity(null);

        assertNull(result);
    }
}