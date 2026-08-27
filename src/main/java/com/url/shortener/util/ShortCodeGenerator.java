package com.url.shortener.util;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating short codes from URLs.
 * Follows the Strategy pattern to allow easy switching between different generation algorithms.
 * This implementation uses hex encoding of the URL hash code.
 */
@Component
@Slf4j
public class ShortCodeGenerator {

    private static final int SHORT_CODE_LENGTH = 8;

    /**
     * Generates a short code from the given URL.
     *
     * @param originalUrl the original URL to generate a short code for
     * @return a short code string of fixed length
     * @throws IllegalArgumentException if URL is null or empty
     */
    public String generate(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            log.warn("Attempted to generate short code for empty URL");
            throw new IllegalArgumentException("URL cannot be null or empty");
        }

        String shortCode = generateHexCode(originalUrl);
        log.debug("Generated short code: {} for URL: {}", shortCode, originalUrl);
        return shortCode;
    }

    /**
     * Generates a hex-based short code using URL hash code.
     * Note: This is a simple implementation. For production, consider using:
     * - Base62 encoding for better density
     * - Database sequence for guaranteed uniqueness
     * - Collision detection and retry logic
     *
     * @param originalUrl the original URL
     * @return hex-encoded short code
     */
    private String generateHexCode(String originalUrl) {
        int hashCode = originalUrl.hashCode();
        String hexString = Integer.toHexString(Math.abs(hashCode));
        
        // Pad with zeros or truncate to ensure consistent length
        if (hexString.length() < SHORT_CODE_LENGTH) {
            hexString = String.format("%0" + SHORT_CODE_LENGTH + "d", Integer.parseInt(hexString, 16));
        } else if (hexString.length() > SHORT_CODE_LENGTH) {
            hexString = hexString.substring(0, SHORT_CODE_LENGTH);
        }
        
        return hexString;
    }

    /**
     * Alternative method for generating short codes using Base62 encoding.
     * Can be used by switching the implementation.
     *
     * @param originalUrl the original URL
     * @return base62-encoded short code
     */
    public String generateBase62(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        
        int hashCode = Math.abs(originalUrl.hashCode());
        return encodeBase62(hashCode);
    }

    /**
     * Encodes an integer to Base62 string.
     * Base62 uses digits 0-9, lowercase a-z, and uppercase A-Z.
     *
     * @param number the number to encode
     * @return base62-encoded string
     */
    private String encodeBase62(long number) {
        if (number == 0) {
            return "0";
        }

        String base62Chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();

        while (number > 0) {
            sb.append(base62Chars.charAt((int) (number % 62)));
            number /= 62;
        }

        return sb.reverse().toString();
    }
}
