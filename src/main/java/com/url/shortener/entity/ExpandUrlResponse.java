package com.url.shortener.entity;


public class ExpandUrlResponse {
    private String originalUrl;

    public ExpandUrlResponse(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
}