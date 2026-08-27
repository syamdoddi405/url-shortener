package com.url.shortener.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.url.shortener.entity.AnalyticsEntity;
import com.url.shortener.entity.UrlEntity;
import com.url.shortener.exceptions.UrlNotFoundException;
import com.url.shortener.service.AnalyticsService;
import com.url.shortener.service.UrlService;
import com.url.shortener.service.context.RequestContext;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UrlService urlService;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private RequestContext requestContext;

    @BeforeEach
    void setUp() {

        UrlController controller =
                new UrlController(
                        urlService,
                        analyticsService,
                        requestContext
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void shortenUrl_shouldReturn201_whenRequestIsValid()
            throws Exception {

        String originalUrl = "https://www.google.com";

        UrlEntity response =
                UrlEntity.builder()
                        .id(1L)
                        .originalUrl(originalUrl)
                        .shortCode("a1b2c3d4")
                        .createdAt(LocalDateTime.now())
                        .build();

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(urlService.shortenUrl(originalUrl))
                .thenReturn(response);

        mockMvc.perform(
                post("/api/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "https://www.google.com"
                                }
                                """)
        )
        .andExpect(status().isCreated())
        .andExpect(content().contentTypeCompatibleWith(
                MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.originalUrl")
                .value(originalUrl))
        .andExpect(jsonPath("$.shortCode")
                .value("a1b2c3d4"));

        verify(urlService).shortenUrl(originalUrl);
    }

    @Test
    void shortenUrl_shouldReturn400_whenUrlIsInvalid()
            throws Exception {

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(urlService.shortenUrl(anyString()))
                .thenThrow(
                        new IllegalArgumentException(
                                "URL cannot be blank"
                        )
                );

        mockMvc.perform(
                post("/api/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": ""
                                }
                                """)
        )
        .andExpect(status().isBadRequest());

        verify(urlService).shortenUrl("");
    }

    @Test
    void shortenUrl_shouldReturn500_whenServiceFails()
            throws Exception {

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(urlService.shortenUrl(anyString()))
                .thenThrow(
                        new RuntimeException("Database failure")
                );

        mockMvc.perform(
                post("/api/urls/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "originalUrl": "https://www.google.com"
                                }
                                """)
        )
        .andExpect(status().isInternalServerError());
    }

    @Test
    void expandUrl_shouldReturn200_whenUrlExists()
            throws Exception {

        String shortCode = "a1b2c3d4";
        String originalUrl = "https://www.google.com";

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(requestContext.getReferer())
                .thenReturn("https://twitter.com");

        when(requestContext.getUserAgent())
                .thenReturn("Mozilla/5.0");

        when(urlService.expandUrl(shortCode))
                .thenReturn(originalUrl);

        mockMvc.perform(
                get("/api/urls/{shortCode}", shortCode)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.originalUrl")
                .value(originalUrl));

        verify(urlService).expandUrl(shortCode);

        verify(analyticsService)
                .saveAnalytics(
                        shortCode,
                        "https://twitter.com",
                        "Mozilla/5.0"
                );
    }

    @Test
    void expandUrl_shouldReturn404_whenShortCodeNotFound()
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
                get("/api/urls/{shortCode}", shortCode)
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
    void expandUrl_shouldReturn500_whenServiceFails()
            throws Exception {

        String shortCode = "a1b2c3d4";

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(urlService.expandUrl(shortCode))
                .thenThrow(
                        new RuntimeException("Database error")
                );

        mockMvc.perform(
                get("/api/urls/{shortCode}", shortCode)
        )
        .andExpect(status().isInternalServerError());
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
                        .totalClicks(25)
                        .build();

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(analyticsService.getStats(shortCode))
                .thenReturn(stats);

        mockMvc.perform(
                get("/api/urls/{shortCode}/stats", shortCode)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shortCode")
                .value(shortCode))
        .andExpect(jsonPath("$.totalClicks")
                .value(25));
    }

    @Test
    void getStats_shouldReturn404_whenStatsNotFound()
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
                get("/api/urls/{shortCode}/stats", shortCode)
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
                        new RuntimeException("Database failure")
                );

        mockMvc.perform(
                get("/api/urls/{shortCode}/stats", shortCode)
        )
        .andExpect(status().isInternalServerError());
    }
}