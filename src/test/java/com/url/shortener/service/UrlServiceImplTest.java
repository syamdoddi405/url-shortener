package com.url.shortener.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.url.shortener.dto.UrlDTO;
import com.url.shortener.entity.UrlEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.mapper.UrlMapper;
import com.url.shortener.repository.UrlRepository;
import com.url.shortener.service.cache.CacheService;
import com.url.shortener.util.ShortCodeGenerator;

@ExtendWith(MockitoExtension.class)
class UrlServiceImplTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private UrlMapper urlMapper;

    @Mock
    private ShortCodeGenerator shortCodeGenerator;

    @InjectMocks
    private UrlServiceImpl urlService;

    private final String originalUrl = "https://www.google.com";
    private final String shortCode = "a1b2c3d4";

    private UrlDTO urlDTO;
    private UrlEntity urlEntity;

    @BeforeEach
    void setUp() {
        urlDTO = UrlDTO.builder()
                .id(1L)
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .createdAt(LocalDateTime.now())
                .build();

        urlEntity = UrlEntity.builder()
                .id(1L)
                .originalUrl(originalUrl)
                .shortCode(shortCode)
                .createdAt(urlDTO.getCreatedAt())
                .build();
    }

    @Test
    void shortenUrl_shouldCreateAndReturnShortenedUrl() {

        when(shortCodeGenerator.generate(originalUrl))
                .thenReturn(shortCode);

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        when(urlRepository.save(any(UrlDTO.class)))
                .thenReturn(urlDTO);

        when(urlMapper.toDTO(urlDTO))
                .thenReturn(urlEntity);

        UrlEntity result = urlService.shortenUrl(originalUrl);

        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(shortCode, result.getShortCode());

        verify(shortCodeGenerator).generate(originalUrl);
        verify(urlRepository).findByShortCode(shortCode);
        verify(urlRepository).save(any(UrlDTO.class));
        verify(cacheService).put(shortCode, originalUrl);
        verify(urlMapper).toDTO(urlDTO);
    }

    @Test
    void shortenUrl_shouldUpdateExistingUrl() {

        UrlDTO existingUrl = UrlDTO.builder()
                .id(10L)
                .originalUrl("https://old-url.com")
                .shortCode(shortCode)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(shortCodeGenerator.generate(originalUrl))
                .thenReturn(shortCode);

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(existingUrl));

        when(urlRepository.save(any(UrlDTO.class)))
                .thenReturn(urlDTO);

        when(urlMapper.toDTO(urlDTO))
                .thenReturn(urlEntity);

        UrlEntity result = urlService.shortenUrl(originalUrl);

        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(shortCode, result.getShortCode());

        ArgumentCaptor<UrlDTO> captor =
                ArgumentCaptor.forClass(UrlDTO.class);

        verify(urlRepository).save(captor.capture());

        UrlDTO savedUrl = captor.getValue();

        assertEquals(10L, savedUrl.getId());
        assertEquals(originalUrl, savedUrl.getOriginalUrl());
        assertEquals(shortCode, savedUrl.getShortCode());

        verify(cacheService).put(shortCode, originalUrl);
    }

    @Test
    void shortenUrl_shouldThrowException_whenUrlIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> urlService.shortenUrl(null)
                );

        assertEquals("URL cannot be blank", exception.getMessage());

        verify(shortCodeGenerator, never()).generate(any());
        verify(urlRepository, never()).save(any());
        verify(cacheService, never()).put(any(), any());
    }

    @Test
    void shortenUrl_shouldThrowException_whenUrlIsBlank() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> urlService.shortenUrl("   ")
                );

        assertEquals("URL cannot be blank", exception.getMessage());

        verify(shortCodeGenerator, never()).generate(any());
        verify(urlRepository, never()).save(any());
    }

    @Test
    void expandUrl_shouldReturnUrlFromCache_whenCacheHit() {

        when(cacheService.get(shortCode))
                .thenReturn(Optional.of(originalUrl));

        String result = urlService.expandUrl(shortCode);

        assertEquals(originalUrl, result);

        verify(cacheService).get(shortCode);

        // Database should not be queried on cache hit
        verify(urlRepository, never()).findByShortCode(any());

        // Cache should not be updated again
        verify(cacheService, never()).put(any(), any());
    }

    @Test
    void expandUrl_shouldReturnUrlFromDatabase_whenCacheMiss() {

        when(cacheService.get(shortCode))
                .thenReturn(Optional.empty());

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.of(urlDTO));

        String result = urlService.expandUrl(shortCode);

        assertEquals(originalUrl, result);

        verify(cacheService).get(shortCode);
        verify(urlRepository).findByShortCode(shortCode);

        // Database result should be cached
        verify(cacheService).put(shortCode, originalUrl);
    }

    @Test
    void expandUrl_shouldThrowUrlNotFoundException_whenUrlDoesNotExist() {

        when(cacheService.get(shortCode))
                .thenReturn(Optional.empty());

        when(urlRepository.findByShortCode(shortCode))
                .thenReturn(Optional.empty());

        UrlNotFoundException exception =
                assertThrows(
                        UrlNotFoundException.class,
                        () -> urlService.expandUrl(shortCode)
                );

        assertEquals(
                "URL not found for short code: " + shortCode,
                exception.getMessage()
        );

        verify(cacheService).get(shortCode);
        verify(urlRepository).findByShortCode(shortCode);

        verify(cacheService, never()).put(any(), any());
    }
}