package com.url.shortener.exceptions;

/**
 * Exception thrown when URL validation fails.
 * Extends RuntimeException for unchecked exception handling.
 * Follows the Single Responsibility Principle by focusing on URL validation errors.
 */
public class InvalidUrlException extends RuntimeException {

    /**
     * Constructs a new InvalidUrlException with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidUrlException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidUrlException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public InvalidUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
