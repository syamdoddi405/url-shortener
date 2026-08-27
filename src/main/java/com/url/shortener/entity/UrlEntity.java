package com.url.shortener.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UrlEntity {
    
    private Long id;

    private String originalUrl;
    
    private String shortCode;
    
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "UrlEntity [id=" + id + ", originalUrl=" + originalUrl + ", shortCode=" + shortCode
                + ", createdAt=" + createdAt + "]";
    }
}
