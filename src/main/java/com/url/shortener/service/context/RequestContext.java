package com.url.shortener.service.context;

/**
 * Request context abstraction interface.
 * Follows the Dependency Inversion Principle.
 * Decouples business logic from Spring's HttpServletRequest.
 * Allows for easier testing and flexibility in obtaining request data.
 */
public interface RequestContext {

    /**
     * Gets the referer header from the request.
     *
     * @return the referer value, or null if not present
     */
    String getReferer();

    /**
     * Gets the User-Agent header from the request.
     *
     * @return the User-Agent value, or null if not present
     */
    String getUserAgent();

    /**
     * Gets the client IP address.
     *
     * @return the client IP address
     */
    String getClientIp();
}
