package com.url.shortener.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.url.shortener.dto.AnalyticsDTO;
import com.url.shortener.dto.UrlDTO;
import com.url.shortener.entity.AnalyticsEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.mapper.AnalyticsMapper;
import com.url.shortener.repository.AnalyticsRepository;
import com.url.shortener.repository.UrlRepository;
import com.url.shortener.service.cache.CacheService;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private AnalyticsMapper analyticsMapper;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private final String shortCode = "a1b2c3d4";
    private final String originalUrl = "https://www.google.com";

    private UrlDTO urlDTO;
    private AnalyticsDTO analyticsDTO;
    private AnalyticsEntity analyticsEntity;

    @BeforeEach
    void setUp() {

        urlDTO = UrlDTO.builder()
                .id(1L)
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .build();

        analyticsDTO = AnalyticsDTO.builder()
                .id(100L)
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .totalClicks(10)
                .build();

        analyticsEntity = AnalyticsEntity.builder()
                .id(100L)
                .shortCode(shortCode)
                .originalUrl(originalUrl)
                .totalClicks(10)
                .build();
    }

    @Test
    void getStats_shouldReturnExistingAnalytics() {

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(urlDTO));

        when(analyticsRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(analyticsDTO));

        when(analyticsMapper.toDTO(analyticsDTO))
                .thenReturn(analyticsEntity);

        AnalyticsEntity result =
                analyticsService.getStats(shortCode);

        assertNotNull(result);
        assertEquals(shortCode, result.getShortCode());
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(10, result.getTotalClicks());

        verify(urlRepository).findByShortCode(shortCode);
        verify(analyticsRepository).findByShortCode(shortCode);
        verify(analyticsMapper).toDTO(analyticsDTO);
    }

    @Test
    void getStats_shouldCreateEmptyAnalytics_whenAnalyticsDoesNotExist() {

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(urlDTO));

        when(analyticsRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        AnalyticsEntity emptyAnalytics =
                AnalyticsEntity.builder()
                        .shortCode(shortCode)
                        .originalUrl(originalUrl)
                        .totalClicks(0)
                        .build();

        when(analyticsMapper.toDTO(any(AnalyticsDTO.class)))
                .thenReturn(emptyAnalytics);

        AnalyticsEntity result =
                analyticsService.getStats(shortCode);

        assertNotNull(result);
        assertEquals(shortCode, result.getShortCode());
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(0, result.getTotalClicks());

        verify(urlRepository).findByShortCode(shortCode);
        verify(analyticsRepository).findByShortCode(shortCode);
        verify(analyticsMapper).toDTO(any(AnalyticsDTO.class));
    }

    @Test
    void getStats_shouldThrowException_whenUrlDoesNotExist() {

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        UrlNotFoundException exception =
                assertThrows(
                        UrlNotFoundException.class,
                        () -> analyticsService.getStats(shortCode)
                );

        assertEquals(
                "URL not found for short code: " + shortCode,
                exception.getMessage()
        );

        verify(urlRepository).findByShortCode(shortCode);

        verify(analyticsRepository, never())
                .findByShortCode(any());

        verify(analyticsMapper, never())
                .toDTO(any());
    }

    @Test
    void saveAnalytics_shouldCreateNewAnalyticsRecord() {

        when(analyticsRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        when(cacheService.get(shortCode))
                .thenReturn(Optional.empty());

        analyticsService.saveAnalytics(
                shortCode,
                "google.com",
                "Mozilla/5.0"
        );

        ArgumentCaptor<AnalyticsDTO> captor =
                ArgumentCaptor.forClass(AnalyticsDTO.class);

        verify(analyticsRepository).save(captor.capture());

        AnalyticsDTO savedAnalytics = captor.getValue();

        assertEquals(shortCode, savedAnalytics.getShortCode());
        assertEquals(1, savedAnalytics.getTotalClicks());
        assertEquals("google.com", savedAnalytics.getLastReferrer());
        assertEquals("Mozilla/5.0", savedAnalytics.getLastUserAgent());

        assertNotNull(savedAnalytics.getLastAccessed());
    }

    @Test
    void saveAnalytics_shouldIncrementExistingClicks() {

        AnalyticsDTO existingAnalytics =
                AnalyticsDTO.builder()
                        .id(100L)
                        .shortCode(shortCode)
                        .originalUrl(originalUrl)
                        .totalClicks(10)
                        .build();

        when(analyticsRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(existingAnalytics));

        when(cacheService.get(shortCode))
                .thenReturn(Optional.empty());

        analyticsService.saveAnalytics(
                shortCode,
                "twitter.com",
                "Chrome"
        );

        ArgumentCaptor<AnalyticsDTO> captor =
                ArgumentCaptor.forClass(AnalyticsDTO.class);

        verify(analyticsRepository).save(captor.capture());

        AnalyticsDTO savedAnalytics = captor.getValue();

        assertEquals(100L, savedAnalytics.getId());
        assertEquals(11, savedAnalytics.getTotalClicks());
        assertEquals("twitter.com", savedAnalytics.getLastReferrer());
        assertEquals("Chrome", savedAnalytics.getLastUserAgent());
        assertNotNull(savedAnalytics.getLastAccessed());
    }

    @Test
    void saveAnalytics_shouldUseCachedOriginalUrl() {

        when(analyticsRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        when(cacheService.get(shortCode))
                .thenReturn(Optional.of(originalUrl));

        analyticsService.saveAnalytics(
                shortCode,
                "google.com",
                "Mozilla/5.0"
        );

        ArgumentCaptor<AnalyticsDTO> captor =
                ArgumentCaptor.forClass(AnalyticsDTO.class);

        verify(analyticsRepository).save(captor.capture());

        AnalyticsDTO savedAnalytics = captor.getValue();

        assertEquals(originalUrl, savedAnalytics.getOriginalUrl());
        assertEquals(1, savedAnalytics.getTotalClicks());
    }

    @Test
    void saveAnalytics_shouldWrapRepositoryException() {

        when(analyticsRepository.findByShortCode(shortCode))
                .thenThrow(new RuntimeException("Database unavailable"));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> analyticsService.saveAnalytics(
                                shortCode,
                                "google.com",
                                "Chrome"
                        )
                );

        assertEquals(
                "Failed to save analytics data",
                exception.getMessage()
        );

        assertNotNull(exception.getCause());
        assertEquals(
                "Database unavailable",
                exception.getCause().getMessage()
        );
    }
}