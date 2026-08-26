package com.url.shortener.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.shortener.entity.UrlEntity;
import com.url.shortener.service.UrlService;

@RestController
@RequestMapping("/url")
public class UrlController {

    private final UrlService urlService;

    public UrlController(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public ResponseEntity<UrlEntity> shorten(@RequestBody String originalUrl) {
        return ResponseEntity.ok(urlService.shortenUrl(originalUrl));
    }

    @GetMapping("/expand/{shortCode}")
    public ResponseEntity<String> expand(@PathVariable String shortCode) {
        return ResponseEntity.ok(urlService.expandUrl(shortCode));
    }
}

