package com.url.shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for URL shortening endpoint.
 * Includes input validation annotations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortenUrlRequest {
    
    @NotBlank(message = "URL cannot be blank")
    @Pattern(regexp = "^(https?://)[^\\s]+$", message = "Must be a valid HTTP/HTTPS URL")
    private String originalUrl;
}
