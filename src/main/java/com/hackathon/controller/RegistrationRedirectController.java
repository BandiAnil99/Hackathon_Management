package com.hackathon.controller;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Keeps registration QR codes generated before the frontend URL fix usable.
 */
@RestController
public class RegistrationRedirectController {

    private final String frontendUrl;

    public RegistrationRedirectController(@Value("${app.frontend-url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/participants/register")
    public ResponseEntity<Void> redirectToRegistration(@RequestParam Long eventId) {
        String base = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
        URI destination = URI.create(base + "/participants/register?eventId=" + eventId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, destination.toString())
                .build();
    }
}
