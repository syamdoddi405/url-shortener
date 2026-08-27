package com.url.shortener.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.url.shortener.service.context.HttpRequestContext;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class HttpRequestContextTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    private HttpRequestContext requestContext;

    @BeforeEach
    void setUp() {
        requestContext =
                new HttpRequestContext(
                        httpServletRequest
                );
    }

    @Test
    void getReferer_shouldReturnRefererHeader() {

        when(httpServletRequest.getHeader("Referer"))
                .thenReturn("https://google.com");

        String result =
                requestContext.getReferer();

        assertEquals(
                "https://google.com",
                result
        );
    }

    @Test
    void getReferer_shouldReturnNull_whenHeaderMissing() {

        when(httpServletRequest.getHeader("Referer"))
                .thenReturn(null);

        String result =
                requestContext.getReferer();

        assertEquals(null, result);
    }

    @Test
    void getUserAgent_shouldReturnUserAgentHeader() {

        when(httpServletRequest.getHeader("User-Agent"))
                .thenReturn("Mozilla/5.0");

        String result =
                requestContext.getUserAgent();

        assertEquals(
                "Mozilla/5.0",
                result
        );
    }

    @Test
    void getUserAgent_shouldReturnNull_whenHeaderMissing() {

        when(httpServletRequest.getHeader("User-Agent"))
                .thenReturn(null);

        String result =
                requestContext.getUserAgent();

        assertEquals(null, result);
    }

    @Test
    void getClientIp_shouldReturnForwardedIp() {

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn("192.168.1.10");

        String result =
                requestContext.getClientIp();

        assertEquals(
                "192.168.1.10",
                result
        );
    }

    @Test
    void getClientIp_shouldReturnFirstIp_whenMultipleForwardedIpsExist() {

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn(
                        "192.168.1.10, 10.0.0.5, 172.16.0.1"
                );

        String result =
                requestContext.getClientIp();

        assertEquals(
                "192.168.1.10",
                result
        );
    }

    @Test
    void getClientIp_shouldTrimForwardedIp() {

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn(
                        "   192.168.1.10  , 10.0.0.5"
                );

        String result =
                requestContext.getClientIp();

        assertEquals(
                "192.168.1.10",
                result
        );
    }

    @Test
    void getClientIp_shouldUseRemoteAddress_whenForwardedHeaderMissing() {

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn(null);

        when(httpServletRequest.getRemoteAddr())
                .thenReturn("127.0.0.1");

        String result =
                requestContext.getClientIp();

        assertEquals(
                "127.0.0.1",
                result
        );
    }

    @Test
    void getClientIp_shouldUseRemoteAddress_whenForwardedHeaderEmpty() {

        when(httpServletRequest.getHeader("X-Forwarded-For"))
                .thenReturn("");

        when(httpServletRequest.getRemoteAddr())
                .thenReturn("127.0.0.1");

        String result =
                requestContext.getClientIp();

        assertEquals(
                "127.0.0.1",
                result
        );
    }
}