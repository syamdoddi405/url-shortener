package com.url.shortener.exceptions;

/**
 * Exception thrown when a URL with a given short code is not found.
 * Extends RuntimeException for unchecked exception handling.
 * Follows the Single Responsibility Principle by focusing on URL not found scenarios.
 */
public class UrlNotFoundException extends RuntimeException {

    /**
     * Constructs a new UrlNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public UrlNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new UrlNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public UrlNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
