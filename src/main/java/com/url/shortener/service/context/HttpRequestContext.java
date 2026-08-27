package com.url.shortener.service.context;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * HTTP Request context implementation.
 * Wraps HttpServletRequest to provide a clean abstraction.
 * Uses @RequestScope to ensure one instance per HTTP request.
 */
@Component
@RequestScope
@RequiredArgsConstructor
public class HttpRequestContext implements RequestContext {

    private final HttpServletRequest httpServletRequest;

    @Override
    public String getReferer() {
        return httpServletRequest.getHeader("Referer");
    }

    @Override
    public String getUserAgent() {
        return httpServletRequest.getHeader("User-Agent");
    }

    @Override
    public String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}
