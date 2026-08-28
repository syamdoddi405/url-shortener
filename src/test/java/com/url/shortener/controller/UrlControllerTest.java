package com.url.shortener.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.url.shortener.entity.UrlEntity;
import com.url.shortener.kafka.AnalyticsEventProducer;
import com.url.shortener.service.UrlService;
import com.url.shortener.service.context.RequestContext;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UrlService urlService;


    @Mock
    private RequestContext requestContext;
    
    @Mock
    private AnalyticsEventProducer analyticsEventProducer;


    @BeforeEach
    void setUp() {

        UrlController controller =
                new UrlController(
                        urlService,
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
    void getUrls_shouldReturn200WithUrls() throws Exception {

        // Arrange
        UrlEntity url1 = UrlEntity.builder()
                .id(1L)
                .originalUrl("https://google.com")
                .shortCode("abc12345")
                .build();

        UrlEntity url2 = UrlEntity.builder()
                .id(2L)
                .originalUrl("https://amazon.com")
                .shortCode("xyz98765")
                .build();

        List<UrlEntity> urls =
                Arrays.asList(url1, url2);

        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(urlService.getUrls())
                .thenReturn(urls);

        // Act & Assert
        mockMvc.perform(
                get("/api/urls/")
                        .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(
                content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                )
        )
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(
                jsonPath("$[0].id").value(1)
        )
        .andExpect(
                jsonPath("$[0].originalUrl")
                        .value("https://google.com")
        )
        .andExpect(
                jsonPath("$[0].shortCode")
                        .value("abc12345")
        )
        .andExpect(
                jsonPath("$[1].id").value(2)
        )
        .andExpect(
                jsonPath("$[1].originalUrl")
                        .value("https://amazon.com")
        )
        .andExpect(
                jsonPath("$[1].shortCode")
                        .value("xyz98765")
        );

        // Verify
        verify(requestContext).getClientIp();
        verify(urlService).getUrls();
    }

    @Test
    void getUrls_shouldReturn200WithEmptyList_whenNoUrlsExist()
            throws Exception {

        // Arrange
        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(urlService.getUrls())
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(
                get("/api/urls/")
                        .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(
                content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                )
        )
        .andExpect(jsonPath("$.length()").value(0));

        // Verify
        verify(requestContext).getClientIp();
        verify(urlService).getUrls();
    }

    @Test
    void getUrls_shouldReturn500_whenServiceThrowsException()
            throws Exception {

        // Arrange
        when(requestContext.getClientIp())
                .thenReturn("127.0.0.1");

        when(urlService.getUrls())
                .thenThrow(
                        new RuntimeException(
                                "Database connection failed"
                        )
                );

        // Act & Assert
        mockMvc.perform(
                get("/api/urls/")
                        .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(
                status().isInternalServerError()
        );

        // Verify
        verify(requestContext).getClientIp();
        verify(urlService).getUrls();
    }


  
}