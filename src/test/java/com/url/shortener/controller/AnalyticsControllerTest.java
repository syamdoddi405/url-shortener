package com.url.shortener.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.url.shortener.entity.AnalyticsEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.service.AnalyticsService;
import com.url.shortener.service.UrlService;
import com.url.shortener.service.context.RequestContext;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private UrlService urlService;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {

        AnalyticsController controller =
                new AnalyticsController(
                        analyticsService,
                        urlService,
                        requestContext
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void getStats_shouldReturn200_whenStatsExist()
            throws Exception {

        String shortCode = "a1b2c3d4";

        AnalyticsEntity stats =
                AnalyticsEntity.builder()
                        .id(1L)
                        .shortCode(shortCode)
                        .originalUrl("https://www.google.com")
                        .totalClicks(100)
                        .lastReferrer("google.com")
                        .lastUserAgent("Chrome")
                        .build();

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(analyticsService.getStats(shortCode))
                .thenReturn(stats);

        mockMvc.perform(
                get("/api/analytics/{shortCode}", shortCode)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortCode")
                .value(shortCode))
        .andExpect(jsonPath("$.originalUrl")
                .value("https://www.google.com"))
        .andExpect(jsonPath("$.totalClicks")
                .value(100))
        .andExpect(jsonPath("$.lastReferrer")
                .value("google.com"));

        verify(analyticsService).getStats(shortCode);
    }

    @Test
    void getStats_shouldReturn404_whenShortCodeNotFound()
            throws Exception {

        String shortCode = "invalid";

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(analyticsService.getStats(shortCode))
                .thenThrow(
                        new UrlNotFoundException(
                                "URL not found"
                        )
                );

        mockMvc.perform(
                get("/api/analytics/{shortCode}", shortCode)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void getStats_shouldReturn500_whenServiceFails()
            throws Exception {

        String shortCode = "a1b2c3d4";

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(analyticsService.getStats(shortCode))
                .thenThrow(
                        new RuntimeException("Database error")
                );

        mockMvc.perform(
                get("/api/analytics/{shortCode}", shortCode)
        )
        .andExpect(status().isInternalServerError());
    }

    @Test
    void expand_shouldReturn200_andCaptureAnalytics()
            throws Exception {

        String shortCode = "a1b2c3d4";
        String originalUrl = "https://www.google.com";

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(requestContext.getReferer())
                .thenReturn("https://google.com");

        when(requestContext.getUserAgent())
                .thenReturn("Mozilla/5.0");

        when(urlService.expandUrl(shortCode))
                .thenReturn(originalUrl);

        mockMvc.perform(
                get("/api/analytics/expand/{shortCode}", shortCode)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.originalUrl")
                .value(originalUrl));

        verify(urlService).expandUrl(shortCode);

        verify(analyticsService)
                .saveAnalytics(
                        shortCode,
                        "https://google.com",
                        "Mozilla/5.0"
                );
    }

    @Test
    void expand_shouldReturn404_whenShortCodeNotFound()
            throws Exception {

        String shortCode = "invalid";

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(urlService.expandUrl(shortCode))
                .thenThrow(
                        new UrlNotFoundException(
                                "URL not found"
                        )
                );

        mockMvc.perform(
                get("/api/analytics/expand/{shortCode}", shortCode)
        )
        .andExpect(status().isNotFound());

        verify(analyticsService, never())
                .saveAnalytics(
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void expand_shouldReturn500_whenServiceFails()
            throws Exception {

        String shortCode = "a1b2c3d4";

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(urlService.expandUrl(shortCode))
                .thenThrow(
                        new RuntimeException("Database error")
                );

        mockMvc.perform(
                get("/api/analytics/expand/{shortCode}", shortCode)
        )
        .andExpect(status().isInternalServerError());
    }
}